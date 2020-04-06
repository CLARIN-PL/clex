package pl.clarin.pwr.g419.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import pl.clarin.pwr.g419.kbase.CompanyNormalizer;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.MetadataWithContext;
import pl.clarin.pwr.g419.struct.Person;
import pl.clarin.pwr.g419.text.InformationExtractor;
import pl.clarin.pwr.g419.utils.TrueFalseCounter;

@Component
public class ActionEval extends Action {

  ActionOptionMetadata optionMetadata = new ActionOptionMetadata();
  ActionOptionInput optionInput = new ActionOptionInput();
  ActionOptionThreads optionThreads = new ActionOptionThreads();
  ActionOptionOutput optionOutput = new ActionOptionOutput();

  List<String> correct = Lists.newArrayList();
  List<String> incorrect = Lists.newArrayList();

  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
  CompanyNormalizer normCompany = new CompanyNormalizer();

  InformationExtractor extractor = new InformationExtractor();

  TrueFalseCounter globalCounter = new TrueFalseCounter();
  Map<String, TrueFalseCounter> counters = Maps.newHashMap();

  public ActionEval() {
    super("eval", "evaluate the information extraction module against a dataset");
    this.options.add(optionMetadata);
    this.options.add(optionInput);
    this.options.add(optionOutput);
    this.options.add(optionThreads);
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
            "Extracted", "Context"));
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
    final MetadataWithContext metadata = extractor.extract(document);
    final List<List<String>> records = Lists.newArrayList();

    records.add(evalField(document.getId(), "drawing_date",
        formatDate(document.getMetadata().getDrawingDate()),
        formatDate(metadata.getDrawingDate()),
        metadata.getDrawingDateContext())
    );

    records.add(evalField(document.getId(), "period_from",
        formatDate(document.getMetadata().getPeriodFrom()),
        formatDate(metadata.getPeriodFrom()),
        metadata.getPeriodFromContext())
    );

    records.add(evalField(document.getId(), "period_to",
        formatDate(document.getMetadata().getPeriodTo()),
        formatDate(metadata.getPeriodTo()),
        metadata.getPeriodFromContext())
    );

    records.add(evalField(document.getId(), "company",
        normCompany.normalize(document.getMetadata().getCompany()),
        normCompany.normalize(metadata.getCompany()),
        metadata.getCompanyContext())
    );

    records.addAll(evalSets(document.getId(),
        "person",
        peopleToString(document.getMetadata().getPeople()),
        peopleToString(metadata.getPeople())));

    return records;
  }

  private Set<String> peopleToString(final Collection<Person> people) {
    return people.stream()
        .map(this::formatPerson)
        .map(this::normalizePerson)
        .collect(Collectors.toSet());
  }

  private String formatPerson(final Person person) {
    return String.format("%s_%s_%s",
        //formatDate(person.getDate()),
        "",
        person.getRole().toLowerCase(),
        person.getName()
    );
  }

  private String formatDate(final Date date) {
    if (date == null) {
      return "";
    }
    return format.format(date);
  }

  private String normalizePerson(final String value) {
    if (value == null) {
      return "";
    }
    return value
        .replaceAll("( )*([-])( )*", "$2")
        .replaceAll("_główna księgowa_", "_główny księgowy_")
        .replaceAll("zarzadu", "zarządu")
        .replaceAll("wieceprezes", "wiceprezes")
        .replaceAll("wiceprezez", "wiceprezes")
        .replace("czlonek", "członek")
        .replaceAll(" (spółki|banku)", "")
        .replaceAll(" (zarządu)", "")
        .replaceAll(" ds[.] [^_]+", "_")
        ;
  }

  synchronized private List<String> evalField(final String id,
                                              final String fieldName,
                                              final String referenceValue,
                                              final String extractedValue) {
    return evalField(id, fieldName, referenceValue, extractedValue, "");
  }


  synchronized private List<String> evalField(final String id,
                                              final String fieldName,
                                              final String referenceValue,
                                              final String extractedValue,
                                              final String extractedValueContext) {
    if (Objects.equals(referenceValue, extractedValue)) {
      globalCounter.addTrue();
      counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addTrue();
    } else {
      globalCounter.addFalse();
      counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addFalse();
    }

    final String label = Objects.equals(referenceValue, extractedValue) ? "OK" : "ERROR";
    return record(label, id, fieldName, referenceValue, extractedValue, extractedValueContext);
  }

  synchronized private List<List<String>> evalSets(final String id,
                                                   final String fieldName,
                                                   final Set<String> referenceValues,
                                                   final Set<String> extractedValues) {
    final List<List<String>> records = Lists.newArrayList();
    for (final String reference : referenceValues) {
      if (extractedValues.contains(reference)) {
        records.add(record("OK", id, fieldName, reference, reference, ""));
        globalCounter.addTrue();
        counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addTrue();
      } else {
        records.add(record("ERROR", id, fieldName, reference, "FalseNegative", ""));
        globalCounter.addFalse();
        counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addFalse();
      }
    }
    extractedValues.removeAll(referenceValues);
    for (final String value : extractedValues) {
      records.add(record("ERROR", id, fieldName, "FalsePositive", value, ""));
      globalCounter.addFalse();
      counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addFalse();
    }
    return records;
  }

  private List<String> record(final String label, final String id, final String field,
                              final String valueReference,
                              final String valueExtracted, final String valueExtractedContext) {
    return Lists.newArrayList(label, id, field, valueReference, valueExtracted, valueExtractedContext);
  }

}
