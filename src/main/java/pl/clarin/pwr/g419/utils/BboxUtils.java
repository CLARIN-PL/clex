package pl.clarin.pwr.g419.utils;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.struct.Bbox;
import pl.clarin.pwr.g419.struct.Bboxes;
import pl.clarin.pwr.g419.struct.Range;

public class BboxUtils {

  public static List<Pair<Range, Integer>> createLines(final List<Bbox> page) {
    Optional<Range> lineRange = Optional.empty();
    final List<Pair<Range, Integer>> ranges = Lists.newArrayList();
    for (int i = 0; i < page.size(); i++) {
      final Bbox bbox = page.get(i);
      if (lineRange.isPresent()) {
        lineRange.get().merge(bbox.getBox().getTop(), bbox.getBox().getBottom());
      } else {
        lineRange = Optional.of(new Range(bbox.getBox().getTop(), bbox.getBox().getBottom()));
      }
      if (bbox.isLineEnd()) {
        ranges.add(new ImmutablePair<>(lineRange.get(), i));
        lineRange = Optional.empty();
      }
    }
    return ranges;
  }

  public static List<Pair<Range, Bboxes>> createLines2(final List<Bbox> page) {
    Optional<Range> lineRange = Optional.empty();
    Bboxes bboxes = new Bboxes();
    final List<Pair<Range, Bboxes>> ranges = Lists.newArrayList();
    for (int i = 0; i < page.size(); i++) {
      final Bbox bbox = page.get(i);
      if (lineRange.isPresent()) {
        lineRange.get().merge(bbox.getBox().getTop(), bbox.getBox().getBottom());
      } else {
        lineRange = Optional.of(new Range(bbox.getBox().getTop(), bbox.getBox().getBottom()));
      }
      bboxes.add(bbox);
      if (bbox.isLineEnd()) {
        ranges.add(new ImmutablePair<>(lineRange.get(), bboxes));
        lineRange = Optional.empty();
        bboxes = new Bboxes();
      }
    }
    return ranges;
  }
}
