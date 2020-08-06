package pl.clarin.pwr.g419.text.pattern.matcher;

import pl.clarin.pwr.g419.struct.HocrPage;
import java.util.Optional;
import java.util.Set;

public class MatcherTwoWordsSequenceInSet extends Matcher {

  Set<String> texts;

  public MatcherTwoWordsSequenceInSet(final String text) {
    this.texts = Set.of(text);
  }

  public MatcherTwoWordsSequenceInSet(final Set<String> texts) {
    this.texts = texts;
  }

  @Override
  public Optional<MatcherResult> matchesAt(final HocrPage page, final int index) {
    if (index < 0 || index >= page.size()) {
      return Optional.empty();
    }
    final String matchValue = page.get(index).getText();

    if (index + 1 < page.size()) {
      final String matchLongerValue = matchValue + " " + page.get(index + 1).getText();
      if (texts.contains(matchLongerValue)) {
        return Optional.of(postprocessMatcherResult(new MatcherResult(2), matchLongerValue));
      }
    }
    if (texts.contains(matchValue)) {
      return Optional.of(postprocessMatcherResult(new MatcherResult(1), matchValue));
    }
    return Optional.empty();
  }
}
