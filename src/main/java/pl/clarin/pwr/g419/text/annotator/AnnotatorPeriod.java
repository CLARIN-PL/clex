package pl.clarin.pwr.g419.text.annotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.*;
import pl.clarin.pwr.g419.text.normalization.NormalizerMap;
import pl.clarin.pwr.g419.text.normalization.NormalizerNum2Digit;
import pl.clarin.pwr.g419.text.pattern.Pattern;
import pl.clarin.pwr.g419.text.pattern.PatternMatch;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherAnnotationType;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherLowerText;
import pl.clarin.pwr.g419.text.pattern.matcher.MatcherRegexText;

public class AnnotatorPeriod extends Annotator {

  public static String PERIOD = "period";

  public static String DATE_BEGIN = "date_begin";
  public static String DATE_END = "date_end";
  public static String DAY_BEGIN = "day_begin";
  public static String DAY_END = "day_end";
  public static String MONTH_BEGIN = "month_begin";
  public static String MONTH_END = "month_end";
  public static String YEAR = "year";

  public static String HALFYEAR = "halfyear";
  public static String HALFYEAR1 = "1";
  public static String HALFYEAR2 = "2";

  private static List<Pattern> getPatterns() {
    final List<Pattern> patterns = Lists.newArrayList();

    final Map<String, String> halfYears = Maps.newHashMap();
    halfYears.put("i", HALFYEAR1);
    halfYears.put("ii", HALFYEAR2);
    halfYears.put("i-szym", HALFYEAR1);
    halfYears.put("ii-m", HALFYEAR2);
    halfYears.put("pierwsze", HALFYEAR1);
    halfYears.put("drugie", HALFYEAR2);

    final Map<String, String> months = AnnotatorDate.getMonths();

    patterns.add(new Pattern()
        .next(new MatcherAnnotationType(AnnotatorDate.DATE).group(DATE_BEGIN))
        .next(new MatcherLowerText(Set.of("roku", "r.")).optional())
        .next(new MatcherLowerText(Sets.newHashSet("do", "–", "-")))
        .next(new MatcherLowerText(Sets.newHashSet("dnia")).optional())
        .next(new MatcherAnnotationType(AnnotatorDate.DATE).group(DATE_END))
    );
    patterns.add(new Pattern()
        .next(new MatcherRegexText("[0-9]{1,2}", 2).group(DAY_BEGIN)
            .normalizer(new NormalizerNum2Digit()))
        .next(new MatcherLowerText(months.keySet()).group(MONTH_BEGIN)
            .normalizer(new NormalizerMap(months)))
        .next(new MatcherLowerText(Sets.newHashSet("do", "–", "-")))
        .next(new MatcherAnnotationType(AnnotatorDate.DATE).group(DATE_END))
    );
    patterns.add(new Pattern()
        .next(new MatcherRegexText(
            "([0-9]{1,2})[.]([0-9]{1,2})(?:-|do)([0-9]{1,2})[.]([0-9]{1,2})[.]([0-9]{4})", 16,
            Map.of(1, DAY_BEGIN, 2, MONTH_BEGIN, 3, DAY_END, 4, MONTH_END, 5, YEAR)))
    );
    patterns.add(new Pattern()
        .next(new MatcherRegexText(
            "([0-9]{1,2})[-]([0-9]{1,2})[.]([0-9]{4})", 10,
            Map.of(1, MONTH_BEGIN, 2, MONTH_END, 3, YEAR)))
    );
    patterns.add(new Pattern()
        .next(new MatcherLowerText(halfYears.keySet()).group(HALFYEAR)
            .normalizer(new NormalizerMap(halfYears)))
        .next(new MatcherLowerText(Set.of("półrocze", "półroczu")))
        .next(new MatcherRegexText("([0-9]{4}).*", 6, Map.of(1, YEAR)))
    );
    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("6", "sześciu")))
        .next(new MatcherLowerText(Set.of("miesięcy")))
        .next(new MatcherLowerText(
            Set.of("zakonczony", "zakończony", "kończący", "kończących", "zakończonych")))
        .next(new MatcherLowerText(Set.of("się")).optional())
        .next(new MatcherLowerText(Set.of("dnia")).optional())
        .next(new MatcherAnnotationType(AnnotatorDate.DATE).group(DATE_END))
    );
    patterns.add(new Pattern()
        .next(new MatcherLowerText(Set.of("półrocze")))
        .next(new MatcherLowerText(Set.of("zakończone")))
        .next(new MatcherLowerText(Set.of("dnia")))
        .next(new MatcherAnnotationType(AnnotatorDate.DATE).group(DATE_END))
    );
    return patterns;
  }

  public AnnotatorPeriod() {
    super(PERIOD, getPatterns());
  }

  @Override
  public String normalize(final PatternMatch pm) {
    final Optional<String> dateBegin = pm.getGroupValue(DATE_BEGIN);
    final Optional<String> dateEnd = pm.getGroupValue(DATE_END);
    final Optional<String> dayBegin = pm.getGroupValue(DAY_BEGIN);
    final Optional<String> dayEnd = pm.getGroupValue(DAY_END);
    final Optional<String> monthBegin = pm.getGroupValue(MONTH_BEGIN);
    final Optional<String> monthEnd = pm.getGroupValue(MONTH_END);
    final Optional<String> halfYear = pm.getGroupValue(HALFYEAR);
    final Optional<String> year = pm.getGroupValue(YEAR);

    if (dateBegin.isPresent() && dateEnd.isPresent()) {
      return dateBegin.get() + ":" + dateEnd.get();
    }

    if (halfYear.isPresent() && year.isPresent()) {
      final String begin = year.get() + (halfYear.get().equals(HALFYEAR1) ? "-01-01" : "-07-01");
      final String end = year.get() + (halfYear.get().equals(HALFYEAR1) ? "-06-30" : "-12-31");
      return String.format("%s:%s", begin, end);
    }

    if (dayBegin.isPresent() && dayEnd.isPresent()
        && monthBegin.isPresent() && monthEnd.isPresent() && year.isPresent()) {
      final String begin = String.format("%s-%s-%s", year.get(), monthBegin.get(), dayBegin.get());
      final String end = String.format("%s-%s-%s", year.get(), monthEnd.get(), dayEnd.get());
      return String.format("%s:%s", begin, end);
    }

    if (monthBegin.isPresent() && monthEnd.isPresent() && year.isPresent()) {
      final int monthDays = YearMonth.of(Integer.parseInt(year.get()), Integer.parseInt(monthEnd.get()))
          .lengthOfMonth();
      final String begin = String.format("%s-%s-01", year.get(), monthBegin.get());
      final String end = String.format("%s-%s-%02d", year.get(), monthEnd.get(), monthDays);
      return String.format("%s:%s", begin, end);
    }

    if (dateEnd.isPresent()) {
      try {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        final Date date = format.parse(dateEnd.get().toLowerCase());
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.MONTH, -6);
        final String startDate = format.format(c.getTime());
        return String.format("%s:%s", startDate, dateEnd.get());
      } catch (final ParseException e) {
        getLogger().error("Failed to parse date '{}'", dateEnd.get());
      }
    }

    return pm.getText();
  }

}
