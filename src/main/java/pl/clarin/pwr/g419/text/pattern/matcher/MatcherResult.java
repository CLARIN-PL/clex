package pl.clarin.pwr.g419.text.pattern.matcher;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import lombok.Data;

@Data
public class MatcherResult {

  int length;
  Map<String, String> groups = Maps.newHashMap();

  public MatcherResult(final int length) {
    this.length = length;
  }

  public Optional<String> getGroupValue(final String name) {
    if (groups.containsKey(name)) {
      return Optional.of(groups.get(name));
    } else {
      return Optional.empty();
    }
  }

}
