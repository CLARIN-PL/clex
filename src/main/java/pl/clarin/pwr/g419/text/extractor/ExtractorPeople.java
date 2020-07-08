package pl.clarin.pwr.g419.text.extractor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.kbase.NeLexicon2;
import pl.clarin.pwr.g419.kbase.lexicon.FirstNameLexicon;
import pl.clarin.pwr.g419.kbase.lexicon.PersonNameLexicon;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPersonHorizontal;

import static pl.clarin.pwr.g419.text.annotator.AnnotatorPersonHorizontal.*;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherWordInSet;
import static pl.clarin.pwr.g419.utils.DateUtils.parseDate;

@Slf4j
public class ExtractorPeople implements IExtractor<List<FieldContext<Person>>> {

  PersonNameLexicon personNameLexicon = new PersonNameLexicon();

  public static NeLexicon2 neLexicon2 = NeLexicon2.get();
  public static FirstNameLexicon firstNameLexicon = new FirstNameLexicon();
  public static Set<String> firstNames;

  public final java.util.regex.Pattern patternLastName =
      java.util.regex.Pattern.compile("\\p{Lu}(\\p{Ll}|\\p{Lu})+(-\\p{Lu}(\\p{Ll}|\\p{Lu})+)?");


  public ExtractorPeople() {
    firstNames = Sets.newHashSet(neLexicon2.getNames(NeLexicon2.LIV_PERSON_FIRST));
    firstNames.addAll(firstNameLexicon);
    firstNames.addAll(firstNames.stream().map(String::toUpperCase).collect(Collectors.toList()));
  }

  @Override
  public Optional<List<FieldContext<Person>>> extract(final HocrDocument document) {
    log.info("getting people");
    return getPeople(document);
  }

