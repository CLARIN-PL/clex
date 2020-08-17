package pl.clarin.pwr.g419.utils;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import pl.clarin.pwr.g419.struct.Bbox;
import pl.clarin.pwr.g419.struct.Box;

public class TestUtils {

  public static List<Bbox> getSequenceOfBboxes(final String sentence) {
    final List<Bbox> bboxes = Lists.newArrayList();
    Optional<Bbox> lastBbox = Optional.empty();
    for (final String word : sentence.split(" ")) {
      final Bbox bbox = wordToBbox(word);
      bboxes.add(bbox);
      lastBbox.ifPresent(last -> bbox.setLineBegin(last.isLineEnd()));
      lastBbox = Optional.of(bbox);
    }
    if (bboxes.size() > 0) {
      bboxes.get(0).setLineBegin(true);
      bboxes.get(bboxes.size() - 1).setLineEnd(true);
      bboxes.get(bboxes.size() - 1).setBlockEnd(true);
    }
    return bboxes;
  }

  public static Bbox wordToBbox(final String word) {
    final boolean isEnd = word.endsWith("|");
    final Box box = new Box(0, 0, 10, 10);
    final Bbox bbox = new Bbox(0, word.replaceAll("[|]", ""), box);
    bbox.setLineEnd(isEnd);
    bbox.setBlockEnd(isEnd);
    return bbox;
  }

}
