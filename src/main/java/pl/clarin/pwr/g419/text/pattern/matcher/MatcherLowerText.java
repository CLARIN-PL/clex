package pl.clarin.pwr.g419.text.pattern.matcher;

import java.util.Optional;
import java.util.Set;
import pl.clarin.pwr.g419.struct.HocrPage;

public class MatcherLowerText extends Matcher {

  Set<String> texts;

  public MatcherLowerText(final Set<String> texts) {
    this.texts = texts;
  }

  @Override
  public Optional<Integer> matchesAt(final HocrPage page, final int index) {
    if (index < 0 || index >= page.size()) {
      return Optional.empty();
    }
    if (texts.contains(page.get(index).getText().toLowerCase())) {
      return Optional.of(1);
    }
    return Optional.empty();
  }
}
