package pl.clarin.pwr.g419.struct;

import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

@Data
public class HocrDocument extends ArrayList<HocrPage> {

  String id;

  Metadata metadata = new Metadata();

  public AnnotationList getAnnotations() {
    return new AnnotationList(this.stream().map(HocrPage::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList()));
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

    return documentHistogram;
  }


}
