package pl.clarin.pwr.g419.text.pattern.matcher;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import pl.clarin.pwr.g419.struct.HocrPage;

public class MatcherRegexText extends Matcher {

  Pattern pattern;
  int maxLength;

  public MatcherRegexText(final Pattern pattern, final int maxLength) {
    this.pattern = pattern;
    this.maxLength = maxLength;
  }

  @Override
  public Optional<MatcherResult> matchesAt(final HocrPage page, final int index) {
    final StringJoiner sj = new StringJoiner("");
    int n = index;
    while (sj.length() < maxLength && n < page.size()) {
      if (n >= 0 && n < page.size()) {
        sj.add(page.get(n++).getText());
      }
      if (pattern.matcher(sj.toString()).matches()) {
        return Optional.of(new MatcherResult(n - index));
      }
    }
    return Optional.empty();
  }
}
