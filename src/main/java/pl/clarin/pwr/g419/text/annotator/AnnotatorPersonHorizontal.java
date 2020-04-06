package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pl.clarin.pwr.g419.kbase.NeLexicon2;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherWordInSet;

public class AnnotatorPersonHorizontal extends Annotator {

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

    final Map<String, String> roleMap = Map.of(
        "zarządu,", "zarządu",
        "zarzadu,", "zarządu");

    patterns.add(new Pattern().singleLine()
        .next(new MatcherWordInSet(neLexicon2.getNames("nam_liv_person_first")).group(NAME))
        .next(new MatcherWordInSet(neLexicon2.getNames("nam_liv_person_first")).group(NAME).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+(-\\p{Lu}\\p{Ll}+)?", 40).group(NAME))
        .next(new MatcherLowerText(Set.of("–", ":", "-")).optional())
        .next(new MatcherAnnotationType(AnnotatorRole.ROLE).group(TITLE))
    );

    patterns.add(new Pattern().singleLine()
        .next(new MatcherAnnotationType(AnnotatorRole.ROLE).group(TITLE))
        .next(new MatcherLowerText(Set.of("ds.")).optional())
        .next(new MatcherLowerText(Set.of("finansowych", "marketingu")).optional())
        .next(new MatcherLowerText(Set.of("–", ":", "-")).optional())
        .next(new MatcherWordInSet(neLexicon2.getNames("nam_liv_person_first")).group(NAME))
        .next(new MatcherWordInSet(neLexicon2.getNames("nam_liv_person_first")).group(NAME).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+(-\\p{Lu}\\p{Ll}+)?", 40).group(NAME))
    );

    return patterns;
  }

  public AnnotatorPersonHorizontal() {
    super(PERSON, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return String.format("%s|%s",
        pm.getGroupValue(TITLE).orElse(""),
        pm.getGroupValue(NAME).orElse(""));
  }

}
