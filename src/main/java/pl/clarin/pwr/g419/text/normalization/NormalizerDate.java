package pl.clarin.pwr.g419.text.normalization;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NormalizerDate extends Normalizer<Date> {

  @Override
  public String normalize(final Date value) {
    if (value == null) {
      return null;
    }
    try {
      return new SimpleDateFormat("yyyy-MM-dd").format(value);
    } catch (final Exception ex) {
      return null;
    }
  }
}
