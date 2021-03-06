package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pl.clarin.pwr.g419.text.normalization.NormalizerStringMap;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;

public class AnnotatorRole extends Annotator {

  public static String ROLE = "role";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    final Map<String, String> roleMap = Map.of("zarzadu", "zarządu");

    patterns.add(new Pattern().singleLine()
        .next(new MatcherLowerText("v-ce").group(ROLE).optional())
        .next(new MatcherRegexText(
            "(prezes|wiceprezes|członek|prokurent)", 12)
            .lowerCase()
            .group(ROLE))
        .next(new MatcherLowerText(
            Set.of("zarządu", "zarzadu"))
            .group(ROLE)
            .normalizer(new NormalizerStringMap(roleMap))
        )
    );

    patterns.add(
        patternSequence("Dyrektor Wykonawczy ds. Planowania i Sprawozdawczości".toLowerCase(), "")
    );

    return patterns;
  }

  public AnnotatorRole() {
    super(ROLE, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return pm.getGroupValue(ROLE).orElse(pm.getText());
  }

}
