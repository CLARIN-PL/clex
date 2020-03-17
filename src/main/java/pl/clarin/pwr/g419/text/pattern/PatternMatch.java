package pl.clarin.pwr.g419.text.pattern;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Data;
import pl.clarin.pwr.g419.struct.Bbox;
import pl.clarin.pwr.g419.struct.HocrPage;

@Data
public class PatternMatch {
  int indexBegin;
  int indexEnd;
  HocrPage page;
  Map<String, String> groups = Maps.newHashMap();

  public PatternMatch(final int begin, final int end,
                      final HocrPage page, final Map<String, String> groups) {
    this.indexBegin = begin;
    this.indexEnd = end;
    this.page = page;
    this.groups = groups;
  }

  public String getText() {
    return IntStream.range(indexBegin, indexEnd)
        .mapToObj(page::get)
        .map(Bbox::getText)
        .collect(Collectors.joining(" "));
  }

  public String toString() {
    return String.format("[%4d:%4d] = %s", indexBegin, indexEnd, getText());
  }

  public int getLength() {
    return indexEnd - indexBegin;
  }

  public Optional<String> getGroupValue(final String name) {
    if (groups.containsKey(name)) {
      return Optional.of(groups.get(name));
    } else {
      return Optional.empty();
    }
  }
}
