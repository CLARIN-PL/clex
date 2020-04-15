package pl.clarin.pwr.g419.text;

import com.google.common.collect.Lists;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.kbase.lexicon.CompanyLexicon;
import pl.clarin.pwr.g419.kbase.lexicon.PersonNameLexicon;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.MetadataWithContext;
import pl.clarin.pwr.g419.struct.Person;
import pl.clarin.pwr.g419.text.annotator.*;
import pl.clarin.pwr.g419.text.lemmatizer.CompanyLemmatizer;
import pl.clarin.pwr.g419.text.normalization.NormalizerCompany;

public class InformationExtractor implements HasLogger {

  List<Annotator> annotators = Lists.newArrayList(
      new AnnotatorDate(),
      new AnnotatorPeriod(),
      new AnnotatorCompanyPrefix(),
      new AnnotatorCompanySuffix(),
      new AnnotatorCompany(),
      new AnnotatorRole(),
      new AnnotatorPersonHorizontal(),
      new AnnotatorPersonVertical(),
      new AnnotatorDrawingDate()
  );

  CompanyLexicon companyLexicon = new CompanyLexicon();
  NormalizerCompany companyNormalizer = new NormalizerCompany();
  CompanyLemmatizer companyLemmatizer = new CompanyLemmatizer();
  PersonNameLexicon personNameLexicon = new PersonNameLexicon();

  public MetadataWithContext extract(final HocrDocument document) {
    document.stream()
        .forEach(page -> annotators.forEach(an -> an.annotate(page)));

    final MetadataWithContext metadata = new MetadataWithContext();

    getPeriod(document).ifPresent(p -> {
      metadata.setPeriodFrom(p.getLeft());
      metadata.setPeriodTo(p.getRight());
    });

    getDrawingDate(document).ifPresent(metadata::setDrawingDate);
    getCompany(document).ifPresent(metadata::setCompany);

    metadata.setPeople(getPoeple(document));

    assignDefaultSignDate(metadata);

    return metadata;
  }

  private void assignDefaultSignDate(final MetadataWithContext metadata) {
    if (metadata.getDrawingDate() != null) {
      metadata.getPeople().stream()
          .filter(f -> f.getField().getDate() == null)
          .forEach(f -> {
            f.getField().setDate(metadata.getDrawingDate().getField());
            f.setRule(f.getRule() + "; date=drawing_date");
          });
    }
  }

  private List<FieldContext<Person>> getPoeple(final HocrDocument document) {
    return document.getAnnotations()
        .filterByType(AnnotatorPersonHorizontal.PERSON)
        .removeNested()
        .sortByLoc()
        .stream()
        .map(an -> new FieldContext<>(strToPerson(an.getNorm()), an.getContext(), an.getSource()))
        .collect(Collectors.toMap(o -> o.getField().getName(), Function.identity(),
            (p1, p2) -> p2)) // take the last occurance
        .values().stream().collect(Collectors.toList());
  }

  private Person strToPerson(final String str) {
    final Person person = new Person();
    final String[] parts = str.split("[|]");
    if (parts.length > 0) {
      person.setDate(parseDate(parts[0]));
    }
    if (parts.length > 1) {
      person.setRole(parts[1].toLowerCase());
    }
    if (parts.length > 2) {
      final String name = parts[2]
          .replaceAll("[ ]*-[ ]*", "-");
      person.setName(personNameLexicon.approximate(name));
    }
    return person;
  }

  private Optional<Pair<FieldContext<Date>, FieldContext<Date>>> getPeriod(
      final HocrDocument document) {
    final Optional<FieldContext<String>> period = document.getAnnotations()
        .filterByType(AnnotatorPeriod.PERIOD)
        .topScore().sortByLoc().getFirst();

    if (period.isPresent()) {
      final String[] parts = period.get().getField().split(":");
      if (parts.length == 2) {
        final FieldContext<Date> periodStart = new FieldContext<>(
            parseDate(parts[0]), period.get().getContext(), period.get().getRule()
        );
        final FieldContext<Date> periodEnd = new FieldContext<>(
            parseDate(parts[1]), period.get().getContext(), period.get().getRule()
        );
        return Optional.of(new ImmutablePair<>(periodStart, periodEnd));
      }
    }
    return Optional.empty();
  }

  private Optional<FieldContext<Date>> getDrawingDate(final HocrDocument document) {
    return document
        .getAnnotations()
        .filterByType(AnnotatorDrawingDate.DRAWING_DATE)
        .topScore()
        .sortByPos()
        .getFirst()
        .map(vc -> new FieldContext<>(parseDate(vc.getField()), vc.getContext(), vc.getRule()));
  }

  private Optional<FieldContext<String>> getCompany(final HocrDocument document) {
    final Optional<FieldContext<String>> value = document.getAnnotations()
        .filterByType(AnnotatorCompany.COMPANY)
        .topScore()
        .sortByLoc()
        .getFirst();
    value.ifPresent(vc -> {
      final String nameLem = companyLemmatizer.lemmatize(vc.getField().toUpperCase());
      final String nameNorm = companyNormalizer.normalize(nameLem);
      final String nameApprox = companyLexicon.approximate(nameNorm);
      if (!nameNorm.equals(nameApprox)) {
        vc.setField(nameApprox);
        vc.setRule(String.format("%s; %s -> %s", vc.getRule(), nameNorm, nameApprox));
      } else {
        vc.setField(nameLem);
      }
    });
    return value;
  }

  private Date parseDate(final String str) {
    try {
      return new SimpleDateFormat("yyyy-MM-dd").parse(str);
    } catch (final Exception e) {
      return null;
    }
  }

}
