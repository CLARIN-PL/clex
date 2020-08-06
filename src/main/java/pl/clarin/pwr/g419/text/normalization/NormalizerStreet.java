package pl.clarin.pwr.g419.text.normalization;

import java.util.Set;

public class NormalizerStreet extends Normalizer<String> {

  @Override
  public String normalize(final String value) {
    if (value == null) return null;

    Set<String> trimStartWords = Set.of(
        "ul. ",
        "ul ",
        "al. ",
        "al ",
        "pl. ",
        "pl "
    );

    return trimFromStartIfMatch(value, trimStartWords).toUpperCase();

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
