package pl.clarin.pwr.g419.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.options.*;
import pl.clarin.pwr.g419.io.reader.DocumentsReader;
import pl.clarin.pwr.g419.io.writer.MetadataWriter;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.Metadata;
import pl.clarin.pwr.g419.struct.MetadataWithContext;
import pl.clarin.pwr.g419.text.InformationExtractor;
import pl.clarin.pwr.g419.text.normalization.MetadataNormalizer;
import pl.clarin.pwr.g419.text.normalization.Normalizer;
import pl.clarin.pwr.g419.utils.TrueFalseCounter;

import static pl.clarin.pwr.g419.struct.Metadata.*;

@Component
@Slf4j
public class ActionEval extends Action {

  ActionOptionMetadata optionMetadata = new ActionOptionMetadata();
  ActionOptionInput optionInput = new ActionOptionInput();
  ActionOptionOutput optionOutput = new ActionOptionOutput();
  ActionOptionSelectOne optionSelectOne = new ActionOptionSelectOne();
  ActionOptionPersonEvaluationVariants optionPersonEvaluationVariants
      = new ActionOptionPersonEvaluationVariants();

  InformationExtractor extractor = new InformationExtractor();

  TrueFalseCounter globalCounter = new TrueFalseCounter();
  Map<String, TrueFalseCounter> counters = Maps.newHashMap();

  MetadataNormalizer normalizer = new MetadataNormalizer();

  List<Metadata> outFileMetadataList = new LinkedList<>();

  public ActionEval() {
    super("eval", "evaluate the information extraction module against a dataset");
    this.options.add(optionMetadata);
    this.options.add(optionInput);
    this.options.add(optionOutput);
    this.options.add(optionSelectOne);
    this.options.add(optionPersonEvaluationVariants);

    //normalizer.setPerson(new NormalizerPersonRole());
  }

  @Override
  public void run() throws Exception {
    final DocumentsReader reader = new DocumentsReader();

    // do pamięci zaczytujemy wszystkie metadane ...
    final Path metadataCsv = Paths.get(optionMetadata.getString());
    final List<Metadata> metadata = reader.loadMetadata(metadataCsv);
    final Map<String, Metadata> idToMetadata =
        metadata.stream().collect(Collectors.toMap(Metadata::getId, Function.identity()));

    // do pamięci zaczytujemy wszystkie ściężki do dokumentów ...
    final Path hocrIndex = Paths.get(optionInput.getString());
    List<Path> paths;
    if (hocrIndex.toString().endsWith(".hocr")) {
      paths = List.of(hocrIndex);
    } else {
      paths = reader.loadPaths(hocrIndex);
    }

    // jeśli jest podane zawężenie do katalogu o podanym numerze to weż poda uwagę tylko ten dokument
    if (optionSelectOne.getString() != null) {
      log.info("Podano parameter selectOne = " + optionSelectOne.getString());
      paths = paths.stream().filter(p -> p.getParent().endsWith(optionSelectOne.getString())).collect(Collectors.toList());
    }

    if (optionPersonEvaluationVariants.getString() != null) {
      log.info("Podano parameter personevalvariant = " + optionPersonEvaluationVariants.getString());
    }


    // dla każdej pojedynczej ścieżki zaczytuajemy jej dokument i zapamiętujemy tylko wyniki
    // jego przetwarzania
    final List<List<String>> records = Collections.synchronizedList(new LinkedList<>());
    paths.parallelStream().forEach(path -> {
      try {
        evaluateOneDocumentWithPath(reader, path, idToMetadata, records);
      } catch (final Exception ex) {
        getLogger().error("Failed evaluate the document. Path = " + path, ex);
      }
    });

    printRecords(records);

    printSummary();

    // gdybyśmy jeszcze tu chcieli drukować plik zgodny z z formatem konkursu PolEval 2020
    //printPolEvalFile();
  }


  // Lista records musi tu być przekazana jako synchornized gdy używamy wielu wątków
  private void evaluateOneDocumentWithPath(final DocumentsReader reader, final Path path, final Map<String, Metadata> idToMetadata, final List<List<String>> records)
      throws Exception {
    getLogger().info(String.format("Starting processing document %s", path.toString()));
    final HocrDocument document = reader.loadHocrDocument(path);
    getLogger().info(String.format("%3d page(s) in %s", document.size(), path.toString()));
    document.setMetadata(idToMetadata.getOrDefault(document.getId(), new Metadata()));
    final List<List<String>> result = processDocument(document);
    records.addAll(result);
  }


