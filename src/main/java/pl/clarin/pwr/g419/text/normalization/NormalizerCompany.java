package pl.clarin.pwr.g419.text.normalization;

import pl.clarin.pwr.g419.kbase.CompanySuffix;

public class NormalizerCompany extends NormalizerString {

  CompanySuffix suffix = new CompanySuffix();

  public String normalize(final String name) {
    if (name == null || name.length() == 0) {
      return name;
    }
    return suffix.stripFromName(name.toUpperCase())
        .replaceAll("( )*([-.-])( )*", "$2")
        .replaceAll("–", "-")
        .replaceAll("[„”\"]", "")
        .replaceAll("[.]$", "");
  }

}
