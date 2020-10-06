package pl.clarin.pwr.g419.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

  public static Date parseDate(final String str) {
    try {
      return new SimpleDateFormat("yyyy-MM-dd").parse(str);
    } catch (final Exception e) {
      return null;
    }
  }

  public static Date strToDate(final String date) throws ParseException {
    if (date == null || date.length() == 0) {
      return null;
    }
    return new SimpleDateFormat("yyyy-MM-dd").parse(date);
  }


}
