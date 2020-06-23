package pl.clarin.pwr.g419.text;

import com.google.common.collect.Lists;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.kbase.lexicon.CompanyLexicon;
import pl.clarin.pwr.g419.kbase.lexicon.PersonNameLexicon;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.*;
import pl.clarin.pwr.g419.text.lemmatizer.CompanyLemmatizer;
import pl.clarin.pwr.g419.text.normalization.NormalizerCompany;

@Slf4j
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

    final FieldContext<String> signsPageNr = getSignsPage(document);

    metadata.setSignsPage(signsPageNr);

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
    // jeśli mamy stronę z podpisami to sprawdźmy czy tylko z niej można coś sensowego wyciągnąć ...
    if (document.getPageNrWithSigns() != 0) {
      final var resultForSignsPage = getPeopleForAnnotations(document.getAnnotationsForSignsPage());
      if (resultForSignsPage.size() > 0) {
        return resultForSignsPage;
      }
    }
    // .. jeśli nie można to prcoesujemy standardowo
    return getPeopleForAnnotations(document.getAnnotations());
  }

  private List<FieldContext<Person>> getPeopleForAnnotations(final AnnotationList annotations) {
    return annotations
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

  private FieldContext<String> getSignsPage(final HocrDocument document) {
    final List<Pair<Integer, Integer>> linesWithPodpisy = findLinesWithSigns(document);
    int pageNrWithSigns = 0;
    final Pair<Integer, Integer> lineWithSigns;

    if ((linesWithPodpisy == null) || (linesWithPodpisy.size() == 0)) {
      return new FieldContext<String>("0", "", null);

    } else if (linesWithPodpisy.size() == 1) {

      lineWithSigns = linesWithPodpisy.get(0);
    } else {
      lineWithSigns = linesWithPodpisy.stream()
          .max((p1, p2) -> p1.getLeft() < p2.getLeft() ? -1 : 1).get();
    }

    final String line = document.getLineInPage(lineWithSigns.getRight(), lineWithSigns.getLeft() - 1);


    if (isThisLineWithSignsActually(line)) {
      pageNrWithSigns = lineWithSigns.getLeft();
    }

    document.setPageNrWithSigns(pageNrWithSigns);


    return new FieldContext<String>("" + pageNrWithSigns, "", null);

  }

  private boolean isThisLineWithSignsActually(final String line) {
    final String[] strWords = line.trim().toLowerCase().split("[ :.,']");
    final Set<String> words = new HashSet();
    for (final String s : strWords) {
      if (s.length() > 0) {
        words.add(s);
      }
    }

    final Set<String> highlightWords = Set.of("podpisy", "wszystkich", "członków", "zarządu",
        "osób", "odpowiedzialnych", "reprezentujących");
    final Set<String> skipWords = Set.of("s", "a", "grupy", "kapitałowej", "data", "wchodzących", "skład");

    final Set<String> rest = new HashSet<>();

    int hitsCounter = 0;
    for (final String w : words) {
      if (highlightWords.contains(w)) {
        hitsCounter++;
      } else {
        if (!skipWords.contains(w)) {
          if (w.length() > 1) {
            // jeszcze może odfiltrowywać słowa będące mieszanką cyfr o liter
            rest.add(w);
          }
        }
      }
    }

    if (hitsCounter >= rest.size()) {
      return true;
    }

    if ((hitsCounter == 1) && (rest.size() > 5)) {
      return false;
    }

    return true;
  }


  private Date parseDate(final String str) {
    try {
      return new SimpleDateFormat("yyyy-MM-dd").parse(str);
    } catch (final Exception e) {
      return null;
    }
  }

  public List<Pair<Integer, Integer>> findLinesWithSigns(final HocrDocument document) {
    final List<Pair<Integer, Integer>> result = new ArrayList<>();

    for (int pageIndex = 0; pageIndex < document.size(); pageIndex++) {
      final HocrPage page = document.get(pageIndex);
      for (int lineIndex = 0; lineIndex < page.getLines().size(); lineIndex++) {
        final String line = page.getLines().get(lineIndex).getText();
        if (line.matches("(?i).*\\bPodpisy\\b.*")) {
          result.add(Pair.of(pageIndex + 1, lineIndex));
        }
      }
    }
    return result;
  }

}
