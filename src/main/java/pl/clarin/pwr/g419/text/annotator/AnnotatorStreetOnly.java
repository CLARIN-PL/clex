package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.text.lemmatizer.StreetLemmatizer;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import java.util.List;
import java.util.Set;

@Slf4j
public class AnnotatorStreetOnly extends Annotator {

  public static String STREET_ONLY = "street_only";

  private final StreetLemmatizer lemmatizer = new StreetLemmatizer();

  private static List<Pattern> getPatterns() {

    final Set<String> streetNames = Sets.newHashSet();

    //streetNames.addAll(streetLexicon);
    //streetNames.addAll(streetNames.stream().map(String::toUpperCase).collect(Collectors.toList()));

    final List<Pattern> patterns = Lists.newArrayList();

    // popularne ulice trzywyrazowe:
    patterns.add(new Pattern("street_dot_jpii")
        .next(new MatcherLowerText(Set.of("przy", "na")).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("(?i)((\\()?ul\\.|(\\()?al\\.|(\\()?pl\\.)", 3).group(STREET_ONLY))
        .next(new MatcherLowerText("jana").group(STREET_ONLY))
        .next(new MatcherLowerText("pawła").group(STREET_ONLY))
        .next(new MatcherLowerText("ii").group(STREET_ONLY))
    );

    patterns.add(new Pattern("street_dot_ken")
        .next(new MatcherLowerText(Set.of("przy", "na")).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("(?i)((\\()?ul\\.|(\\()?al\\.|(\\()?pl\\.)", 3).group(STREET_ONLY))
        .next(new MatcherLowerText("komisji").group(STREET_ONLY))
        .next(new MatcherLowerText("edukacji").group(STREET_ONLY))
        .next(new MatcherLowerText("narodowej").group(STREET_ONLY))
    );

    patterns.add(new Pattern("street_nodot_jpii")
        .next(new MatcherLowerText(Set.of("przy", "na")).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("(?i)((\\()?ul|(\\()?al|(\\()?pl)", 3).group(STREET_ONLY))
        .next(new MatcherLowerText("jana").group(STREET_ONLY))
        .next(new MatcherLowerText("pawła").group(STREET_ONLY))
        .next(new MatcherLowerText("ii").group(STREET_ONLY))
    );

    patterns.add(new Pattern("street_nodot_ken")
        .next(new MatcherLowerText(Set.of("przy", "na")).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("(?i)((\\()?ul|(\\()?al|(\\()?pl)", 3).group(STREET_ONLY))
        .next(new MatcherLowerText("komisji").group(STREET_ONLY))
        .next(new MatcherLowerText("edukacji").group(STREET_ONLY))
        .next(new MatcherLowerText("narodowej").group(STREET_ONLY))
    );


    patterns.add(new Pattern("street_dot")
        .next(new MatcherLowerText(Set.of("przy", "na")).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("(?i)((\\()?ul\\.|(\\()?al\\.|(\\()?pl\\.)", 4).group(STREET_ONLY))
        .next(new MatcherRegexText("[0-9]+", 20).group(STREET_ONLY).optional())
        .next(new MatcherLowerText(Set.of("św.", "płk.", "pl.", "gen.", "prof.")).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET_ONLY))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}+", 20).group(STREET_ONLY).optional())
    );

    patterns.add(new Pattern("street_nodot")
        .next(new MatcherLowerText(Set.of("przy", "na")).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("(?i)((\\()?ul|(\\()?al|(\\()?pl|ulicy|alei|placu)", 6).group(STREET_ONLY))
        .next(new MatcherRegexText("[0-9]+", 20).group(STREET_ONLY).optional())
        .next(new MatcherLowerText(Set.of("św.", "płk.", "pl.", "gen.", "prof.")).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET_ONLY))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET_ONLY).optional())
        .next(new MatcherRegexText("\\p{Lu}+", 20).group(STREET_ONLY).optional())
    );

    return patterns;
  }

  public AnnotatorStreetOnly() {
    super(STREET_ONLY, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {

    String tmpStreetValue = pm.getGroupValue(STREET_ONLY).orElse(pm.getText());

    String oldTmpStreetValue = tmpStreetValue;
    tmpStreetValue = trimFromStartIfMatch(tmpStreetValue, Set.of("przy ", "na "));

    if (!oldTmpStreetValue.equals(tmpStreetValue)) {
      // TODO - zmiana tylko ostatniego wystąpienia
      if (tmpStreetValue.endsWith("kiej")) {
        tmpStreetValue = tmpStreetValue.replaceAll("kiej", "ka");
      } else if (tmpStreetValue.endsWith("czej")) {
        tmpStreetValue = tmpStreetValue.replaceAll("czej", "cza");
      } else if (tmpStreetValue.endsWith("nej")) {
        tmpStreetValue = tmpStreetValue.replaceAll("nej", "na");
      } else if (tmpStreetValue.endsWith("stej")) {
        tmpStreetValue = tmpStreetValue.replaceAll("stej", "sta");
      } else if (tmpStreetValue.endsWith("owej")) {
        tmpStreetValue = tmpStreetValue.replaceAll("owej", "owa");
      } else if (tmpStreetValue.endsWith("otej")) {
        tmpStreetValue = tmpStreetValue.replaceAll("otej", "ota");
      } else if (tmpStreetValue.endsWith("skim")) {
        tmpStreetValue = tmpStreetValue.replaceAll("skim", "ski");
      }


    }

    //log.trace("tmpStreetValue = '" + tmpStreetValue + "'");
    Set<String> trimStartWords = Set.of(
        "ul. ", "(ul. ",
        "ul ", "(ul ",
        "al. ", "(al. ",
        "al ", "(al ",
        "pl. ", "(pl. ",
        "pl ", "(pl ",
        "ulicy ", "alei ", "placu ");

    tmpStreetValue = trimFromStartIfMatch(tmpStreetValue, trimStartWords);

    return tmpStreetValue.toUpperCase();
  }

  private String trimFromStartIfMatch(String str, Set<String> words) {
    for (String tr : words) {
      if (str.toLowerCase().startsWith(tr)) {
        return str.substring(tr.length());
      }
    }
    return str;
  }

}
