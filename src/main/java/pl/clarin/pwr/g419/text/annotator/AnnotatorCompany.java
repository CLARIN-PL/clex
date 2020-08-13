package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.*;

public class AnnotatorCompany extends Annotator {

  public static String COMPANY = "company";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("c1")
        .next(new MatcherAnnotationType(AnnotatorCompanyPrefix.COMPANY_PREFIX))
        .next(new MatcherNotAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY))
        .next(new MatcherNotAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY).optional())
        .next(new MatcherNotAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY).optional())
        .next(new MatcherNotAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY).optional())
        .next(new MatcherAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY))
    );

    final Set<String> preposition = Set.of("w");
    patterns.add(new Pattern("c2")
        .next(new MatcherAnnotationType(AnnotatorCompanyPrefix.COMPANY_PREFIX))
        .next(new MatcherNotLowerText(preposition).group(COMPANY))
        .next(new MatcherNotLowerText(preposition).group(COMPANY).optional())
        .next(new MatcherNotLowerText(preposition).group(COMPANY).optional())
        .next(new MatcherNotLowerText(preposition).group(COMPANY).optional())
        .next(new MatcherLowerText(preposition))
        .next(new MatcherAnnotationType(AnnotatorPeriod.PERIOD))
    );

    patterns.add(new Pattern("c3").matchLine()
        .next(new MatcherLowerText("grupy"))
        .next(new MatcherLowerText("kapitałowej"))
        .next(new MatcherAny().group(COMPANY))
    );

    patterns.add(new Pattern("c4").matchLine()
        .next(new MatcherLowerText("grupa"))
        .next(new MatcherLowerText("kapitałowa").optional())
        .next(new MatcherAny().group(COMPANY))
    );

    patterns.add(new Pattern("c5").matchLine()
        .next(new MatcherLowerText("zarządu"))
        .next(new MatcherNotAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY))
        .next(new MatcherAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY))
    );

    patterns.add(new Pattern("c6").matchLine()
        .next(new MatcherRegexText("\\p{Lu}.+", 20).group(COMPANY))
        .next(new MatcherRegexText("\\p{Lu}.+", 20).group(COMPANY).optional())
        .next(new MatcherAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY))
    );

    patterns.add(new Pattern("c7")
        .next(new MatcherAnnotationType(AnnotatorCompanyPrefix.COMPANY_PREFIX))
        .next(new MatcherRegexText("„([^”]+)”", 20, Map.of(1, COMPANY)).join(" "))
    );

    return patterns;
  }

  public AnnotatorCompany() {
    super(COMPANY, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return pm.getGroupValue(COMPANY).orElse(pm.getText());
  }

}
