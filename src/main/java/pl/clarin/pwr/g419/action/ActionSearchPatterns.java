package pl.clarin.pwr.g419.action;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.options.ActionOptionInput;
import pl.clarin.pwr.g419.action.options.ActionOptionMetadata;
import pl.clarin.pwr.g419.action.options.ActionOptionThreads;
import pl.clarin.pwr.g419.io.reader.DocumentsReader;
import pl.clarin.pwr.g419.struct.Annotation;
import pl.clarin.pwr.g419.struct.AnnotationList;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.HocrPage;
import pl.clarin.pwr.g419.text.annotator.AnnotatorDate;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPeriod;

@Component
public class ActionSearchPatterns extends Action {

  ActionOptionMetadata optionMetadata = new ActionOptionMetadata();
  ActionOptionInput optionInput = new ActionOptionInput();
  ActionOptionThreads optionThreads = new ActionOptionThreads();

  AnnotatorDate annotatorDate = new AnnotatorDate();
  AnnotatorPeriod annotatorPeriod = new AnnotatorPeriod();

  public ActionSearchPatterns() {
    super("search-patterns", "search predefined patterns in the documents");
    this.options.add(optionMetadata);
    this.options.add(optionInput);
    this.options.add(optionThreads);
  }

  @Override
  public void run() throws Exception {
    final DocumentsReader reader = new DocumentsReader(optionThreads.getInteger());
    final List<HocrDocument> documents =
        reader.parse(Paths.get(optionMetadata.getString()), Paths.get(optionInput.getString()));

    documents.forEach(this::processDocument);
  }

  private void processDocument(final HocrDocument document) {
    AnnotationList alist = new AnnotationList(document.stream()
        .peek(this::processPage)
        .map(HocrPage::getAnnotations)
        .flatMap(Collection::stream)
        .collect(Collectors.toList()));

    alist = alist.filterByType(AnnotatorPeriod.PERIOD);

    final String referenceRange = getMetadataDateRange(document);

    if (alist.size() == 0) {
      System.out.println(String.format("[%s]\t%s\t0\tNOT_FOUND", document.getId(), referenceRange));
    } else {
      final Set<String> periods = alist.stream()
          .map(Annotation::toString)
          .collect(Collectors.toSet());

      System.out.println(
          String.format("[%s]\t%s\t%d\t%s", document.getId(), referenceRange, periods.size(),
              String.join(", ", periods)));
    }
  }

  private String getMetadataDateRange(final HocrDocument document) {
    final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
    return format.format(document.getMetadata().getPeriodFrom()) + ":" +
        format.format(document.getMetadata().getPeriodTo());
  }

  private void processPage(final HocrPage page) {
    annotatorDate.annotate(page);
    annotatorPeriod.annotate(page);
  }
}
