package pl.clarin.pwr.g419.text;

import com.google.common.collect.Lists;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.kbase.lexicon.CompanyLexicon;
import pl.clarin.pwr.g419.kbase.lexicon.PersonNameLexicon;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.*;
import pl.clarin.pwr.g419.text.lemmatizer.CompanyLemmatizer;
import pl.clarin.pwr.g419.text.normalization.NormalizerCompany;

@Log
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

    final Map<Integer, Set<Pair<Integer, Integer>>> documentHistogram = document.buildHistogramOfLinesHeightsForDocument();

    // wartość do wykorzystania przy znajdywaniu "dużych" linii ...
    final int mostCommonHeightOfLineInDocument = findMostCommonHeightOfLine(documentHistogram);

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

    //assignDefaultSignDate(metadata);

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
        .collect(Collectors.toMap(o -> personToFirstLastName(o.getField()), Function.identity(),
            (p1, p2) -> p1.getField().getName().length() > p2.getField().getName().length() ? p1 : p2))
        // take the one with longer name or latter occurance
        .values().stream().collect(Collectors.toList());
  }

  private String personToFirstLastName(final Person p) {
    final String[] parts = p.getName().toLowerCase().split(" ");
    if (parts.length == 1) {
      return parts[0];
    }
    return parts[0] + " " + parts[parts.length - 1];
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
    AnnotationList drawingDateCandidates = document.getAnnotations()
        .filterByType(AnnotatorDrawingDate.DRAWING_DATE);

    final AnnotationList firstPage = drawingDateCandidates.filterByPageNo(1);
    if (firstPage.size() > 0) {
      drawingDateCandidates = firstPage;
    }

    return drawingDateCandidates
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


  public int findMostCommonHeightOfLine(final Map<Integer, Set<Pair<Integer, Integer>>> histogram) {
    return histogram.entrySet().stream().max((entry1, entry2) -> entry1.getValue().size() > entry2.getValue().size() ? 1 : -1).get().getKey();
  }

  //------------------ diagnostyka -----------------------------------


  public void printHistogramOfLinesHeightsForDoc(final Map<Integer, Set<Pair<Integer, Integer>>> histogram) {
    final List<Integer> keys = new LinkedList<>(histogram.keySet());
    keys.sort((o1, o2) -> o1 < o2 ? -1 : 1);
    log.fine(" ---------- Lines Heights histogram for document ");

    keys.stream().forEach(key ->
        log.fine(" Key : " + key + "  counter: " + histogram.get(key).size() + " [" + histogram.get(key).stream().limit(5).map(v -> v.toString()).collect(Collectors.joining()) + " ]")
    );
  }

  public void printLinesWithGivenHeigth(final int height, final Set<Pair<Integer, Integer>> lines, final HocrDocument document) {
    lines.stream().forEach(pair ->
        {
          final HocrPage page = document.get(pair.getLeft() - 1); // !! W pair jest numer strony a nie indeks tablicy
          final List<Range> linesOfPage = page.getLines();
          final int pageNumber = pair.getRight();
          final String text = linesOfPage.get(pageNumber).getText();

          log.fine("Wysokość: " + height + " Strona: " + page.getNo() + " linia: " + (pair.getRight() + 1) + " : " + text);
        }
    );
  }

  public void printLinesWithHeightBiggerThanMostCommon(final Map<Integer, Set<Pair<Integer, Integer>>> histogram,
                                                       final HocrDocument document) {
    log.fine(" --- Document: " + document.getId());
    final int mostCommonHeightOfLine = findMostCommonHeightOfLine(histogram);

    final List<Integer> keys = new LinkedList<>(histogram.keySet());
    keys.sort((o1, o2) -> o1 < o2 ? -1 : 1);
    keys.stream().filter(n -> n > mostCommonHeightOfLine).forEach(key -> printLinesWithGivenHeigth(key, histogram.get(key), document));

  }


}