  private void printRecords(final List<List<String>> records) throws IOException {
    try (final Writer out = new BufferedWriter(new FileWriter(optionOutput.getString()));
         final CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.TDF)) {
      try {
        csvPrinter.printRecord(record("Eval", "Document", "Field",
            "Truth Normalized", "Extracted Normalized",
            "Truth", "Extracted", "Context", "Rule"));
        csvPrinter.printRecords(records.stream()
            .sorted(Comparator.comparing(o -> o.get(1)))
            .collect(Collectors.toList()));
      } catch (final Exception ex) {
        getLogger().error("Failed to write to CSV", ex);
      }
    }
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
//        evalField(document.getId(), "sign_page", normalizer.getSignPage(),
//            ref.getSignsPage(), metadata.getSignsPage()),
        evalField(document.getId(), DRAWING_DATE, normalizer.getDate(),
            ref.getDrawingDate(), metadata.getDrawingDate()),
        evalField(document.getId(), PERIOD_FROM, normalizer.getDate(),
            ref.getPeriodFrom(), metadata.getPeriodFrom()),
        evalField(document.getId(), PERIOD_TO, normalizer.getDate(),
            ref.getPeriodTo(), metadata.getPeriodTo()),
        evalField(document.getId(), COMPANY, normalizer.getCompany(),
            ref.getCompany(), metadata.getCompany()),
        evalField(document.getId(), POSTAL_CODE, normalizer.getPostalCode(),
            ref.getPostalCode(), metadata.getPostalCode()),
        evalField(document.getId(), CITY, normalizer.getCity(),
            ref.getCity(), metadata.getCity()),
        evalField(document.getId(), STREET, normalizer.getStreet(),
            ref.getStreet(), metadata.getStreet()),
        evalField(document.getId(), STREET_NO, normalizer.getStreetNo(),
            ref.getStreetNo(), metadata.getStreetNo())
    );

    final String pev = optionPersonEvaluationVariants.getString();
    int intPev = 3;
    if ((pev != null) && (pev.equals("2"))) {
      intPev = 2;
    }

    if (intPev == 3) {
      records.addAll(evalSets(document.getId(), "person", normalizer.getPerson(),
          ref.getPeople(), metadata.getPeople()));
    } else if (intPev == 2) {
      records.addAll(evalSets(document.getId(), "person-role", normalizer.getPersonRole(),
          ref.getPeople(), metadata.getPeople()));
      records.addAll(evalSets(document.getId(), "person-date", normalizer.getPersonDate(),
          ref.getPeople(), metadata.getPeople()));
    }

    Metadata outFileMetadata = Metadata.of(records);
    outFileMetadata.setPeople(metadata.getPeople().stream().map(fc -> fc.getField()).collect(Collectors.toList()));
    outFileMetadataList.add(outFileMetadata);

    return records;
  }

  synchronized private <T> List<String> evalField(final String id,
                                                  final String fieldName,
                                                  final Normalizer<T> normalizer,
                                                  final T reference,
                                                  final FieldContext<T> extracted) {
    final String referenceValueNorm = normalizer.normalize(reference);
    final String extractedValueNorm = normalizer.normalize(extracted.getField());
    if (Objects.equals(referenceValueNorm, extractedValueNorm)) {
      globalCounter.addTrue();
      counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addTrue();
    } else {
      globalCounter.addFalse();
      counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addFalse();
    }
    final String label = Objects.equals(referenceValueNorm, extractedValueNorm) ? "OK" : "ERROR";
    return record(label, id, fieldName, referenceValueNorm, extractedValueNorm,
        "" + reference, "" + extracted.getField(), extracted.getContext(), extracted.getRule());
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
        records.add(record("OK", id, fieldName, reference, reference, reference, reference,
            context.getContext(), context.getRule()));
        globalCounter.addTrue();
        counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addTrue();
      } else {
        records.add(record("ERROR", id, fieldName, reference,
            "FalseNegative", "", "",
            "", ""));
        globalCounter.addFalse();
        counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addFalse();
      }
    }
    referenceValues.keySet().stream().forEach(extractedValues::remove);

    for (final String value : extractedValues.keySet()) {
      final FieldContext<T> context = extractedValues.get(value);
      records.add(record("ERROR", id, fieldName,
          "FalsePositive", value, "", "",
          context.getContext(), context.getRule()));
      globalCounter.addFalse();
      counters.computeIfAbsent(fieldName, o -> new TrueFalseCounter()).addFalse();
    }
    return records;
  }

  private List<String> record(final String label, final String id, final String field,
                              final String valueReferenceNorm, final String valueExtractedNorm,
                              final String valueReference, final String valueExtracted,
                              final String context, final String rule) {
    return Lists.newArrayList(label, id, field, valueReferenceNorm, valueExtractedNorm,
        valueReference, valueExtracted, context, rule);
  }

  private void printPolEvalFile() throws IOException {
    Path path = Path.of("outfile.tsv");
    new MetadataWriter().write(outFileMetadataList, path);
  }


}
