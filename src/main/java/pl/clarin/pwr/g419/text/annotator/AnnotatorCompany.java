package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherNotAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherNotLowerText;

public class AnnotatorCompany extends Annotator {

  public static String COMPANY = "company";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern()
        .next(new MatcherAnnotationType(AnnotatorCompanyPrefix.COMPANY_PREFIX))
        .next(new MatcherNotAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY))
        .next(new MatcherNotAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY).optional())
        .next(new MatcherNotAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY).optional())
        .next(new MatcherNotAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY).optional())
        .next(new MatcherAnnotationType(AnnotatorCompanySuffix.COMPANY_SUFFIX).group(COMPANY))
    );

    final Set<String> preposition = Set.of("w", "za");
    patterns.add(new Pattern()
        .next(new MatcherAnnotationType(AnnotatorCompanyPrefix.COMPANY_PREFIX))
        .next(new MatcherNotLowerText(preposition).group(COMPANY))
        .next(new MatcherNotLowerText(preposition).group(COMPANY).optional())
        .next(new MatcherNotLowerText(preposition).group(COMPANY).optional())
        .next(new MatcherNotLowerText(preposition).group(COMPANY).optional())
        .next(new MatcherLowerText(preposition))
        .next(new MatcherAnnotationType(AnnotatorPeriod.PERIOD))
    );

    return patterns;
  }

  public AnnotatorCompany() {
    super(COMPANY, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return pm.getGroupValue(COMPANY).orElse(pm.getText()).toUpperCase();
  }

}
