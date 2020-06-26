package pl.clarin.pwr.g419.text.extractor;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.struct.Annotation;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.HocrPage;
import pl.clarin.pwr.g419.text.annotator.AnnotatorSignsPage;

@Slf4j
public class ExtractorSignsPage implements IExtractor<FieldContext<String>> {

  @Override
  public Optional<FieldContext<String>> extract(final HocrDocument document) {
    return getSignsPageWithAnnotator(document);
  }

  private Optional<FieldContext<String>> getSignsPageWithAnnotator(final HocrDocument document) {
    final Optional<FieldContext<String>> value = document.getAnnotations()
        .filterByType(AnnotatorSignsPage.SIGNS_PAGE)
        .topScore()
        .sortByLocDesc()
        .filter(ann -> isThisAnnotationWithSignsActually(ann))
        .getFirst();

    value.ifPresent(vc -> {
      vc.setField(String.valueOf(vc.getPage()));
      document.setPageNrWithSigns(vc.getPage());
    });

    return value;

  }


  private Optional<FieldContext<String>> getSignsPage(final HocrDocument document) {
    final List<Pair<Integer, Integer>> linesWithPodpisy = findLinesWithSigns(document);
    final Pair<Integer, Integer> lineWithSigns;

    if ((linesWithPodpisy == null) || (linesWithPodpisy.size() == 0)) {
      return Optional.empty();
    } else if (linesWithPodpisy.size() == 1) {
      lineWithSigns = linesWithPodpisy.get(0);
    } else {
      lineWithSigns = linesWithPodpisy.stream()
          .max((p1, p2) -> p1.getLeft() < p2.getLeft() ? -1 : 1).get();
    }

    final String line = document.getLineInPage(lineWithSigns.getRight(), lineWithSigns.getLeft() - 1);
    int pageNrWithSigns = 0;
    if (isThisLineWithSignsActually(line)) {
      pageNrWithSigns = lineWithSigns.getLeft();
    }
    document.setPageNrWithSigns(pageNrWithSigns);
    return Optional.of(new FieldContext<String>("" + pageNrWithSigns, "", null));
  }


  private boolean isThisAnnotationWithSignsActually(final Annotation ann) {
    return isThisLineWithSignsActually(ann.getWholeLineText());
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

    boolean result = true;
    if (hitsCounter >= rest.size()) {
      result = true;
    }

    if ((hitsCounter <= 1) && (rest.size() > 5)) {
      result = false;
    }

    return result;
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
