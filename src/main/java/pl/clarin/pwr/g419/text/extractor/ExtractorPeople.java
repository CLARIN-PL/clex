package pl.clarin.pwr.g419.text.extractor;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import pl.clarin.pwr.g419.kbase.lexicon.PersonNameLexicon;
import pl.clarin.pwr.g419.struct.AnnotationList;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.Person;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPersonHorizontal;

import static pl.clarin.pwr.g419.utils.DateUtils.parseDate;

public class ExtractorPeople implements IExtractor<List<FieldContext<Person>>> {

  PersonNameLexicon personNameLexicon = new PersonNameLexicon();


  @Override
  public Optional<List<FieldContext<Person>>> extract(final HocrDocument document) {
    return getPeople(document);
  }

  private Optional<List<FieldContext<Person>>> getPeople(final HocrDocument document) {
    // jeśli mamy stronę z podpisami to sprawdźmy czy tylko z niej można coś sensowego wyciągnąć ...
    if (document.getPageNrWithSigns() != 0) {
      final var resultForSignsPage = getPeopleForAnnotations(document.getAnnotationsForSignsPage());
      if (resultForSignsPage.size() > 0) {
        return Optional.of(resultForSignsPage);
      }
    }
    // .. jeśli nie można to procesujemy standardowo
    final List<FieldContext<Person>> result = getPeopleForAnnotations(document.getAnnotations());
    if (result.size() == 0) {
      return Optional.empty();
    }

    return Optional.of(result);
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


}
