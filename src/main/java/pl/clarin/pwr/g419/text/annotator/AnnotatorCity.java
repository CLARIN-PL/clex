package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import pl.clarin.pwr.g419.kbase.lexicon.CityLexicon;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherWordInSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotatorCity extends Annotator {

  public static String CITY = "city";

  public static CityLexicon cityLexicon = new CityLexicon();

  private static List<Pattern> getPatterns() {

    final Set<String> cityNames = Sets.newHashSet();

    cityNames.addAll(cityLexicon);
    //cityNames.addAll(cityNames.stream().map(String::toUpperCase).collect(Collectors.toList()));

    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("city")
        .next(new MatcherWordInSet(cityNames).group(CITY)));

    return patterns;
  }

  public AnnotatorCity() {
    super(CITY, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return pm.getGroupValue(CITY).orElse(pm.getText());
  }

}
