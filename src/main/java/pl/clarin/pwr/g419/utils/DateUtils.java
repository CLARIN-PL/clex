package pl.clarin.pwr.g419.utils;

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


}
