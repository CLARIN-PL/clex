package pl.clarin.pwr.g419.text.normalization;

import lombok.extern.slf4j.Slf4j;
import java.util.Set;

@Slf4j
public class NormalizerStreet extends Normalizer<String> {

  @Override
  public String normalize(final String value) {

    if (value == null) {
      return null;
    }

    if (value.isEmpty()) {  // TODO : dlaczego tu w ogóle docierają takie rzeczy ?
      return value;
    }

    final Set<String> trimStartWords = Set.of(
        "ul. ",
        "ul "
        //"al. ",
        //"al ",
        //"pl. ",
        //"pl "
    );

    String result = trimFromStartIfMatch(value, trimStartWords);

    final String[] words = result.split(" ");
    if (words.length > 0) {
      final String lastWord = words[words.length - 1];
      if (Character.isDigit(lastWord.charAt(0))) {
        final int endIndex = result.length() - lastWord.length() - 1;
        if (endIndex >= 0) {  // np: 11-listopada
          result = result.substring(0, endIndex);
        }
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
