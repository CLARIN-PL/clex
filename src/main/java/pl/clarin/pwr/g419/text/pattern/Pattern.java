package pl.clarin.pwr.g419.text.pattern;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import pl.clarin.pwr.g419.struct.HocrPage;
import pl.clarin.pwr.g419.text.pattern.matcher.Matcher;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherResult;

public class Pattern {

  List<Matcher> matchers = Lists.newArrayList();

  public Pattern() {
  }

  public Pattern next(final Matcher matcher) {
    matchers.add(matcher);
    return this;
  }

  public Optional<Integer> matchesAt(final HocrPage page, final int index) {
    int i = index;
    for (final Matcher matcher : matchers) {
      final Optional<MatcherResult> length = matcher.matchesAt(page, i);
      if (length.isPresent()) {
        i += length.get().getLength();
      } else if (matcher.isOptional()) {
        // just ignore optional matcher
      } else {
        return Optional.empty();
      }
    }
    return Optional.of(i - index);
  }

  public List<PatternMatch> find(final HocrPage page) {
    int i = 0;
    final List<PatternMatch> matches = Lists.newArrayList();
    while (i < page.size()) {
      final Optional<Integer> length = matchesAt(page, i);
      if (length.isPresent()) {
        matches.add(new PatternMatch(i, i + length.get(), page));
        i += length.get();
      } else {
        i++;
      }
    }
    return matches;
  }

}
