package pl.clarin.pwr.g419.text.extractor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.kbase.NeLexicon2;
import pl.clarin.pwr.g419.kbase.lexicon.FirstNameLexicon;
import pl.clarin.pwr.g419.kbase.lexicon.PersonNameLexicon;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPersonHorizontal;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherWordInSet;
import pl.clarin.pwr.g419.utils.TextUtils;

import static pl.clarin.pwr.g419.text.annotator.AnnotatorPersonHorizontal.*;
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
    return getPeople(document);
  }

  private Optional<List<FieldContext<Person>>> getPeople(final HocrDocument document) {
    // jeśli mamy stronę z podpisami to sprawdźmy czy z niej można coś sensowego wyciągnąć ...
    if (document.getDocContextInfo().getPageNrWithSigns() != 0) {
      final var resultForSignsPage = getPeopleForAnnotations(document.getAnnotationsForSignsPage());
      if (resultForSignsPage.size() > 0) {
        log.debug("DOC ID :" + document.getId() + " Znaleziono stronę z podpisami i znaleziono na niej adnotacje");
        return Optional.of(resultForSignsPage);
      } else {
        log.debug("DOC ID :" + document.getId() + " Znaleziono stronę z podpisami ale nie znaleziono na niej adnotacji");
        final HocrPage signsPage = document.get(document.getDocContextInfo().getPageNrWithSigns() - 1);
        final var secondTryToExtractInfoFromSignsPage = tryHardToExtractPeopleInfoFromPage(signsPage);
        if (secondTryToExtractInfoFromSignsPage.size() > 0) {
          final String str = secondTryToExtractInfoFromSignsPage.stream().map(c -> c.toString()).collect(Collectors.joining(""));
          return Optional.of(secondTryToExtractInfoFromSignsPage);
        } else {
          log.warn("DOC ID :" + document.getId() + " Znaleziono stronę z podpisami i szukano w dodatkowy sposób ale nadal nie znaleziono na niej adnotacji dot. ludzi");
        }
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
      person.setRole(TextUtils.toTitleCase(parts[1]));
    }
    if (parts.length > 2) {
      final String name = parts[2]
          .replaceAll("[ ]*-[ ]*", "-");
      person.setName(personNameLexicon.approximate(name));
    }
    return person;
  }


  public List<FieldContext<Person>> tryHardToExtractPeopleInfoFromPage(final HocrPage page) {
    // czy w ogóle są jakieś role wymienione na tej stronie (może dodać, że po tej linii w której są "Podpisy" ale to nie zawsze tak jest  )
    final Set<String> roles = Set.of("prezes", "wiceprezes", "prokurent", "księgowa", "księgowy", "członek");
    final List<Integer> detectedRoles = Lists.newArrayList();
    for (int i = 0; i < page.size(); i++) {
      final String bbText = page.get(i).getLowNiceText();
      if (roles.contains(bbText)) {
        detectedRoles.add(i);
      }
    }

    // < indeks do pola z rolą na stronie w Bboxach, adnotacja>
    final List<Pair<Integer, Annotation>> roleAndAnnotationList = Lists.newArrayList();

    // czy na tej stronie są jakieś imiona albo nazwiska pod/nad tą/tymi linią/liniami gdzie są wykryte role
    for (int i = 0; i < detectedRoles.size(); i++) {
      final int roleIndex = detectedRoles.get(i);
      log.debug("  ");
      log.debug(" --- Szukamy dla roli : " + page.get(roleIndex).getText() + "(" + roleIndex + ")  ---");
      // znajdź BBoxy z jakąś treścią powyżej i poniżej BBoxa z rolą
      final List<Integer> above = page.findBBoxesAboveBBox(roleIndex, 1);
      final List<Integer> below = page.findBBoxesBelowBBox(roleIndex, 1);

      // spróbuj z tych znalezionych Bboxów lub ich bliskiego sąsiedztwa wyciągnąć osoby
      // i zapisz je do listy persons
      final List<Annotation> persons = Lists.newArrayList();
      persons.addAll(tryToFindPersonNearIndexes(above, page));
      persons.addAll(tryToFindPersonNearIndexes(below, page));

      // jeśli te same osoby są przyporządkowane do różnych ról tu (persons)
      // i wśród wcześniej znalezionych (roleAndAnnotationList) to rozstrzygnij konflikty
      // i dodaj wyniki do rezultatu (roleAndAnnotationList)
      mergeFoundAnnotationsOnPage(roleAndAnnotationList, persons, roleIndex, page);
    }

    // jak się okazuje są dokumenty które na stronie zpodpisami mają dwie serie tych podpisów i to takich samych
    // (301256) i tu najprostszym sposobem to obchodzimy po prostu przekazujemy dalej tylko unikalne
    final Set<FieldContext<Person>> tmp = roleAndAnnotationList.stream().map(pair ->
    {
      Person person = strToPerson(pair.getValue().getNorm());
      person.setRole(page.get(pair.getKey()).getLowNiceText());
      FieldContext<Person> fc = new FieldContext<>(person, pair.getValue().getContext(), pair.getValue().getSource());
      return fc;
    }).collect(Collectors.toSet());

    return tmp.stream().collect(Collectors.toList());

  }


  public List<Annotation> tryToFindPersonNearIndexes(final List<Integer> bboxesNearCandidates, final HocrPage page) {
    final List<Annotation> result = Lists.newArrayList();

    for (int k = 0; k < bboxesNearCandidates.size(); k++) {

      // maksymalny zakres jak bardzo "w bok" sięgamy od wskazanego pola by próbować znaleźć osobę
      final int MAX_RANGE = 1;
      for (int range = 0; range <= MAX_RANGE; range++) {
        final int newIndex = bboxesNearCandidates.get(k) + range;
        if (newIndex >= page.size()) {
          break;
        }

        findMatchAndMergeResultInLine(page, result, newIndex);
      }

      for (int range = -1; range <= -MAX_RANGE; range--) {
        final int newIndex = bboxesNearCandidates.get(k) + range;
        if (newIndex < 0) {
          break;
        }
        if ((page.get(newIndex).isBlockEnd()) || (page.get(newIndex).isLineEnd())) {
          break; // tu próbujemy wykorzystać info na temat czy nie cofamy się do innego bloku
        }

        findMatchAndMergeResultInLine(page, result, newIndex);
      }
    }
    return result;
  }

  private void findMatchAndMergeResultInLine(final HocrPage page, final List<Annotation> result, final int newIndex) {

    final String NAME = "name";
    final Pattern justPersonPattern = new Pattern("person-hor:date-name-role")
        .next(new MatcherWordInSet(firstNames).group(NAME))
        .next(new MatcherWordInSet(firstNames).group(NAME).optional())
        .next(new MatcherRegexText(patternLastName, 40).group(NAME));

    final Optional<PatternMatch> matching = justPersonPattern.matchesAt(page, newIndex);
    if (matching.isPresent()) {
      final PatternMatch pm = matching.get();
      final Annotation foundAnnotation = new Annotation("person-on-signs-page", page, pm.getIndexBegin(), pm.getIndexEnd())
          .withNorm(normalize(pm))
          .withScore(pm.getScore())
          .withSource(pm.getSource());

      mergeFoundAnnotationInLine(result, foundAnnotation);
    }
  }

  private boolean mergeFoundAnnotationInLine(final List<Annotation> result, final Annotation newAnn) {
    // pre-prune result
    final String annText = newAnn.getText();
    boolean addNewAnnotation = true; // jeśli tablica pusta to zawsze dodawaj
    for (int i = 0; i < result.size(); i++) {
      final String chkText = result.get(i).getText();
      if (annText.contains(chkText)) {
        // znalezione dopasowanie jest "lepsze" niż jedno z już istniejących w tablicy
        log.debug("dod/usu " + annText);
        addNewAnnotation = true;
        result.remove(result.get(i));
      } else if (chkText.contains(annText)) {
        // po prostu nic nie rób - mamy już co najmniej tak dobre dopasowanie
        log.debug("skip " + annText);
        addNewAnnotation = false;
        break;
      } else {  // tego tu w sumie nawet chyba nie musi być ...
        log.debug("dod " + annText);
        addNewAnnotation = true;
      }
    }
    if (addNewAnnotation) {
      result.add(newAnn);
    }
    return addNewAnnotation;
  }

  public void mergeFoundAnnotationsOnPage(final List<Pair<Integer, Annotation>> roleAndAnnotationList,
                                          final List<Annotation> newPersons,
                                          final int newRoleIndex,
                                          final HocrPage page) {
    log.debug(" --> Adding :" + newPersons);
    // sprawdź czy czasem taki jeden nowy BBox z imieniem i nazwiskiem nie jest przez inne Bboxy z innymi rolami wskazywany ...
    for (int i = 0; i < newPersons.size(); i++) {
      final Annotation newPerson = newPersons.get(i);
      log.debug("---> Sprawdzamy :" + newPerson);

      for (int j = 0; j < roleAndAnnotationList.size(); j++) {
        final Annotation against = roleAndAnnotationList.get(j).getValue();
        log.debug("    względem :" + against);

        // ... jeśli jest ...
        if (newPerson.getIndexBegin() == against.getIndexBegin()) {
          log.debug("       Konflikt! p1: " + newPerson + " p2: " + against);

          // (porównywanie wg odległości standardowej)
          final Contour personBbox = newPerson.getContour();
          final Bbox newRoleBbox = page.get(newRoleIndex);
          final Bbox oldRoleBbox = page.get(roleAndAnnotationList.get(j).getKey());

          final int distanceSqrToNewRole = personBbox.distanceSqrTo(newRoleBbox);
          final int distanceSqrToOldRole = personBbox.distanceSqrTo(oldRoleBbox);

          // ... to pozostaw tylko tego który jest bliżej :
          if (distanceSqrToOldRole < distanceSqrToNewRole) {
            // nie bierz już pod uwagę tego nowego a zostaw stary
            newPersons.remove(i);
            i--; // skoro skasowaliśmy ten element to cofamy licznik o jeden
          } else if (distanceSqrToOldRole > distanceSqrToNewRole) {
            // usuń starą adnotację z wyników i będzie wstawiona nowa
            roleAndAnnotationList.remove(j);
          }
        }
      }
    }

    log.debug("Ostatecznie dodajemy z tego " + newPersons);

    // zapamiętujemy znalezione i "oczyszczone" adnotacje dot. osób i dla jakiej roli jest ta adnotacja
    newPersons.stream().forEach(ann -> roleAndAnnotationList.add(Pair.of(newRoleIndex, ann)));
  }


  protected String normalize(final PatternMatch pm) {
    return String.format("%s|%s|%s",
        pm.getGroupValue(DATE).orElse(""),
        pm.getGroupValue(TITLE).orElse(""),
        pm.getGroupValue(NAME).orElse(""));
  }


}
