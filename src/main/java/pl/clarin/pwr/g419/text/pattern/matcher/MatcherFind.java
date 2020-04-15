package pl.clarin.pwr.g419.text.pattern.matcher;

import java.util.Optional;
import pl.clarin.pwr.g419.struct.HocrPage;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;

public class MatcherFind extends Matcher {

  Pattern pattern;
  int maxDistance;

  public MatcherFind(final Pattern pattern, final int maxDistance) {
    this.pattern = pattern;
    this.maxDistance = maxDistance;
  }

  @Override
  public Optional<MatcherResult> matchesAt(final HocrPage page, final int index) {
    if (index < 0 || index >= page.size()) {
      return Optional.empty();
    } else {
      for (int n = 0; n < maxDistance; n++) {
        final Optional<PatternMatch> m = pattern.matchesAt(page, index + n);
        if (m.isPresent()) {
          final MatcherResult mr = new MatcherResult(n + 1);
          mr.groups.putAll(m.get().getGroups());
          return Optional.of(postprocessMatcherResult(mr, page.get(index).getText()));
        }
      }
    }
    return Optional.empty();
  }
}
