package pl.clarin.pwr.g419.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.options.ActionOptionInput;
import pl.clarin.pwr.g419.action.options.ActionOptionMetadata;
import pl.clarin.pwr.g419.action.options.ActionOptionOutput;
import pl.clarin.pwr.g419.action.options.ActionOptionThreads;
import pl.clarin.pwr.g419.io.reader.DocumentsReader;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.Metadata;
import pl.clarin.pwr.g419.struct.MetadataWithContext;
import pl.clarin.pwr.g419.text.InformationExtractor;
import pl.clarin.pwr.g419.text.normalization.MetadataNormalizer;
import pl.clarin.pwr.g419.text.normalization.Normalizer;
import pl.clarin.pwr.g419.text.normalization.NormalizerPersonRole;
import pl.clarin.pwr.g419.utils.TrueFalseCounter;

@Component
public class ActionEval extends Action {

  ActionOptionMetadata optionMetadata = new ActionOptionMetadata();
  ActionOptionInput optionInput = new ActionOptionInput();
  ActionOptionThreads optionThreads = new ActionOptionThreads();
  ActionOptionOutput optionOutput = new ActionOptionOutput();

  InformationExtractor extractor = new InformationExtractor();

  TrueFalseCounter globalCounter = new TrueFalseCounter();
  Map<String, TrueFalseCounter> counters = Maps.newHashMap();

  MetadataNormalizer normalizer = new MetadataNormalizer();

  public ActionEval() {
    super("eval", "evaluate the information extraction module against a dataset");
    this.options.add(optionMetadata);
    this.options.add(optionInput);
    this.options.add(optionOutput);
    this.options.add(optionThreads);

    //normalizer.setPerson(new NormalizerPersonDateRole());
    normalizer.setPerson(new NormalizerPersonRole());
  }

  @Override
  public void run() throws Exception {
    final DocumentsReader reader = new DocumentsReader(optionThreads.getInteger());
    final List<HocrDocument> documents =
        reader.parse(Paths.get(optionMetadata.getString()), Paths.get(optionInput.getString()));

    final ExecutorService service = Executors.newFixedThreadPool(optionThreads.getInteger());
    final List<Future<List<List<String>>>> futures = documents.stream()
        .map(document -> service.submit(() -> processDocument(document)))
        .collect(Collectors.toList());

    final List<List<String>> records = Lists.newArrayList();
    futures.stream().forEach(future -> {
      try {
        records.addAll(future.get());
      } catch (final Exception ex) {
        getLogger().error("Failed evaluate the document", ex);
      }
    });
    service.shutdown();

    try (final Writer out = new BufferedWriter(new FileWriter(optionOutput.getString()));
         final CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.TDF)) {
      try {
        csvPrinter.printRecord(record("Eval", "Document", "Field", "Truth",
            "Extracted", "Context", "Rule"));
        csvPrinter.printRecords(records.stream()
            .sorted(Comparator.comparing(o -> o.get(1)))
            .collect(Collectors.toList()));
      } catch (final Exception ex) {
        getLogger().error("Failed to write to CSV", ex);
      }
    }

    printSummary();
  }

  private void printSummary() {
    System.out.println(String.format("%20s | %5s | %5s | %8s",
        "Type", "True", "False", "Accuracy"));
    System.out.println(StringUtils.repeat("-", 80));
    for (final Map.Entry<String, TrueFalseCounter> entry : counters.entrySet()) {
      final TrueFalseCounter tfc = entry.getValue();
      System.out.println(String.format("%20s | %5d | %5d | %8.2f%%",
          entry.getKey(), tfc.getTrue(), tfc.getFalse(), tfc.getAccuracy()));
    }
    System.out.println(StringUtils.repeat("-", 80));
    final TrueFalseCounter tfc = globalCounter;
    System.out.println(String.format("%20s | %5d | %5d | %8.2f%%",
        "TOTAL", tfc.getTrue(), tfc.getFalse(), tfc.getAccuracy()));
  }

  private List<List<String>> processDocument(final HocrDocument document) {
    final Metadata ref = document.getMetadata();
    final MetadataWithContext metadata = extractor.extract(document);
    final List<List<String>> records = Lists.newArrayList(
        evalField(document.getId(), "drawing_date", normalizer.getDate(),
            ref.getDrawingDate(), metadata.getDrawingDate()),
        evalField(document.getId(), "period_from", normalizer.getDate(),
            ref.getPeriodFrom(), metadata.getPeriodFrom()),
        evalField(document.getId(), "period_to", normalizer.getDate(),
            ref.getPeriodTo(), metadata.getPeriodTo()),
        evalField(document.getId(), "company", normalizer.getCompany(),
            ref.getCompany(), metadata.getCompany())
    );

    records.addAll(evalSets(document.getId(), "person", normalizer.getPerson(),
        ref.getPeople(), metadata.getPeople()));

    return records;
  }

  synchronized private <T> List<String> evalField(final String id,
                                                  final String fieldName,
                                                  final Normalizer<T> normalizer,
                                                  final T reference,
                                                  final FieldContext<T> extracted) {
    final String referenceValue = normalizer.normalize(reference);
    final String extractedValue = normalizer.normalize(extracted.getField());
    if (Objects.equals(referenceValue, extractedValue)) {
      globalCounter.addTrue();
      counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addTrue();
    } else {
      globalCounter.addFalse();
      counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addFalse();
    }
    final String label = Objects.equals(referenceValue, extractedValue) ? "OK" : "ERROR";
    return record(label, id, fieldName, referenceValue, extractedValue,
        extracted.getContext(), extracted.getRule());
  }

  synchronized private <T> List<List<String>> evalSets(final String id,
                                                       final String fieldName,
                                                       final Normalizer<T> normalizer,
                                                       final Collection<T> references,
                                                       final Collection<FieldContext<T>> extracts) {
    final Map<String, T> referenceValues = references.stream()
        .collect(Collectors.toMap(o -> normalizer.normalize(o), Function.identity()));
    final Map<String, FieldContext<T>> extractedValues = extracts.stream()
        .collect(Collectors.toMap(o -> normalizer.normalize(o.getField()), Function.identity()));

    final List<List<String>> records = Lists.newArrayList();
    for (final String reference : referenceValues.keySet()) {
      if (extractedValues.containsKey(reference)) {
        final FieldContext<T> context = extractedValues.get(reference);
        records.add(record("OK", id, fieldName, reference, reference,
            context.getContext(), context.getRule()));
        globalCounter.addTrue();
        counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addTrue();
      } else {
        records.add(record("ERROR", id, fieldName, reference, "FalseNegative",
            "", ""));
        globalCounter.addFalse();
        counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addFalse();
      }
    }
    referenceValues.keySet().stream().forEach(extractedValues::remove);

    for (final String value : extractedValues.keySet()) {
      final FieldContext<T> context = extractedValues.get(value);
      records.add(record("ERROR", id, fieldName, "FalsePositive", value,
          context.getContext(), context.getRule()));
      globalCounter.addFalse();
      counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addFalse();
    }
    return records;
  }

  private List<String> record(final String label, final String id, final String field,
                              final String valueReference, final String valueExtracted,
                              final String context, final String rule) {
    return Lists.newArrayList(label, id, field, valueReference, valueExtracted, context, rule);
  }

}
