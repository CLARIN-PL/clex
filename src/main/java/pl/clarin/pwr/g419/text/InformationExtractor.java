package pl.clarin.pwr.g419.text;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.*;

public class InformationExtractor implements HasLogger {


  List<Annotator> annotators = Lists.newArrayList(
      new AnnotatorDate(),
      new AnnotatorPeriod(),
      new AnnotatorCompanyPrefix(),
      new AnnotatorCompanySuffix(),
      new AnnotatorCompany(),
      new AnnotatorPerson(),
      new AnnotatorDrawingDate()
  );

  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

  Map<String, String> lemmas = Maps.newHashMap();

  Set<String> ignore = Sets.newHashSet();

  public InformationExtractor() {
    lemmas.put("AGORY", "AGORA");
    lemmas.put("BANKU", "BANK");
    lemmas.put("DOMU", "DOM");
    lemmas.put("FABRYKI", "FABRYKA");
    lemmas.put("GRUPY", "GRUPA");
    lemmas.put("HANDLOWEGO", "HANDLOWY");
    lemmas.put("MAKLERSKIEGO", "MAKLERSKI");
    lemmas.put("NARODOWEGO", "NARODOWY");
    lemmas.put("TOWARZYSTWA", "TOWARZYSTWO");
    lemmas.put("ZACHODNIEGO", "ZACHODNI");
    lemmas.put("FUNDUSZU", "FUNDUSZ");
    lemmas.put("PRZEDSIĘBIORSTWA", "PRZEDSIĘBIORSTWO");
    lemmas.put("PRODUKCYJNO-HANDLOWEGO", "PRODUKCYJNO-HANDLOWE");
    lemmas.put("INWESTYCYJNEGO", "INWESTYCYJNY");
    lemmas.put("FABRYK", "FABRYKI");
    lemmas.put("NFI", "NARODOWY FUNDUSZ INWESTYCYJNY");

    ignore.add("PÓŁROCZNY");
    ignore.add("DOMINUJĄCA");
    ignore.add("SPÓŁKI");
    ignore.add("FIRMY");
    ignore.add("UDZIAŁ");
    ignore.add("%");
  }

  public MetadataWithContext extract(final HocrDocument document) {
    document.stream().forEach(page -> annotators.forEach(an -> an.annotate(page)));

    final MetadataWithContext metadata = new MetadataWithContext();

    final ValueContext vcPeriod = getPeriod(document);
    final String[] parts = vcPeriod.getValue().split(":");
    if (parts.length > 1) {
      metadata.setPeriodFrom(parseDate(parts[0]));
      metadata.setPeriodFromContext(vcPeriod.getContext());
      metadata.setPeriodTo(parseDate(parts[1]));
      metadata.setPeriodToContext(vcPeriod.getContext());
    }

    final ValueContext vcCompany = getCompany(document);
    metadata.setCompany(vcCompany.getValue());
    metadata.setCompanyContext(vcCompany.getContext());

    final ValueContext vcDrawingDate = getDrawingDate(document);
    metadata.setDrawingDate(parseDate(vcDrawingDate.getValue()));
    metadata.setDrawingDateContext(vcDrawingDate.getContext());

    metadata.setPeople(getPoeple(document));

    return metadata;
  }

  private List<Person> getPoeple(final HocrDocument document) {
    return document.getAnnotations()
        .filterByType(AnnotatorPerson.PERSON)
        .removeNested()
        .stream()
        .map(Annotation::getNorm)
        .collect(Collectors.toSet())
        .stream()
        .map(this::strToPerson)
        .collect(Collectors.toList());
  }

  private Person strToPerson(final String str) {
    final Person person = new Person();
    final String[] parts = str.split("[|]");
    if (parts.length == 2) {
      person.setRole(parts[0].toLowerCase());
      person.setName(parts[1]);
    }
    return person;
  }

  private ValueContext getPeriod(final HocrDocument document) {
    return document.getAnnotations()
        .filterByType(AnnotatorPeriod.PERIOD)
        .topScore()
        .sortByLoc()
        .getFirstNomContext();
  }

  private ValueContext getDrawingDate(final HocrDocument document) {
    final AnnotationList annotations = document
        .getAnnotations()
        .filterByType(AnnotatorDrawingDate.DRAWING_DATE)
        .sortByPos();
    return annotations.getFirstNomContext();
  }

  private ValueContext getCompany(final HocrDocument document) {
    final ValueContext vc = document.getAnnotations()
        .filterByType(AnnotatorCompany.COMPANY)
        .topScore()
        .sortByLoc()
        .getFirstNomContext();
    vc.setValue(simpyLemmatize(vc.getValue().toUpperCase()));
    return vc;
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
    } catch (final Exception e) {
      return null;
    }
  }

}
