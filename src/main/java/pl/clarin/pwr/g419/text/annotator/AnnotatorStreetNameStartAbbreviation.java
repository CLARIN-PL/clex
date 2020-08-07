package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.text.normalization.NormalizerStreetNameStartAbbrv;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import java.util.List;
import java.util.Set;

@Slf4j
public class AnnotatorStreetNameStartAbbreviation extends Annotator {

  public static String STREET_NAME_START_ABBRV = "street_name_start_abbrv";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("street_name_start_abbrv")
        //.next(new MatcherLowerText(Set.of("św", "płk", "pl", "gen", "prof")).group(STREET_NAME_START_ABBRV))
        .next(new MatcherRegexText("(?iu)gen|św|płk|pl|prof", 2).group(STREET_NAME_START_ABBRV))
        .next(new MatcherRegexText("\\.", 2).group(STREET_NAME_START_ABBRV).optional())
    );
    return patterns;
  }

  public AnnotatorStreetNameStartAbbreviation() {
    super(STREET_NAME_START_ABBRV, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    String value = pm.getGroupValue(STREET_NAME_START_ABBRV).orElse(pm.getText());
    return new NormalizerStreetNameStartAbbrv().normalize(value);
  }

}
