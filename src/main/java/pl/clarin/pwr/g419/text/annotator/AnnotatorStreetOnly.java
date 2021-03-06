package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.text.lemmatizer.StreetLemmatizer;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;

@Slf4j
public class AnnotatorStreetOnly extends Annotator {

  public static String STREET_ONLY = "street_only";
  public static String STREET_ONLY_PREPOSITION = "street_only_preposition";

  private final StreetLemmatizer lemmatizer = new StreetLemmatizer();

  private static List<Pattern> getPatterns() {

    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("street_only_dot_al_pl")
        .next(new MatcherLowerText(Set.of("przy", "na")).group(STREET_ONLY_PREPOSITION).optional())
        .next(new MatcherRegexText("(?i)((\\()?al\\.|(\\()?pl\\.)", 4).group(STREET_ONLY))
        .next(new MatcherRegexText("[0-9]+", 20).group(STREET_ONLY).optional())
        .next(new MatcherAnnotationType(AnnotatorStreetNameStartAbbreviation.STREET_NAME_START_ABBRV).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}{2,20}", 1).group(STREET_ONLY))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}{2,20}", 1).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}(\\p{Lu}|\\p{Ll})+", 1).group(STREET_ONLY).optional())
    );

    patterns.add(new Pattern("street_only_dot_ul")
        .next(new MatcherLowerText(Set.of("przy", "na")).group(STREET_ONLY_PREPOSITION).optional())
        .next(new MatcherRegexText("(?i)((\\()?ul\\.)", 4))
        .next(new MatcherRegexText("[0-9]+", 20).group(STREET_ONLY).optional())
        .next(new MatcherAnnotationType(AnnotatorStreetNameStartAbbreviation.STREET_NAME_START_ABBRV).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}{2,20}", 1).group(STREET_ONLY))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}{2,20}", 1).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}(\\p{Lu}|\\p{Ll})+", 1).group(STREET_ONLY).optional())
    );

    patterns.add(new Pattern("street_only_nodot_al_pl")
        .next(new MatcherLowerText(Set.of("przy", "na")).group(STREET_ONLY_PREPOSITION).optional())
        .next(new MatcherRegexText("(?i)((\\()?al|(\\()?pl)", 6).group(STREET_ONLY))
        .next(new MatcherRegexText("[0-9]+", 20).group(STREET_ONLY).optional())
        .next(new MatcherAnnotationType(AnnotatorStreetNameStartAbbreviation.STREET_NAME_START_ABBRV).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}{2,20}", 1).group(STREET_ONLY))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}{2,20}", 1).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}(\\p{Lu}|\\p{Ll})+", 1).group(STREET_ONLY).optional())
    );

    patterns.add(new Pattern("street_only_nodot_ul")
        .next(new MatcherLowerText(Set.of("przy", "na")).group(STREET_ONLY_PREPOSITION).optional())
        .next(new MatcherRegexText("(?i)((\\()?ul|ulicy|alei|placu)", 6))
        .next(new MatcherRegexText("[0-9]+", 20).group(STREET_ONLY).optional())
        .next(new MatcherAnnotationType(AnnotatorStreetNameStartAbbreviation.STREET_NAME_START_ABBRV).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}{2,20}", 1).group(STREET_ONLY))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}{2,20}", 1).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}(\\p{Lu}|\\p{Ll})+", 1).group(STREET_ONLY).optional())
    );

    return patterns;
  }

  public AnnotatorStreetOnly() {
    super(STREET_ONLY, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    String streetName = pm.getGroupValue(STREET_ONLY).orElse(pm.getText());

    final Optional<String> streetOnlyPreposition = pm.getGroupValue(STREET_ONLY_PREPOSITION);
    final boolean isPrepositionPartiallyDeterminingLemmatization = false; // do wypróbowania na true

    if (!isPrepositionPartiallyDeterminingLemmatization || streetOnlyPreposition.isPresent()) {
      streetName = lemmatizer.lemmatize(streetName);
    }

    return streetName;
  }

}
