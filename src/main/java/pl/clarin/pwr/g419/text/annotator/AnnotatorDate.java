package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import pl.clarin.pwr.g419.text.normalization.NormalizerMap;
import pl.clarin.pwr.g419.text.normalization.NormalizerNum2Digit;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;

public class AnnotatorDate extends Annotator {

  public static String DATE = "date";
  public static String DAY = "day";
  public static String MONTH = "month";
  public static String YEAR = "year";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    final Map<String, String> months = getMonths();

    final Set<String> years = IntStream.range(1900, 2030)
        .mapToObj(Objects::toString).collect(Collectors.toSet());
    final Set<String> yearSuffix = Sets.newHashSet("r", "r.", "roku");
    final Set<String> days = IntStream.range(1, 32)
        .mapToObj(Objects::toString).collect(Collectors.toSet());

    patterns.add(new Pattern()
        .next(new MatcherLowerText(days).group(DAY)
            .normalizer(new NormalizerNum2Digit()))
        .next(new MatcherLowerText(months.keySet()).group(MONTH)
            .normalizer(new NormalizerMap(months)))
        .next(new MatcherRegexText("([0-9]{4}).*", 7, Map.of(1, YEAR)))
        .next(new MatcherLowerText(yearSuffix).optional())
    );

    final Map<Integer, String> groups = Map.of(1, DAY, 2, MONTH, 3, YEAR);

    patterns.add(new Pattern().next(
        new MatcherRegexText("([0-9]{1,2})[.-]([0-9]{1,2})[.-]([0-9]{4}).*",
            11, groups)
            .normalizer(DAY, new NormalizerNum2Digit())
            .normalizer(MONTH, new NormalizerNum2Digit())));

    patterns.add(new Pattern().next(
        new MatcherRegexText("([0-9]{1,2})[.-]([0-9]{1,2})[.-]([0-9]{4}).*",
            13, groups)
            .normalizer(DAY, new NormalizerNum2Digit())
            .normalizer(MONTH, new NormalizerNum2Digit())));

    return patterns;
  }

  public static Map<String, String> getMonths() {
    final Map<String, String> months = Maps.newHashMap();
    months.put("stycznia", "01");
    months.put("lutego", "02");
    months.put("marca", "03");
    months.put("kwietnia", "04");
    months.put("maja", "05");
    months.put("czerwca", "06");
    months.put("lipca", "07");
    months.put("sierpnia", "08");
    months.put("września", "09");
    months.put("października", "10");
    months.put("listopada", "11");
    months.put("grudnia", "12");
    return months;
  }

  public AnnotatorDate() {
    super(DATE, getPatterns());
  }

  @Override
  protected String normalize(final PatternMatch pm) {
    final StringJoiner sj = new StringJoiner("-");
    sj.add(pm.getGroupValue(YEAR).orElse(""));
    sj.add(pm.getGroupValue(MONTH).orElse(""));
    sj.add(pm.getGroupValue(DAY).orElse(""));
    return sj.toString();
  }

}