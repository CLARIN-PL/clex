package pl.clarin.pwr.g419.text.pattern;

import java.util.Arrays;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;

public class PatternHelper {

  public static Pattern sequenceLoweCase(final String spaceSeparatedWords) {
    final Pattern p = new Pattern();
    Arrays.stream(spaceSeparatedWords.toLowerCase().split(" "))
        .map(MatcherLowerText::new)
        .forEach(p::next);
    return p;
  }

}
