package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;

public class AnnotatorDrawingDate extends Annotator {

  public static String DRAWING_DATE = "drawing_date";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern().matchLine()
        .next(new MatcherRegexText("\\p{Lu}\\p{L}{3,20}(,)?", 20).join(" ")
            .ignore(Sets.newHashSet("dzień", "dnia",
                "zakończony", "zakończonego", "zakończonych", "zakończone", "zakooczone", "zakooczony",
                "aktywa", "długoterminowe", "dominującej", "sporządzone", "zobowiązania",
                "wrażliwość", "noty", "nota", "waluta", "inwestycje",
                "wyszczególnienie", "bilans", "pasywa", "limit", "część", "wibor", "wskaźniki"))
        )
        .next(new MatcherLowerText("dnia").optional())
        .next(new MatcherAnnotationType(AnnotatorDate.DATE).group(DRAWING_DATE))
    );

    return patterns;
  }

  public AnnotatorDrawingDate() {
    super(DRAWING_DATE, getPatterns());
  }

  @Override
  public String normalize(final PatternMatch pm) {
    return pm.getGroupValue(DRAWING_DATE).orElse("");
  }

}
