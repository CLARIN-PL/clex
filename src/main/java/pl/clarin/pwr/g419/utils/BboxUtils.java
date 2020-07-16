package pl.clarin.pwr.g419.utils;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.struct.Bbox;
import pl.clarin.pwr.g419.struct.Bboxes;
import pl.clarin.pwr.g419.struct.HocrPage;
import pl.clarin.pwr.g419.struct.Range;

public class BboxUtils {

  public static List<Range> createLines(final HocrPage page) {
    final List<Range> ranges = Lists.newArrayList();
    Optional<Range> lineRange = Optional.empty();

    for (int i = 0; i < page.size(); i++) {
      final Bbox bbox = page.get(i);
      if (lineRange.isPresent()) {
        lineRange.get().merge(bbox.getBox().getTop(), bbox.getBox().getBottom());
        lineRange.get().setLastBoxInRangeIndex(i);
      } else {
        final Range range = new Range(page, bbox.getBox().getTop(), bbox.getBox().getBottom());
        range.setFirstBoxInRangeIndex(i);
        lineRange = Optional.of(range);
      }
      if (bbox.isLineEnd()) {
        final Range range = lineRange.get();
        range.setLastBoxInRangeIndex(i);
        ranges.add(range);
        lineRange = Optional.empty();
      }
    }
    return ranges;
  }

  public static List<Pair<Range, Bboxes>> createLines2(final HocrPage page) {
    Optional<Range> lineRange = Optional.empty();
    Bboxes bboxes = new Bboxes();
    final List<Pair<Range, Bboxes>> ranges = Lists.newArrayList();
    for (int i = 0; i < page.size(); i++) {
      final Bbox bbox = page.get(i);
      if (lineRange.isPresent()) {
        lineRange.get().merge(bbox.getBox().getTop(), bbox.getBox().getBottom());
      } else {
        lineRange = Optional.of(new Range(page, bbox.getBox().getTop(), bbox.getBox().getBottom()));
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
