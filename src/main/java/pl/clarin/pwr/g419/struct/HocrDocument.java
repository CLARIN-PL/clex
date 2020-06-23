package pl.clarin.pwr.g419.struct;

import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

@Data
public class HocrDocument extends ArrayList<HocrPage> {

  String id;

  Metadata metadata = new Metadata();
  Map<Integer, Set<Pair<Integer, Integer>>> histogram;
  int mostCommonHeightOfLine;
  int pageNrWithSigns = 0;


  public AnnotationList getAnnotations() {
    return new AnnotationList(this.stream().map(HocrPage::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList()));
  }

  public AnnotationList getAnnotationsForPeople() {
    if (pageNrWithSigns == 0) {
      return new AnnotationList(this.stream()
          .map(HocrPage::getAnnotations)
          .flatMap(Collection::stream).collect(Collectors.toList()));
    } else {
      return new AnnotationList(this.stream()
          .filter(page -> page.getNo() == pageNrWithSigns)
          .map(HocrPage::getAnnotations)
          .flatMap(Collection::stream).collect(Collectors.toList()));
    }
  }


  public Map<Integer, Set<Pair<Integer, Integer>>> buildHistogramOfLinesHeightsForDocument() {

    final Map<Integer, Set<Pair<Integer, Integer>>> documentHistogram = new HashMap<>();

    this.stream()
        .forEach(page ->
        {
          final Map<Integer, Set<Pair<Integer, Integer>>> pageHistogram = page.buildHistogramOfLinesHeightsForPage();
          pageHistogram.forEach((k, v) -> documentHistogram.merge(k, v, (v1, v2) -> {
            v2.addAll(v1);
            return v2;
          }));
        });

    this.histogram = documentHistogram;
    return histogram;
  }

  public String getLineInPage(final int line, final int page) {
    return this.get(page).getLines().get(line).getText();
  }


}
