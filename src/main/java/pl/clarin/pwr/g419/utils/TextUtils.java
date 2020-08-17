package pl.clarin.pwr.g419.utils;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class TextUtils {
  public static String toTitleCase(final String text) {
    return Arrays.stream(text.split(" "))
        .map(String::toLowerCase)
        .map(StringUtils::capitalize)
        .collect(Collectors.joining(" "));
  }
}
