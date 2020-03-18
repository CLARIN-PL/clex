package pl.clarin.pwr.g419.text;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.*;

public class InformationExtractor implements HasLogger {

  AnnotatorDate annotatorDate = new AnnotatorDate();
  AnnotatorPeriod annotatorPeriod = new AnnotatorPeriod();
  AnnotatorCompanyPrefix annotatorCompanyPrefix = new AnnotatorCompanyPrefix();
  AnnotatorCompanySuffix annotatorCompanySuffix = new AnnotatorCompanySuffix();
  AnnotatorCompany annotatorCompany = new AnnotatorCompany();

  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

  Map<String, String> lemmas = Maps.newHashMap();

  Set<String> ignore = Sets.newHashSet();

  public InformationExtractor() {
    lemmas.put("BANKU", "BANK");
    lemmas.put("TOWARZYSTWA", "TOWARZYSTWO");
    lemmas.put("ZACHODNIEGO", "ZACHODNI");
    lemmas.put("FUNDUSZU", "FUNDUSZ");
    lemmas.put("PRZEDSIĘBIORSTWA", "PRZEDSIĘBIORSTWO");
    lemmas.put("PRODUKCYJNO-HANDLOWEGO", "PRODUKCYJNO-HANDLOWE");
    lemmas.put("NARODOWEGO", "NARODOWY");
    lemmas.put("INWESTYCYJNEGO", "INWESTYCYJNY");
    lemmas.put("GRUPY", "GRUPA");
    lemmas.put("AGORY", "AGORA");

    ignore.add("PÓŁROCZNY");
    ignore.add("DOMINUJĄCA");
    ignore.add("SPÓŁKI");
  }

  public Metadata extract(final HocrDocument document) {
    document.stream().forEach(page -> {
      annotatorDate.annotate(page);
      annotatorPeriod.annotate(page);
      annotatorCompanyPrefix.annotate(page);
      annotatorCompanySuffix.annotate(page);
      annotatorCompany.annotate(page);
    });

    final Metadata metadata = new Metadata();

    final String period = getPeriod(document);
    final String[] parts = period.split(":");
    if (parts.length > 1) {
      metadata.setPeriodFrom(parseDate(parts[0]));
      metadata.setPeriodTo(parseDate(parts[1]));
    }
    metadata.setCompany(getCompany(document));

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

  private String getCompany(final HocrDocument document) {
    final AnnotationList alist = new AnnotationList(document.stream()
        .map(HocrPage::getAnnotations)
        .flatMap(Collection::stream)
        .sorted((a1, a2) -> compareByLocation(a1, a2))
        .collect(Collectors.toList()));

    final String name = alist.filterByType(AnnotatorCompany.COMPANY)
        .stream()
        .sorted((a1, a2) -> compareByLocation(a1, a2))
        .findFirst()
        .map(Annotation::getNorm).orElse("");

    final String stripped = name
        .replaceAll("[„”]", "")
        .replaceAll("[ ][.]", ".");

    return simpyLemmatize(stripped.toUpperCase());
  }

  private String simpyLemmatize(final String text) {
    return Arrays.stream(text.split(" "))
        .map(orth -> lemmas.getOrDefault(orth, orth))
        .filter(orth -> !ignore.contains(orth))
        .collect(Collectors.joining(" "));
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
