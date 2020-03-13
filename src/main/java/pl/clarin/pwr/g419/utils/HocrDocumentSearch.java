package pl.clarin.pwr.g419.utils;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import pl.clarin.pwr.g419.struct.Bboxes;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.HocrPage;

public class HocrDocumentSearch {

  private final HocrDocument document;
  private final TextComparator comparator = new TextComparator(0.9);

  public HocrDocumentSearch(final HocrDocument document) {
    this.document = document;
  }

  public List<Bboxes> search(final String phrase) {
    return this.document.stream()
        .map(page -> search(phrase, page))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private List<Bboxes> search(final String phrase, final HocrPage page) {
    final List<Bboxes> regions = Lists.newArrayList();
    for (int i = 0; i < page.size(); i++) {
      final Bboxes region = new Bboxes();
      int j = i;
      boolean matched = false;
      do {
        region.add(page.get(j++));
        matched = comparator.equals(region.getText(), phrase);
        if (matched) {
          regions.add(region);
          i = j;
        }
      } while (j < page.size() && !matched
          && region.getText().length() < phrase.length() * 2);
    }
    return regions;
  }
}
