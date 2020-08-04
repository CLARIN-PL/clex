package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import java.util.List;
import java.util.Set;

@Slf4j
public class AnnotatorStreetNrLok extends Annotator {

  public static String STREET_NR_LOK = "street_nr_lok";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("street_nr_lok")
        .next(new MatcherRegexText("lok(\\.)?", 2).group(STREET_NR_LOK))
        .next(new MatcherRegexText("(\\p{L}|\\p{N})+", 2).group(STREET_NR_LOK).optional())
    );
    return patterns;
  }

  public AnnotatorStreetNrLok() {
    super(STREET_NR_LOK, getPatterns());
  }


}
