package pl.clarin.pwr.g419.kbase;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import pl.clarin.pwr.g419.HasLogger;

public class NeLexicon2 implements HasLogger {

  public static String LIV_PERSON_FIRST = "nam_liv_person_first";

  static String resourcePath = "/nelexicon2-names.txt.gz";

  static Optional<NeLexicon2> neLexicon2 = Optional.empty();

  Map<String, Set<String>> names = Maps.newHashMap();

  public static NeLexicon2 get() {
    try {
      if (neLexicon2.isEmpty()) {
        neLexicon2 = Optional.of(new NeLexicon2());
        neLexicon2.get().load();
      }
    } catch (final Exception ex) {
      return new NeLexicon2();
    }
    return neLexicon2.get();
  }

  protected void load() throws IOException {
    try (
        final InputStream stream = NeLexicon2.class.getResourceAsStream(resourcePath);
        final InputStream streamGz = new GZIPInputStream(stream)
    ) {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(streamGz));
      reader.lines()
          .map(line -> line.split("\t"))
          .filter(parts -> parts.length == 2)
          .forEach(parts -> names.computeIfAbsent(parts[0], v -> Sets.newHashSet()).add(parts[1]));
    }
  }

  public Set<String> getNames(final String category) {
    return names.getOrDefault(category, Collections.emptySet());
  }
}
