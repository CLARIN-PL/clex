package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import java.util.List;
import java.util.Set;

@Slf4j
public class AnnotatorStreet extends Annotator {

  public static String STREET = "street";
  public static String STREET_NO = "street_no";

  private static List<Pattern> getPatterns() {

    final List<Pattern> patterns = Lists.newArrayList();


    patterns.add(new Pattern("street_all")
        .next(new MatcherAnnotationType(AnnotatorStreetOnly.STREET_ONLY).group(STREET))
        .next(new MatcherRegexText("[0-9]{1,3}\\p{L}?(-[0-9]{1,3}\\p{L}?)?(/[0-9]{1,3})?", 2).group(STREET_NO).optional())
        // wyłączenie pojedycznych liter które mają znaczenie - tutaj omijamy tylko 'w' 'W' -teoretycznie pozostaje "i","z","u","a","o"
        .next(new MatcherRegexText("[A-UX-Za-ux-z]", 1).group(STREET_NO).optional())
        .next(new MatcherAnnotationType(AnnotatorStreetNrLok.STREET_NR_LOK).group(STREET_NO).optional())
    );

    return patterns;
  }

  public AnnotatorStreet() {
    super(STREET, getPatterns());
  }


  @Override
  protected String normalize(final PatternMatch pm) {

    String tmpStreetValue = pm.getGroupValue(STREET).orElse(pm.getText());

    String result;
    if (pm.getGroupValue(STREET_NO).isPresent()) {
      result = tmpStreetValue + ":" + pm.getGroupValue(STREET_NO).get();
    } else {
      result = tmpStreetValue + ":";
    }

    return result;
  }


}
