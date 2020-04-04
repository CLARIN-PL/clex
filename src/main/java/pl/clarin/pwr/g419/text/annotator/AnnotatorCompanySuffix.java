package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;

public class AnnotatorCompanySuffix extends Annotator {

  public static String COMPANY_SUFFIX = "company_suffix";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern()
        .next(new MatcherRegexText("s[.]?a[.]?", 4).lowerCase())
    );

    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("spółka")))
        .next(new MatcherLowerText(Set.of("akcyjna")))
    );

    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("se")))
    );

    return patterns;
  }

  public AnnotatorCompanySuffix() {
    super(COMPANY_SUFFIX, getPatterns());
  }

}
