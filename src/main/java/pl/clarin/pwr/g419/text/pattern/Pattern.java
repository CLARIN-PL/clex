package pl.clarin.pwr.g419.text.pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import pl.clarin.pwr.g419.struct.Bbox;
import pl.clarin.pwr.g419.struct.HocrPage;
import pl.clarin.pwr.g419.text.pattern.matcher.Matcher;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherResult;

public class Pattern {

  boolean matchLine = false;
  boolean singleLine = false;
  int score = 100;
  List<Matcher> matchers = Lists.newArrayList();
  String name = "";

  public Pattern() {
  }

  public Pattern(final String name) {
    this.name = name;
  }

  public Pattern matchLine() {
    matchLine = true;
    return this;
  }

  public Pattern singleLine() {
    singleLine = true;
    return this;
  }

  public Pattern score(final int score) {
    this.score = score;
    return this;
  }

  public Pattern next(final Matcher matcher) {
    matchers.add(matcher);
    return this;
  }

  public Pattern name(final String name) {
    this.name = name;
    return this;
  }

  public Optional<PatternMatch> matchesAt(final HocrPage page, final int index) {
    int i = index;
    final Map<String, String> groups = Maps.newHashMap();
    if (index >= page.size()) {
      return Optional.empty();
    }
    if (matchLine && page.get(index).isLineBegin() == false) {
      return Optional.empty();
    }
    for (int n = 0; n < matchers.size(); n++) {
      final Matcher matcher = matchers.get(n);
      final Optional<MatcherResult> length = matcher.matchesAt(page, i);
      if (length.isPresent()) {
        i += length.get().getLength();
        for (final Map.Entry<String, String> entry : length.get().getGroups().entrySet()) {
          groups.put(entry.getKey(),
              (groups.getOrDefault(entry.getKey(), "") + " " + entry.getValue()).trim());
        }
      } else if (matcher.isOptional()) {
        // just ignore optional matcher
      } else {
        return Optional.empty();
      }
    }
    final PatternMatch pm = new PatternMatch(index, i, page, groups)
        .withScore(score)
        .withSource(this.name);
    if (matchLine) {
      if (page.get(i - 1).isLineEnd() == false
          || IntStream.range(index, i).mapToObj(page::get).filter(Bbox::isLineEnd).count() > 1) {
        return Optional.empty();
      }
    }
    if (singleLine
        && IntStream.range(index, i - 2).mapToObj(page::get).filter(Bbox::isLineEnd).count() > 0) {
      return Optional.empty();
    }
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
