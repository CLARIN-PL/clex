package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import java.util.List;
import java.util.Set;

public class AnnotatorHeadQuarters extends Annotator {

  public static final String CITY = "hq_city";
  public static final String STREET = "hq_street";

  public static String HEADQUARTERS = "headquarters";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(
        patternRegexpSequence("z siedzibą w ", "hq5")
            .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(CITY))
            .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(CITY).optional())
            .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(CITY).optional())
            .next(new MatcherLowerText(Set.of(",")).optional())
            .next(new MatcherAnnotationType(AnnotatorStreet.STREET).group(STREET).optional())
    );

    patterns.add(new Pattern("hq6")
        .next(new MatcherLowerText("siedziba"))
        .next(new MatcherLowerText("spólki").optional())
        .next(new MatcherLowerText("spólki").optional())

        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(CITY))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(CITY).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(CITY).optional())
        .next(new MatcherLowerText(Set.of(",")).optional())
        .next(new MatcherAnnotationType(AnnotatorStreet.STREET).group(STREET).optional())
    );


    patterns.add(new Pattern("hq1")
        .next(new MatcherRegexText("Siedziba", 6))
        .next(new MatcherRegexText("spółki", 6).optional())
    );
/*
    patterns.add(new Pattern("hq2")
        .next(new MatcherRegexText("siedziba", 6)));

    patterns.add(new Pattern("hq3")
        .next(new MatcherRegexText("siedzibą", 6)));

    patterns.add(new Pattern("hq3")
        .next(new MatcherRegexText("siedzibą", 6)));

 */


    return patterns;
  }

  public AnnotatorHeadQuarters() {
    super(HEADQUARTERS, getPatterns());
  }

}
