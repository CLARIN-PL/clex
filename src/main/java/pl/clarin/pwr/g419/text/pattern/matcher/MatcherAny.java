package pl.clarin.pwr.g419.text.pattern.matcher;

import java.util.Optional;
import pl.clarin.pwr.g419.struct.HocrPage;

public class MatcherAny extends Matcher {

  @Override
  public Optional<MatcherResult> matchesAt(final HocrPage page, final int index) {
    if (index < 0 || index >= page.size()) {
      return Optional.empty();
    } else {
      return Optional.of(
          postprocessMatcherResult(new MatcherResult(1), page.get(index).getText()));
    }
  }
}
