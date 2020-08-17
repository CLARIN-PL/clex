package pl.clarin.pwr.g419.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.options.ActionOptionInput;
import pl.clarin.pwr.g419.action.options.ActionOptionOutput;
import pl.clarin.pwr.g419.action.options.ActionOptionReport;
import pl.clarin.pwr.g419.action.options.ActionOptionSelectOne;
import pl.clarin.pwr.g419.io.reader.DocumentsReader;
import pl.clarin.pwr.g419.io.reader.HocrReader;
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
  ActionOptionReport optionReport = new ActionOptionReport();
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
    this.options.add(optionReport);
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
      paths = paths.stream()
          .filter(p -> p.getParent().endsWith(optionSelectOne.getString()))
          .collect(Collectors.toList());
    }

    final List<String> documentsFailed = Lists.newArrayList();

    final List<List<String>> records = Collections.synchronizedList(new LinkedList<>());
    paths.parallelStream().forEach(path -> {
      try {
        evaluateOneDocumentWithPath(reader, path, records);
      } catch (final Exception ex) {
        documentsFailed.add(HocrReader.getIdFromPath(path));
        getLogger().error("Failed evaluate the document. Path = " + path, ex);
      }
    });

    for (final String documentId : documentsFailed) {
      final Metadata metadata = new Metadata();
      metadata.setId(documentId);
      outFileMetadataList.add(metadata);
    }

    printRecords(records);

    final Path path = Path.of(optionOutput.getString());
    new MetadataWriter().write(outFileMetadataList, path);

  }

  // Lista records musi tu być przekazana jako synchornized gdy używamy wielu wątków
  private void evaluateOneDocumentWithPath(final DocumentsReader reader,
                                           final Path path,
                                           final List<List<String>> records)
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


  private List<List<String>> processDocument(final HocrDocument document) {

    final MetadataWithContext metadata = extractor.extract(document);
    final List<List<String>> records = metadataToRecord(document.getId(), metadata);
    final Metadata outFileMetadata = Metadata.of(records);
    outFileMetadata.setPeople(metadata.getPeople().stream()
        .map(fc -> fc.getField())
        .collect(Collectors.toList()));
    outFileMetadataList.add(outFileMetadata);

    return records;
  }

  private List<List<String>> metadataToRecord(final String documentId, final MetadataWithContext metadata) {
    return Lists.newArrayList(
        getValForField(documentId, DRAWING_DATE, normalizer.getDate(), metadata.getDrawingDate()),
        getValForField(documentId, PERIOD_FROM, normalizer.getDate(), metadata.getPeriodFrom()),
        getValForField(documentId, PERIOD_TO, normalizer.getDate(), metadata.getPeriodTo()),
        getValForField(documentId, COMPANY, normalizer.getCompany(), metadata.getCompany()),
        getValForField(documentId, POSTAL_CODE, normalizer.getPostalCode(), metadata.getPostalCode()),
        getValForField(documentId, CITY, normalizer.getCity(), metadata.getCity()),
        getValForField(documentId, STREET, normalizer.getStreet(), metadata.getStreet()),
        getValForField(documentId, STREET_NO, normalizer.getStreetNo(), metadata.getStreetNo())
    );
  }

  synchronized private <T> List<String> getValForField(final String id,
                                                       final String fieldName,
                                                       final Normalizer<T> normalizer,
                                                       final FieldContext<T> extracted) {

    final String extractedValueNorm = normalizer.normalize(extracted.getField());

    final String label = "";
    final String referenceValueNorm = "";
    final String reference = "";

    return record(label, id, fieldName, referenceValueNorm, extractedValueNorm,
        "" + reference, "" + extracted.getField(), extracted.getContext(), extracted.getRule());
  }

  private List<String> record(final String label, final String id, final String field,
                              final String valueReferenceNorm, final String valueExtractedNorm,
                              final String valueReference, final String valueExtracted,
                              final String context, final String rule) {
    return Lists.newArrayList(label, id, field, valueReferenceNorm, valueExtractedNorm,
        valueReference, valueExtracted, context, rule);
  }


}
