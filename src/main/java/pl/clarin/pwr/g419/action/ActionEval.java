package pl.clarin.pwr.g419.action;

import com.google.common.collect.Lists;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.options.ActionOptionInput;
import pl.clarin.pwr.g419.action.options.ActionOptionMetadata;
import pl.clarin.pwr.g419.action.options.ActionOptionOutput;
import pl.clarin.pwr.g419.action.options.ActionOptionThreads;
import pl.clarin.pwr.g419.io.reader.DocumentsReader;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.Metadata;
import pl.clarin.pwr.g419.text.InformationExtractor;

@Component
public class ActionEval extends Action {

  ActionOptionMetadata optionMetadata = new ActionOptionMetadata();
  ActionOptionInput optionInput = new ActionOptionInput();
  ActionOptionThreads optionThreads = new ActionOptionThreads();
  ActionOptionOutput optionOutput = new ActionOptionOutput();

  List<String> correct = Lists.newArrayList();
  List<String> incorrect = Lists.newArrayList();

  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

  InformationExtractor extractor = new InformationExtractor();

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
        getLogger().error("Failed to write to CSV", ex);
      }
    });
    service.shutdown();

    try (final Writer out = new BufferedWriter(new FileWriter(optionOutput.getString()));
         final CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.TDF)) {
      try {
        csvPrinter.printRecords(records.stream()
            .sorted(Comparator.comparing(o -> o.get(1)))
            .collect(Collectors.toList()));
      } catch (final Exception ex) {
        getLogger().error("Failed to write to CSV", ex);
      }
    }

    final double sum = correct.size() + incorrect.size();
    final double accuracy = sum == 0 ? 0.0 : ((double) correct.size() * 100.0 / sum);
    System.out.println(String.format("Correct  : %5d", correct.size()));
    System.out.println(String.format("Incorrect: %5d", incorrect.size()));
    System.out.println(String.format("Accuracy : %8.2f%%", accuracy));
  }

  private List<List<String>> processDocument(final HocrDocument document) {
    final Metadata metadata = extractor.extract(document);
    final List<List<String>> records = Lists.newArrayList();

    records.add(evalField(document.getId(), "period_from",
        formatDate(document.getMetadata().getPeriodFrom()),
        formatDate(metadata.getPeriodFrom()))
    );

    records.add(evalField(document.getId(), "period_to",
        formatDate(document.getMetadata().getPeriodTo()),
        formatDate(metadata.getPeriodTo()))
    );

    return records;
  }

  private String formatDate(final Date date) {
    if (date == null) {
      return "";
    }
    return format.format(date);
  }

  private List<String> evalField(final String id, final String fieldName,
                                 final String referenceValue, final String extractedValue) {
    final List<String> record = Lists.newArrayList();
    record.add(Objects.equals(referenceValue, extractedValue) ? "OK" : "ERROR");
    record.add(id);
    record.add(fieldName);
    record.add(referenceValue);
    record.add(extractedValue);

    if (Objects.equals(referenceValue, extractedValue)) {
      correct.add(id);
    } else {
      incorrect.add(id);
    }

    return record;
  }

}
