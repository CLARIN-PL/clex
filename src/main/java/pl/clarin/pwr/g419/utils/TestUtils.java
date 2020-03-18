package pl.clarin.pwr.g419.utils;

import java.util.List;
import java.util.stream.Collectors;
import pl.clarin.pwr.g419.struct.Bbox;
import pl.clarin.pwr.g419.struct.Box;

public class TestUtils {

  public static List<Bbox> getSequenceOfBboxes(final List<String> words) {
    final Box box = new Box(0, 0, 10, 10);
    return words.stream().map(w -> new Bbox(0, w, box)).collect(Collectors.toList());
  }

}
