package pl.clarin.pwr.g419.action;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.options.ActionOptionInput;
import pl.clarin.pwr.g419.action.options.ActionOptionMetadata;
import pl.clarin.pwr.g419.action.options.ActionOptionThreads;
import pl.clarin.pwr.g419.io.reader.DocumentsReader;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.AnnotatorDate;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPeriod;

@Component
public class ActionContextPeriod extends Action {

  ActionOptionMetadata optionMetadata = new ActionOptionMetadata();
  ActionOptionInput optionInput = new ActionOptionInput();
  ActionOptionThreads optionThreads = new ActionOptionThreads();

  AnnotatorDate annotatorDate = new AnnotatorDate();
  AnnotatorPeriod annotatorPeriod = new AnnotatorPeriod();

  public ActionContextPeriod() {
    super("context-period", "print context of periods");
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
    final String referenceRange = getMetadataDateRange(document);
    new AnnotationList(document.stream()
        .peek(this::processPage)
        .map(HocrPage::getAnnotations)
        .flatMap(Collection::stream)
        .collect(Collectors.toList()))
        .filterByType(AnnotatorPeriod.PERIOD)
        .stream().map(an -> String.format("%s\t%s\t%s",
        referenceRange, an.getText(), getContext(an, 5)))
        .forEach(System.out::println);
  }

  private String getContext(final Annotation annotation, final int window) {
    return IntStream.range(Math.max(0, annotation.getIndexBegin() - window),
        Math.min(annotation.getPage().size(), annotation.getIndexEnd() + window))
        .mapToObj(annotation.getPage()::get)
        .map(Bbox::getText).collect(Collectors.joining(" "));
  }

  private String getMetadataDateRange(final HocrDocument document) {
    final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    return format.format(document.getMetadata().getPeriodFrom()) + ":" +
        format.format(document.getMetadata().getPeriodTo());
  }

  private void processPage(final HocrPage page) {
    annotatorDate.annotate(page);
    annotatorPeriod.annotate(page);
  }
}
