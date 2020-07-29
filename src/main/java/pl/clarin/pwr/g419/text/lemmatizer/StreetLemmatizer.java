package pl.clarin.pwr.g419.text.lemmatizer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StreetLemmatizer {

  Map<String, String> lemmas = Maps.newHashMap();

  Set<String> ignore = Sets.newHashSet();

  public StreetLemmatizer() {
  }

  public String lemmatize(final String text) {
    return Arrays.stream(text.split(" "))
        .map(orth -> lemmas.getOrDefault(orth, orth))
        .filter(orth -> !ignore.contains(orth))
        .collect(Collectors.joining(" "));
  }
}
