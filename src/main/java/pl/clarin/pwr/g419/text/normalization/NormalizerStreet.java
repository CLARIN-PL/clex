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
        "al ",
        "pl. ",
        "pl "
    );

    String result = trimFromStartIfMatch(value, trimStartWords).toUpperCase();

    String[] words = result.split(" ");
    if (words.length > 0) {
      String lastWord = words[words.length - 1];
      if (Character.isDigit(lastWord.charAt(0))) {
        result = result.substring(0, result.length() - lastWord.length() - 1);
      }
    }

    return result;

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
