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

  // bierzemy tylko największe 2000 miast - nawet
  // niektóre z nich są oznaczone postfixem "DELETE" by nie brać ich pod uwagę
  public static CityLexicon cityLexicon = new CityLexicon(2000);

  private static List<Pattern> getPatterns() {

    final Set<String> cityNames = Sets.newHashSet();

    cityNames.addAll(cityLexicon);
    // uwzględniamy także miasta pisane całe wielkimi literami
    cityNames.addAll(cityNames.stream().map(String::toUpperCase).collect(Collectors.toList()));

    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("city1")
        .next(new MatcherWordInSet(cityNames).group(CITY)));


    // TODO -- "z siedzibą w Kuźni Raciborskiej" doc.id. 118609, odmiana miast

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
