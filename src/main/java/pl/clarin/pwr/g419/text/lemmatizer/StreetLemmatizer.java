package pl.clarin.pwr.g419.text.lemmatizer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;

public class StreetLemmatizer {

  Map<String, String> endings = Maps.newHashMap();

  Set<String> ignorePrefixes = Sets.newHashSet("al", "al.", "pl", "pl.");

  public StreetLemmatizer() {
    endings.put("ej", "a");
    endings.put("kiej", "ka");
    endings.put("czej", "cza");
    endings.put("nej", "na");
    endings.put("stej", "sta");
    endings.put("owej", "owa");
    endings.put("otej", "ota");
    endings.put("mskich", "mskie");
    endings.put("skim", "ski"); // 2-gi wyraz
  }

  public String lemmatize(final String text) {
    final String[] parts = text.split(" ");
    if (parts.length == 1) {
      return lemmatizeWord(text);
    } else if (parts.length == 2 && ignorePrefixes.contains(parts[0].toLowerCase())) {
      return parts[0] + " " + lemmatizeWord(parts[1]);
    } else {
      return text;
    }
  }

  private String lemmatizeWord(final String text) {
    for (final Map.Entry<String, String> entry : endings.entrySet()) {
      if (text.endsWith(entry.getKey())) {
        return text.substring(0, text.length() - entry.getKey().length()) + entry.getValue();
      }
    }
    return text;
  }
}
