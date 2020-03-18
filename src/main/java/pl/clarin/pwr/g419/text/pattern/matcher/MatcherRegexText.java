package pl.clarin.pwr.g419.text.pattern.matcher;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import pl.clarin.pwr.g419.struct.HocrPage;

public class MatcherRegexText extends Matcher {

  Pattern pattern;
  int maxLength;
  Map<Integer, String> groupNames;
  private boolean lowerCase = false;

  public MatcherRegexText(final Pattern pattern, final int maxLength) {
    this(pattern, maxLength, Maps.newHashMap());

  }

  public MatcherRegexText(final Pattern pattern,
                          final int maxLength,
                          final Map<Integer, String> groupNames) {
    this.pattern = pattern;
    this.maxLength = maxLength;
    this.groupNames = groupNames;
  }

  public MatcherRegexText(final String pattern,
                          final int maxLength) {
    this(Pattern.compile(pattern), maxLength);
  }

  public MatcherRegexText(final String pattern,
                          final int maxLength,
                          final Map<Integer, String> groupNames) {
    this(Pattern.compile(pattern), maxLength, groupNames);
  }

  public MatcherRegexText lowerCase() {
    lowerCase = true;
    return this;
  }

  @Override
  public Optional<MatcherResult> matchesAt(final HocrPage page, final int index) {
    final StringJoiner sj = new StringJoiner("");
    int n = index;
    while (sj.length() < maxLength && n < page.size()) {
      if (n >= 0 && n < page.size()) {
        sj.add(page.get(n++).getText());
      }
      String text = sj.toString();
      if (lowerCase) {
        text = text.toLowerCase();
      }
      final java.util.regex.Matcher m = pattern.matcher(text);
      if (m.matches()) {
        final MatcherResult mr = new MatcherResult(n - index);
        for (final Map.Entry<Integer, String> entry : groupNames.entrySet()) {
          mr.getGroups().put(entry.getValue(), m.group(entry.getKey()));
        }
        return Optional.of(postprocessMatcherResult(mr, m.group()));
      }
    }
    return Optional.empty();
  }
}
