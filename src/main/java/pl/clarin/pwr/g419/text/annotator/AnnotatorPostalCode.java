package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import java.util.List;

public class AnnotatorPostalCode extends Annotator {

  public static String POSTAL_CODE = "postal_code";


  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("pc1")
        .next(new MatcherRegexText("[0-9][0-9]-[0-9][0-9][0-9]", 2).group(POSTAL_CODE)));


    return patterns;
  }

  public AnnotatorPostalCode() {
    super(POSTAL_CODE, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return pm.getGroupValue(POSTAL_CODE).orElse(pm.getText());
  }

}
