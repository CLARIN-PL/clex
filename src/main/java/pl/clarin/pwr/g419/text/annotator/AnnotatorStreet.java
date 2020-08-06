package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import java.util.List;
import java.util.Set;

@Slf4j
public class AnnotatorStreet extends Annotator {

  public static String STREET = "street";
  public static String STREET_NO = "street_no";

  //public static StreetLexicon streetLexicon = new StreetLexicon();

  private static List<Pattern> getPatterns() {

    final Set<String> streetNames = Sets.newHashSet();

    //streetNames.addAll(streetLexicon);
    //streetNames.addAll(streetNames.stream().map(String::toUpperCase).collect(Collectors.toList()));

    final List<Pattern> patterns = Lists.newArrayList();

/*
    patterns.add(new Pattern("street_1")
        .next(new MatcherLowerText(Set.of("przy", "na")).group(STREET).optional())
        .next(new MatcherRegexText("(?i)(ul\\.|al\\.)", 3).group(STREET))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET).optional())
        .next(new MatcherRegexText("[0-9]{1,3}(-[0-9]{1,3})?\\p{L}?(/[0-9]{1,3})?", 2).group(STREET_NO).optional())
    );

 */

    patterns.add(new Pattern("street_house_nr_with_letter")
        .next(new MatcherAnnotationType(AnnotatorStreetOnly.STREET_ONLY).group(STREET))
        .next(new MatcherRegexText("[0-9]{1,3}", 2).group(STREET_NO))
        .next(new MatcherRegexText("[A-Ka-k]", 1).group(STREET_NO))
    );


    patterns.add(new Pattern("street_all")
        .next(new MatcherAnnotationType(AnnotatorStreetOnly.STREET_ONLY).group(STREET))
        .next(new MatcherRegexText("[0-9]{1,3}\\p{L}?(-[0-9]{1,3}\\p{L}?)?(/[0-9]{1,3})?", 2).group(STREET_NO).optional())
        .next(new MatcherAnnotationType(AnnotatorStreetNrLok.STREET_NR_LOK).group(STREET_NO).optional())
    );


//    patterns.add(new Pattern("s2")
//        .next(new MatcherRegexText("(?i)(ul\\.|al\\.)", 3).group(STREET))
//        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET))
//    );
//

//
//    patterns.add(new Pattern("street")
//        .next(new MatcherWordInSet(streetNames).group(STREET)));

    return patterns;
  }

  public AnnotatorStreet() {
    super(STREET, getPatterns());
  }


  @Override
  protected String normalize(final PatternMatch pm) {

    String tmpStreetValue = pm.getGroupValue(STREET).orElse(pm.getText());

    String result;
    if (pm.getGroupValue(STREET_NO).isPresent()) {
      result = tmpStreetValue + ":" + pm.getGroupValue(STREET_NO).get();
    } else {
      result = tmpStreetValue + ":";
    }

    return result;
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
