package pl.clarin.pwr.g419.utils;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.struct.Bbox;
import pl.clarin.pwr.g419.struct.Bboxes;
import pl.clarin.pwr.g419.struct.HocrLine;
import pl.clarin.pwr.g419.struct.HocrPage;

public class BboxUtils {

  public static List<HocrLine> createLines(final HocrPage page) {
    final List<HocrLine> hocrLines = Lists.newArrayList();
    Optional<HocrLine> lineRange = Optional.empty();

    for (int i = 0; i < page.size(); i++) {
      final Bbox bbox = page.get(i);
      if (lineRange.isPresent()) {
        lineRange.get().merge(bbox.getBox().getTop(), bbox.getBox().getBottom());
        lineRange.get().setLastBoxInRangeIndex(i);
      } else {
        final HocrLine hocrLine = new HocrLine(page, bbox.getBox().getTop(), bbox.getBox().getBottom());
        hocrLine.setFirstBoxInRangeIndex(i);
        lineRange = Optional.of(hocrLine);
      }
      if (bbox.isLineEnd()) {
        final HocrLine hocrLine = lineRange.get();
        hocrLine.setLastBoxInRangeIndex(i);
        hocrLines.add(hocrLine);
        lineRange = Optional.empty();
      }
    }
    return hocrLines;
  }

  public static List<Pair<HocrLine, Bboxes>> createLines2(final HocrPage page) {
    Optional<HocrLine> lineRange = Optional.empty();
    Bboxes bboxes = new Bboxes();
    final List<Pair<HocrLine, Bboxes>> ranges = Lists.newArrayList();
    for (int i = 0; i < page.size(); i++) {
      final Bbox bbox = page.get(i);
      if (lineRange.isPresent()) {
        lineRange.get().merge(bbox.getBox().getTop(), bbox.getBox().getBottom());
      } else {
        lineRange = Optional.of(new HocrLine(page, bbox.getBox().getTop(), bbox.getBox().getBottom()));
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
