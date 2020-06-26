package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;

public class AnnotatorSignsPage extends Annotator {

  public static String SIGNS_PAGE = "signs_page";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();
    patterns.add(new Pattern()
        .next(new MatcherRegexText("(?i)Podpisy", 6)));
    return patterns;
  }

  public AnnotatorSignsPage() {
    super(SIGNS_PAGE, getPatterns());
  }

}
