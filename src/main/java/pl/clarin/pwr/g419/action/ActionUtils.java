package pl.clarin.pwr.g419.action;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import pl.clarin.pwr.g419.action.options.ActionOptionSelectOne;
import pl.clarin.pwr.g419.io.reader.DocumentsReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ActionUtils {

  public static List<Path> getPaths(DocumentsReader reader, String inputFile, ActionOptionSelectOne optionSelectOne) throws IOException {
    final Path hocrIndex = Paths.get(inputFile);
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
    return paths;
  }

  public static void printRecords(final List<List<String>> records, String targetFile) throws IOException {
    try (final Writer out = new BufferedWriter(new FileWriter(targetFile));
         final CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.TDF)) {
      try {
        csvPrinter.printRecord(record("Eval", "Document", "Field",
            "Truth Normalized", "Extracted Normalized",
            "Truth", "Extracted", "Context", "Rule"));
        csvPrinter.printRecords(records.stream()
            .sorted(Comparator.comparing(o -> o.get(1)))
            .collect(Collectors.toList()));
      } catch (final Exception ex) {
        log.error("Failed to write to CSV", ex);
      }
    }
  }


  public static List<String> record(final String label, final String id, final String field,
                                    final String valueReferenceNorm, final String valueExtractedNorm,
                                    final String valueReference, final String valueExtracted,
                                    final String context, final String rule) {
    return Lists.newArrayList(label, id, field, valueReferenceNorm, valueExtractedNorm,
        valueReference, valueExtracted, context, rule);
  }


}