  private Optional<List<FieldContext<Person>>> getPeople(final HocrDocument document) {
    // jeśli mamy stronę z podpisami to sprawdźmy czy z niej można coś sensowego wyciągnąć ...
    if (document.getDocContextInfo().getPageNrWithSigns() != 0) {
      final var resultForSignsPage = getPeopleForAnnotations(document.getAnnotationsForSignsPage());
      if (resultForSignsPage.size() > 0) {
        log.info("DOC ID :" + document.getId() + " Znaleziono stronę z podpisami i znaleziono na niej adnotacje");
        return Optional.of(resultForSignsPage);
      } else {
        log.info("DOC ID :" + document.getId() + " Znaleziono stronę z podpisami ale nie znaleziono na niej adnotacji");
        HocrPage signsPage = document.get(document.getDocContextInfo().getPageNrWithSigns() - 1);
        final var secondTryToExtractInfoFromSignsPage = tryHardToExtractPeopleInfoFromPage(signsPage);
        if (secondTryToExtractInfoFromSignsPage.size() > 0) {
          log.info(" strei >0 ");
          String str = secondTryToExtractInfoFromSignsPage.stream().map(c -> c.toString()).collect(Collectors.joining(""));
          log.info(" strei = " + str);
          return Optional.of(secondTryToExtractInfoFromSignsPage);
        } else {
          log.error("DOC ID :" + document.getId() + " Znaleziono stronę z podpisami i szukano w dodatkowy sposób ale nadal nie znaleziono na niej adnotacji dot. ludzi");
        }
      }
    }

    log.info("getting people 2");
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


  public List<FieldContext<Person>> tryHardToExtractPeopleInfoFromPage(HocrPage page) {
    // czy w ogóle są jakieś role wymienione na tej stronie po tej linii w której są podpisy

    log.info("getting people - sprawdzamy na stronie " + page.getNo());
    Set<String> roles = Set.of("prezes", "wiceprezes", "prokurent", "księgowa", "księgowy", "członek");
    List<Integer> detectedRoles = Lists.newArrayList();
    for (int i = 0; i < page.size(); i++) {
      String bbText = page.get(i).getLowNiceText();
      if (roles.contains(bbText))
        detectedRoles.add(i);
    }

    // < indeks do pola z rolą na stronie w Bboxach, adnotacja>
    List<Pair<Integer, Annotation>> roleAndAnnotationList = Lists.newArrayList();

    // czy na tej stronie są jakieś imiona albo nazwiska pod/nad tą/tymi linią/liniami gdzie są wykryte role
    for (int i = 0; i < detectedRoles.size(); i++) {
      int roleIndex = detectedRoles.get(i);
      List<Integer> above = page.findBBoxesAboveBBox(roleIndex, 1);
      List<Integer> below = page.findBBoxesBelowBBox(roleIndex, 1);

      List<Annotation> persons = Lists.newArrayList();
      persons.addAll(tryToFindPersonNearIndexes(above, page));
      persons.addAll(tryToFindPersonNearIndexes(below, page));

      mergeFoundAnnotationsOnPage(roleAndAnnotationList, persons, roleIndex, page);

      // zapamiętujemy znalezione adnotacje dot. osób i dla jakiej roli jest ta adnotacja
      persons.stream().forEach(ann -> roleAndAnnotationList.add(Pair.of(roleIndex, ann)));
    }

    return roleAndAnnotationList.stream().map(pair ->
    {
      Person person = strToPerson(pair.getValue().getNorm());
      person.setRole(page.get(pair.getKey()).getLowNiceText());
      FieldContext<Person> fc = new FieldContext<>(person, pair.getValue().getContext(), pair.getValue().getSource());
      return fc;
    }).collect(Collectors.toList());

  }


  public List<Annotation> tryToFindPersonNearIndexes(List<Integer> bboxesNearCandidates, HocrPage page) {
    List<Annotation> result = Lists.newArrayList();

    final String NAME = "name";
    final Pattern justPersonPattern = new Pattern("person-hor:date-name-role")
        .next(new MatcherWordInSet(firstNames).group(NAME))
        .next(new MatcherWordInSet(firstNames).group(NAME).optional())
        .next(new MatcherRegexText(patternLastName, 40).group(NAME));

    for (int k = 0; k < bboxesNearCandidates.size(); k++) {

      final int MAX_RANGE_BASE = 2;  // jednak 4 daje 193 false positiwy a 2 daje 187 false positivów. Może po crossvalidation bedzie lepiej
      for (int range_base = 0; range_base <= MAX_RANGE_BASE; range_base++) {

        int range_offset = (int) ((range_base + 1) / 2 * Math.signum(0.5 + (-1 * range_base % 2)));
        if ((bboxesNearCandidates.get(k) + range_offset < 0) || (bboxesNearCandidates.get(k) + range_offset >= page.size()))
          continue; // nie break bo z jednej strony wychodzi poza ale z drugiej może być jeszcze dobry

        Optional<PatternMatch> matching = justPersonPattern.matchesAt(page, bboxesNearCandidates.get(k) + range_offset);
        if (matching.isPresent()) {
          PatternMatch pm = matching.get();
          Annotation foundAnnotation = new Annotation("person-on-signs-page", page, pm.getIndexBegin(), pm.getIndexEnd())
              .withNorm(normalize(pm))
              .withScore(pm.getScore())
              .withSource(pm.getSource());

          mergeFoundAnnotationInLine(result, foundAnnotation);
          continue; // dajemy spokój po znalezieniu pierwszego - hmmm ?
        }
      }

    }
    return result;
  }

  private boolean mergeFoundAnnotationInLine(List<Annotation> result, Annotation newAnn) {
    // pre-prune result
    String annText = newAnn.getText();
    boolean addNewAnnotation = true; // jeśli tablica pusta to zawsze dodawaj
    for (int i = 0; i < result.size(); i++) {
      String chkText = result.get(i).getText();
      if (annText.contains(chkText)) {
        // znalezione dopasowanie jest "lepsze" niż jedno z już istniejących w tablicy
        log.info("dod/usu " + annText);
        addNewAnnotation = true;
        result.remove(result.get(i));
      } else if (chkText.contains(annText)) {
        // po prostu nic nie rób - mamy już co najmniej tak dobre dopasowanie
        log.info("skip " + annText);
        addNewAnnotation = false;
        break;
      } else {  // tego tu w sumie nawet chyba nie musi być ...
        log.info("dod " + annText);
        addNewAnnotation = true;
      }
    }
    if (addNewAnnotation) {
      result.add(newAnn);
    }
    return addNewAnnotation;
  }

  public void mergeFoundAnnotationsOnPage(List<Pair<Integer, Annotation>> roleAndAnnotationList,
                                          List<Annotation> newPersons,
                                          int newRoleIndex,
                                          HocrPage page) {
    // sprawdź czy czasem taki jeden nowy BBox z imieniem i nazwiskiem nie jest przez inne Bboxy z innymi rolami wskazywany ...
    for (int i = 0; i < newPersons.size(); i++) {
      Annotation newPerson = newPersons.get(i);
      log.info(" Cross checking :" + newPerson);

      for (int j = 0; j < roleAndAnnotationList.size(); j++) {
        Annotation against = roleAndAnnotationList.get(j).getValue();
        log.info("    against :" + against);

        // ... jeśli jest ...
        if (newPerson.getIndexBegin() == against.getIndexBegin()) {
          log.info(" CLASH!!! p1: " + newPerson + " p2: " + against);

          int centerXofPersonBbox = page.get(newPerson.getIndexBegin()).getBox().getCenterX();
          int centerXofNewRoleBbox = page.get(newRoleIndex).getBox().getCenterX();
          int centerXofOldRoleBbox = page.get(roleAndAnnotationList.get(j).getKey()).getBox().getCenterX();

          int distanceToNewRole = Math.abs(centerXofNewRoleBbox - centerXofPersonBbox);
          int distanceToOldRole = Math.abs(centerXofOldRoleBbox - centerXofPersonBbox);

          // ... to pozostaw tylko tego który jest bliżej
          if (distanceToOldRole <= distanceToNewRole) {
            // nie bierz już pod uwagę tego nowego
            newPersons.remove(i);
          } else {
            // usuń starą adnotację z wyników
            roleAndAnnotationList.remove(j);
          }
        }
      }
    }
  }


  protected String normalize(final PatternMatch pm) {
    return String.format("%s|%s|%s",
        pm.getGroupValue(DATE).orElse(""),
        pm.getGroupValue(TITLE).orElse(""),
        pm.getGroupValue(NAME).orElse(""));
  }


}
