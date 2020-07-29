package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.kbase.lexicon.StreetLexicon;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherWordInSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class AnnotatorStreet extends Annotator {

  public static String STREET = "street";
  public static String STREET_PREFIX = "street";
  public static String STREET_NO = "street_no";
  public static String NUMBER_FOR_STREET = "number_for_street";

  //public static StreetLexicon streetLexicon = new StreetLexicon();

  private static List<Pattern> getPatterns() {

    final Set<String> streetNames = Sets.newHashSet();

    //streetNames.addAll(streetLexicon);
    //streetNames.addAll(streetNames.stream().map(String::toUpperCase).collect(Collectors.toList()));

    final List<Pattern> patterns = Lists.newArrayList();

    patterns.add(new Pattern("s1")
        .next(new MatcherAnnotationType(AnnotatorStreetPrefix.STREET_PREFIX).group(STREET_PREFIX).optional())
        //.next(new MatcherRegexText("(?i)przy", 6).group(STREET).optional())
        .next(new MatcherRegexText("(?i)(ul\\.|al\\.)", 3).group(STREET))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET))
        .next(new MatcherRegexText("\\p{Lu}\\p{Ll}+", 20).group(STREET).optional())
        .next(new MatcherRegexText("[0-9]{1,3}", 2).group(STREET_NO).optional())
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

    String tmpValue = pm.getGroupValue(STREET).orElse(pm.getText());

    boolean isPrzyOrNa = false;

    if (tmpValue.startsWith("przy")) {
      isPrzyOrNa = true;
      tmpValue = tmpValue.substring(5);
    } else if (tmpValue.startsWith("na")) {
      isPrzyOrNa = true;
      tmpValue = tmpValue.substring(3);
    }


    if (isPrzyOrNa) {
      // TODO - zmiana tylko ostatniego wystąpienia
      if (tmpValue.endsWith("kiej")) {
        tmpValue = tmpValue.replaceAll("kiej", "ka");
      } else if (tmpValue.endsWith("czej")) {
        tmpValue = tmpValue.replaceAll("czej", "cza");
      } else if (tmpValue.endsWith("nej")) {
        tmpValue = tmpValue.replaceAll("nej", "na");
      }
    }

    //log.error("tmpValue = '" + tmpValue + "'");
    if (tmpValue.toLowerCase().startsWith("ul."))
      tmpValue = tmpValue.substring(4);
    else if (tmpValue.toLowerCase().startsWith("al."))
      tmpValue = tmpValue.substring(4);

    return tmpValue;
  }

}
