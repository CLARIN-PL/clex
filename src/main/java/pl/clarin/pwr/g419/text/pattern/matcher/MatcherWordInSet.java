package pl.clarin.pwr.g419.text.pattern.matcher;

import java.util.Optional;
import java.util.Set;
import pl.clarin.pwr.g419.struct.HocrPage;

public class MatcherWordInSet extends Matcher {

  Set<String> texts;

  public MatcherWordInSet(final String text) {
    this.texts = Set.of(text);
  }

  public MatcherWordInSet(final Set<String> texts) {
    this.texts = texts;
  }

  @Override
  public Optional<MatcherResult> matchesAt(final HocrPage page, final int index) {
    if (index < 0 || index >= page.size()) {
      return Optional.empty();
    }
    final String matchValue = page.get(index).getText();
    if (texts.contains(matchValue)) {
      return Optional.of(postprocessMatcherResult(new MatcherResult(1), matchValue));
    }
    return Optional.empty();
  }
}
