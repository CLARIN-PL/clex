package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import java.util.List;
import java.util.Set;

public class AnnotatorStreetPrefix extends Annotator {

  public static String STREET_PREFIX = "street_prefix";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("przy", "na")))
    );


    return patterns;
  }

  public AnnotatorStreetPrefix() {
    super(STREET_PREFIX, getPatterns());
  }


}
