package pl.clarin.pwr.g419.text.pattern.matcher;

import java.util.Optional;
import java.util.Set;
import pl.clarin.pwr.g419.struct.HocrPage;

public class MatcherLowerText extends Matcher {

  Set<String> texts;

  public MatcherLowerText(final String text) {
    this.texts = Set.of(text);
  }

  public MatcherLowerText(final Set<String> texts) {
    this.texts = texts;
  }

  @Override
  public Optional<MatcherResult> matchesAt(final HocrPage page, final int index) {
    if (index < 0 || index >= page.size()) {
      return Optional.empty();
    }
    final String matchValue = page.get(index).getText().toLowerCase();
    if (texts.contains(page.get(index).getText().toLowerCase())) {
      return Optional.of(postprocessMatcherResult(new MatcherResult(1), matchValue));
    }
    return Optional.empty();
  }
}
