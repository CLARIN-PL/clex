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
public class ActionWriteOutResults extends Action {

  ActionOptionInput optionInput = new ActionOptionInput();
  ActionOptionOutput optionOutput = new ActionOptionOutput();
  ActionOptionSelectOne optionSelectOne = new ActionOptionSelectOne();

  InformationExtractor extractor = new InformationExtractor();

  TrueFalseCounter globalCounter = new TrueFalseCounter();
  Map<String, TrueFalseCounter> counters = Maps.newHashMap();

  MetadataNormalizer normalizer = new MetadataNormalizer();

  List<Metadata> outFileMetadataList = new LinkedList<>();

  public ActionWriteOutResults() {
    super("writeOut", "write out the information extraction to file");

    this.options.add(optionInput);
    this.options.add(optionOutput);
    this.options.add(optionSelectOne);
  }

  @Override
  public void run() throws Exception {
    final DocumentsReader reader = new DocumentsReader();

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

    // dla każdej pojedynczej ścieżki zaczytuajemy jej dokument i zapamiętujemy tylko wyniki
    // jego przetwarzania
    final List<List<String>> records = Collections.synchronizedList(new LinkedList<>());
    paths.parallelStream().forEach(path -> {
      try {
        evaluateOneDocumentWithPath(reader, path, records);
      } catch (final Exception ex) {
        getLogger().error("Failed evaluate the document. Path = " + path, ex);
      }
    });

    printRecords(records);

    printSummary();

    Path path = Path.of("outfile.csv");
    new MetadataWriter().write(outFileMetadataList, path);

  }

  // Lista records musi tu być przekazana jako synchornized gdy używamy wielu wątków
  private void evaluateOneDocumentWithPath(final DocumentsReader reader, final Path path, final List<List<String>> records)
      throws Exception {
    getLogger().info(String.format("Starting processing document %s", path.toString()));
    final HocrDocument document = reader.loadHocrDocument(path);
    getLogger().info(String.format("%3d page(s) in %s", document.size(), path.toString()));
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

    final MetadataWithContext metadata = extractor.extract(document);


    final List<List<String>> records = Lists.newArrayList(
        getValForField(document.getId(), Metadata.DRAWING_DATE, normalizer.getDate(),
            metadata.getDrawingDate()),
        getValForField(document.getId(), PERIOD_FROM, normalizer.getDate(),
            metadata.getPeriodFrom()),
        getValForField(document.getId(), PERIOD_TO, normalizer.getDate(),
            metadata.getPeriodTo()),
        getValForField(document.getId(), COMPANY, normalizer.getCompany(),
            metadata.getCompany()),
        getValForField(document.getId(), POSTAL_CODE, normalizer.getPostalCode(),
            metadata.getPostalCode()),
        getValForField(document.getId(), CITY, normalizer.getCity(),
            metadata.getCity()),
        getValForField(document.getId(), STREET, normalizer.getStreet(),
            metadata.getStreet()),
        getValForField(document.getId(), STREET_NO, normalizer.getStreetNo(),
            metadata.getStreetNo())
    );

    //List<List<String>> outRecords = composeOutRecord(records);

//    final String pev = optionPersonEvaluationVariants.getString();
//    int intPev = 3;
//    if ((pev != null) && (pev.equals("2"))) {
//      intPev = 2;
//    }
//
//    if (intPev == 3) {
//      records.addAll(evalSets(document.getId(), "person", normalizer.getPerson(),
//          metadata.getPeople()));
//    } else if (intPev == 2) {
//      records.addAll(evalSets(document.getId(), "person-role", normalizer.getPersonRole(),
//          metadata.getPeople()));
//      records.addAll(evalSets(document.getId(), "person-date", normalizer.getPersonDate(),
//          metadata.getPeople()));
//    }

    Metadata outFileMetadata = Metadata.of(records);
    outFileMetadataList.add(outFileMetadata);

    return records;
  }

  synchronized private <T> List<String> getValForField(final String id,
                                                       final String fieldName,
                                                       final Normalizer<T> normalizer,
                                                       final FieldContext<T> extracted) {

    final String extractedValueNorm = normalizer.normalize(extracted.getField());

    String label = "";
    String referenceValueNorm = "";
    String reference = "";

    return record(label, id, fieldName, referenceValueNorm, extractedValueNorm,
        "" + reference, "" + extracted.getField(), extracted.getContext(), extracted.getRule());
  }
  

  /*
  synchronized private <T> List<List<String>> evalSets(final String id,
                                                       final String fieldName,
                                                       final Normalizer<T> normalizer,
                                                       final Collection<FieldContext<T>> extracts) {
//    final Map<String, T> referenceValues = references.stream()
//        .collect(Collectors.toMap(o -> normalizer.normalize(o), Function.identity()));
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
   */


  private List<String> record(final String label, final String id, final String field,
                              final String valueReferenceNorm, final String valueExtractedNorm,
                              final String valueReference, final String valueExtracted,
                              final String context, final String rule) {
    return Lists.newArrayList(label, id, field, valueReferenceNorm, valueExtractedNorm,
        valueReference, valueExtracted, context, rule);
  }


}
