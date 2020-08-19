package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import pl.clarin.pwr.g419.kbase.lexicon.CityLexicon;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotatorCityWithDate extends Annotator {

  public static final String CITY_WITH_DATE = "city_with_date";

  final static String CITY = "city";
  final static String DATE = "date";


  private static List<Pattern> getPatterns() {

    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("city_with_date")
        .next(new MatcherAnnotationType(AnnotatorCity.CITY).group(CITY))
        .next(new MatcherWordInSet(Set.of(",", ".", "-", ":", ";")).optional())
        .next(new MatcherLowerText("dnia").optional())
        .next(new MatcherAnnotationType(AnnotatorDate.DATE).group(DATE))
    );

    patterns.add(new Pattern("date_with_city")
        .next(new MatcherAnnotationType(AnnotatorDate.DATE).group(DATE))
        .next(new MatcherWordInSet(Set.of(",", ".", "-", ":", ";")).optional())
        .next(new MatcherAnnotationType(AnnotatorCity.CITY).group(CITY))
    );

    return patterns;
  }

  public AnnotatorCityWithDate() {
    super(CITY_WITH_DATE, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return pm.getGroupValue(CITY).get() + ":" + pm.getGroupValue(DATE).get();
  }

}
