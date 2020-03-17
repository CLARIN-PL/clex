package pl.clarin.pwr.g419.text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.AnnotatorDate;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPeriod;

public class InformationExtractor implements HasLogger {

  AnnotatorDate annotatorDate = new AnnotatorDate();
  AnnotatorPeriod annotatorPeriod = new AnnotatorPeriod();

  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

  public Metadata extract(final HocrDocument document) {
    document.stream().forEach(page -> {
      annotatorDate.annotate(page);
      annotatorPeriod.annotate(page);
    });

    final Metadata metadata = new Metadata();

    final String period = getPeriod(document);
    final String[] parts = period.split(":");
    if (parts.length > 1) {
      metadata.setPeriodFrom(parseDate(parts[0]));
      metadata.setPeriodTo(parseDate(parts[1]));
    }

    return metadata;
  }

  private String getPeriod(final HocrDocument document) {
    AnnotationList alist = new AnnotationList(document.stream()
        .map(HocrPage::getAnnotations)
        .flatMap(Collection::stream)
        .sorted(Comparator.comparingInt(o -> o.getFirst().getNo()))
        .collect(Collectors.toList()));

    alist = alist.filterByType(AnnotatorPeriod.PERIOD);

    final List<String> periods = alist.stream()
        .sorted((a1, a2) -> compareByLocation(a1, a2))
        .map(Annotation::getNorm)
        .collect(Collectors.toList());

    return periods.stream().findFirst().orElse("");
  }

  private Date parseDate(final String str) {
    try {
      return format.parse(str);
    } catch (final ParseException e) {
      return null;
    }
  }

  private int compareByLocation(final Annotation a1, final Annotation a2) {
    final int page = Integer.compare(a1.getPage().getNo(), a2.getPage().getNo());
    if (page != 0) {
      return page;
    }
    final int top = Integer.compare(a1.getFirst().getBox().getTop(), a2.getFirst().getBox().getTop());
    if (top != 0) {
      return top;
    }
    return Integer.compare(a1.getFirst().getBox().getLeft(), a2.getFirst().getBox().getLeft());
  }
}
