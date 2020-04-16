package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import pl.clarin.pwr.g419.kbase.NeLexicon2;
import pl.clarin.pwr.g419.kbase.lexicon.FirstNameLexicon;
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
  public static String DATE = "date";

  public static NeLexicon2 neLexicon2 = NeLexicon2.get();
  public static FirstNameLexicon firstNameLexicon = new FirstNameLexicon();

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    final Set<String> firstNames =
        Sets.newHashSet(neLexicon2.getNames(NeLexicon2.LIV_PERSON_FIRST));
    firstNames.addAll(firstNameLexicon);
    firstNames.addAll(firstNames.stream().map(String::toUpperCase).collect(Collectors.toList()));

    patterns.add(new Pattern().matchLine()
        .next(new MatcherLowerText("prezes").group(TITLE))
        .next(new MatcherLowerText(Set.of("–", ":", "-")))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(NAME))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(NAME))
    );

    final Map<String, String> roleMap = Map.of("zarządu,", "zarządu");

    patterns.add(new Pattern("person-hor:name-role").singleLine()
        .next(new MatcherAnnotationType(AnnotatorDate.DATE).group(DATE).optional())
        .next(new MatcherWordInSet(firstNames).group(NAME))
        .next(new MatcherWordInSet(firstNames).group(NAME).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+(-\\p{Lu}\\p{Ll}+)?", 40).group(NAME))
        .next(new MatcherLowerText(Set.of("–", ":", "-")).optional())
        .next(new MatcherAnnotationType(AnnotatorRole.ROLE).group(TITLE))
    );

    patterns.add(new Pattern("person-hor:role-name").singleLine()
        .next(new MatcherAnnotationType(AnnotatorDate.DATE).group(DATE).optional())
        .next(new MatcherAnnotationType(AnnotatorRole.ROLE).group(TITLE))
        .next(new MatcherLowerText(Set.of("ds")).optional())
        .next(new MatcherLowerText(Set.of(".")).optional())
        .next(new MatcherLowerText(Set.of("finansowych", "marketingu")).optional())
        .next(new MatcherLowerText(Set.of("–", ":", "-")).optional())
        .next(new MatcherWordInSet(firstNames).group(NAME))
        .next(new MatcherWordInSet(firstNames).group(NAME).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+(-\\p{Lu}\\p{Ll}+)?", 40).group(NAME))
    );

    return patterns;
  }

  public AnnotatorPersonHorizontal() {
    super(PERSON, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    return String.format("%s|%s|%s",
        pm.getGroupValue(DATE).orElse(""),
        pm.getGroupValue(TITLE).orElse(""),
        pm.getGroupValue(NAME).orElse(""));
  }

}
