package pl.clarin.pwr.g419.text.lemmatizer;

import com.google.common.collect.Maps;
import java.util.Map;

public class StreetLemmatizer {

  Map<String, String> endings = Maps.newHashMap();

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
    return text.split(" ").length == 1 ? lemmatizeWord(text) : text;
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
