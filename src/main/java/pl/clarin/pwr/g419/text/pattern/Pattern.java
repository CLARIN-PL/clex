package pl.clarin.pwr.g419.text.pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
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

  public Optional<PatternMatch> matchesAt(final HocrPage page, final int index) {
    int i = index;
    final Map<String, String> groups = Maps.newHashMap();
    for (final Matcher matcher : matchers) {
      final Optional<MatcherResult> length = matcher.matchesAt(page, i);
      if (length.isPresent()) {
        i += length.get().getLength();
        groups.putAll(length.get().getGroups());
      } else if (matcher.isOptional()) {
        // just ignore optional matcher
      } else {
        return Optional.empty();
      }
    }
    final PatternMatch pm = new PatternMatch(index, i, page, groups);
    return Optional.of(pm);
  }

  public List<PatternMatch> find(final HocrPage page) {
    int i = 0;
    final List<PatternMatch> matches = Lists.newArrayList();
    while (i < page.size()) {
      final Optional<PatternMatch> pm = matchesAt(page, i);
      if (pm.isPresent()) {
        matches.add(pm.get());
        i += pm.get().getLength();
      } else {
        i++;
      }
    }
    return matches;
  }

}
