package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;

public class AnnotatorPeriod extends Annotator {

  public static String PERIOD = "period";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();
    patterns.add(new Pattern()
        .next(new MatcherAnnotationType(AnnotatorDate.DATE))
        .next(new MatcherLowerText(Sets.newHashSet("do")))
        .next(new MatcherAnnotationType(AnnotatorDate.DATE))
    );
    patterns.add(new Pattern()
        .next(new MatcherAnnotationType(AnnotatorDate.DATE))
        .next(new MatcherLowerText(Sets.newHashSet("–", "-")))
        .next(new MatcherAnnotationType(AnnotatorDate.DATE))
    );
    patterns.add(new Pattern()
        .next(new MatcherRegexText(
            java.util.regex.Pattern.compile("[0-9]{1,2}[-][0-9]{1,2}[.][0-9]{4}"), 10))
    );
    patterns.add(new Pattern()
        .next(new MatcherRegexText(
            java.util.regex.Pattern.compile("[0-9]{1,2}[.][0-9]{1,2}[-][0-9]{1,2}[.][0-9]{1,2}[.][0-9]{4}"), 16))
    );
    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("i", "ii")))
        .next(new MatcherLowerText(Set.of("półrocze")))
        .next(new MatcherRegexText(java.util.regex.Pattern.compile("[0-9]{4}"), 4))
    );
    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("i-szym", "ii-m")))
        .next(new MatcherLowerText(Set.of("półroczu")))
        .next(new MatcherRegexText(java.util.regex.Pattern.compile("[0-9]{4}"), 4))
    );
    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("6")))
        .next(new MatcherLowerText(Set.of("miesięcy")))
        .next(new MatcherRegexText(java.util.regex.Pattern.compile("zako[nń]czony"), 12))
        .next(new MatcherLowerText(Set.of("dnia")))
        .next(new MatcherAnnotationType(AnnotatorDate.DATE))
    );
    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("półrocze")))
        .next(new MatcherLowerText(Set.of("zakończone")))
        .next(new MatcherLowerText(Set.of("dnia")))
        .next(new MatcherAnnotationType(AnnotatorDate.DATE))
    );
    return patterns;
  }

  public AnnotatorPeriod() {
    super(PERIOD, getPatterns());
  }

}
