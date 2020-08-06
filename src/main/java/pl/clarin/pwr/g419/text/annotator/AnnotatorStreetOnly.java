package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.text.lemmatizer.StreetLemmatizer;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;

@Slf4j
public class AnnotatorStreetOnly extends Annotator {

  public static String STREET_ONLY = "street_only";

  private final StreetLemmatizer lemmatizer = new StreetLemmatizer();

  private static List<Pattern> getPatterns() {

    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("street_dot")
        .next(new MatcherLowerText(Set.of("przy", "na")).optional())
        .next(new MatcherRegexText("(?i)((\\()?ul\\.|(\\()?al\\.)", 4))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET_ONLY))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}+", 20).group(STREET_ONLY).optional())
    );

    patterns.add(new Pattern("street_nodot")
        .next(new MatcherLowerText(Set.of("przy", "na")).optional())
        .next(new MatcherRegexText("(?i)((\\()?ul[.]?|(\\()?al[.]?|ulicy|alei)", 6))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET_ONLY))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}+", 20).group(STREET_ONLY).optional())
    );

    return patterns;
  }

  public AnnotatorStreetOnly() {
    super(STREET_ONLY, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    final String streetName = pm.getGroupValue(STREET_ONLY).orElse(pm.getText());
    return lemmatizer.lemmatize(streetName);
  }

}
