package pl.clarin.pwr.g419.kbase;

import java.util.List;

public class CompanySuffix {

  List<String> companySuffixes = List.of("SPÓŁKA AKCYJNA", "SPÓLKA AKCYJNA",
      "S.A.", "SA", "S.A", "S. A.", "S. A", "S A");


  public String stripFromName(final String name) {
    String text = name;
    for (final String suffix : companySuffixes) {
      if (text.endsWith(" " + suffix)) {
        text = text.substring(0, text.length() - suffix.length()).trim();
      }
    }
    return text;
  }
}
