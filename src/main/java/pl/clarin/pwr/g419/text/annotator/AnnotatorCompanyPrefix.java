package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;

public class AnnotatorCompanyPrefix extends Annotator {

  public static String COMPANY_PREFIX = "company_prefix";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("sprawozdanie")))
        .next(new MatcherLowerText(Set.of("finansowe")))
        .next(new MatcherLowerText(Set.of("grupy")).optional())
        .next(new MatcherLowerText(Set.of("kapitałowej")).optional())
        .next(new MatcherLowerText(Set.of("spółki")).optional())
    );

    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("działalności", "raport")))
        .next(new MatcherLowerText(Set.of("gospodarczej")).optional())
        .next(new MatcherLowerText(Set.of("finansowy")).optional())
        .next(new MatcherLowerText(Set.of("półroczny")).optional())
        .next(new MatcherLowerText(Set.of("grupy")).optional())
        .next(new MatcherLowerText(Set.of("kapitałowej")).optional())
    );

    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("nazwa")))
        .next(new MatcherLowerText(Set.of("jednostki:")))
    );

    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("grupa")))
        .next(new MatcherLowerText(Set.of("kapitałowa")))
    );

    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("spółka")))
    );

    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("nazwa:")))
    );

    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("nazwa")))
        .next(new MatcherLowerText(Set.of("i")))
        .next(new MatcherLowerText(Set.of("siedziba:")))
    );

    return patterns;
  }

  public AnnotatorCompanyPrefix() {
    super(COMPANY_PREFIX, getPatterns());
  }

}
