package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherWordInSet;
import pl.clarin.pwr.g419.utils.NeLexicon2;

public class AnnotatorPerson extends Annotator {

  public static String PERSON = "person";
  public static String NAME = "name";
  public static String TITLE = "title";

  public static NeLexicon2 neLexicon2 = NeLexicon2.get();

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern().matchLine()
        .next(new MatcherLowerText("prezes").group(TITLE))
        .next(new MatcherLowerText(Set.of("–", ":", "-")))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(NAME))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(NAME))
    );

    patterns.add(new Pattern()
        .next(new MatcherWordInSet(neLexicon2.getNames("nam_liv_person_first")).group(NAME))
        .next(new MatcherWordInSet(neLexicon2.getNames("nam_liv_person_first")).group(NAME).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+(-\\p{Lu}\\p{Ll}+)?", 40).group(NAME))
        .next(new MatcherLowerText(Set.of("–", ":", "-")).optional())
        .next(new MatcherLowerText(Set.of("prezes", "wiceprezes", "członek")).group(TITLE))
        .next(new MatcherLowerText("zarządu").group(TITLE))
    );

    return patterns;
  }

  public AnnotatorPerson() {
    super(PERSON, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return String.format("%s|%s",
        pm.getGroupValue(TITLE).orElse(""),
        pm.getGroupValue(NAME).orElse(""));
  }

}
