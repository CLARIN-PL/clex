package pl.clarin.pwr.g419.text.normalization;

import java.util.Set;

public class NormalizerStreet extends Normalizer<String> {

  @Override
  public String normalize(final String value) {
    if (value == null) {
      return null;
    }

    final Set<String> trimStartWords = Set.of(
        "ul. ",
        "ul ",
        "al. ",
        "al "
    );

    return trimFromStartIfMatch(value, trimStartWords).toUpperCase();

  }

  private String trimFromStartIfMatch(final String str, final Set<String> words) {
    for (final String tr : words) {
      if (str.toLowerCase().startsWith(tr)) {
        return str.substring(tr.length());
      }
    }
    return str;
  }

}
