package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import java.util.List;

public class AnnotatorStreetNo extends Annotator {

  public static String STREET_NO = "street_no";


  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("sn1")
        .next(new MatcherRegexText("[0-9]{1,3}", 2).group(STREET_NO)));


    return patterns;
  }

  public AnnotatorStreetNo() {
    super(STREET_NO, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return pm.getGroupValue(STREET_NO).orElse(pm.getText());
  }

}
