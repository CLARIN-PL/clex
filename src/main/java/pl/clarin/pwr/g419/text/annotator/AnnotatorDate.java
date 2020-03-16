package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;

public class AnnotatorDate extends Annotator {

  public static String DATE = "date";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    final Set<String> months = Sets.newHashSet(
        "stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca",
        "lipca", "sierpnia", "września", "października", "listopada", "grudnia");
    final Set<String> years = IntStream.range(1900, 2030)
        .mapToObj(Objects::toString).collect(Collectors.toSet());
    final Set<String> yearSuffix = Sets.newHashSet("r", "r.", "roku");
    final Set<String> days = IntStream.range(1, 31)
        .mapToObj(Objects::toString).collect(Collectors.toSet());
    patterns.add(new Pattern()
        .next(new MatcherLowerText(days))
        .next(new MatcherLowerText(months))
        .next(new MatcherLowerText(years))
        .next(new MatcherLowerText(yearSuffix).optional())
    );

    patterns.add(new Pattern()
        .next(new MatcherRegexText(
            java.util.regex.Pattern.compile("[0-9]{1,2}[.-][0-9]{1,2}[.-][0-9]{4}"), 13)));

    patterns.add(new Pattern()
        .next(new MatcherRegexText(
            java.util.regex.Pattern.compile("[0-9]{1,2}[.-][0-9]{1,2}[.-][0-9]{4}(r[.])"), 13)));

    return patterns;
  }

  public AnnotatorDate() {
    super(DATE, getPatterns());
  }

}
