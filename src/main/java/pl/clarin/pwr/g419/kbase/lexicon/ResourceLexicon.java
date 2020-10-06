package pl.clarin.pwr.g419.kbase.lexicon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.kbase.NeLexicon2;

abstract public class ResourceLexicon extends HashSet<String> implements HasLogger {

  abstract protected String getResourcePath();

  public ResourceLexicon() {
    this(0);
  }

  // jeśli limit==0 to ładuj wszystko
  public ResourceLexicon(int limit) {
    try {
      load(limit);
    } catch (final Exception ex) {
      getLogger().error("Failed to load CompanyLexicon from {}", getResourcePath());
      getLogger().error("", ex);
    }
  }

  protected void load(int limit) throws IOException {
    try (
        final InputStream stream = NeLexicon2.class.getResourceAsStream(getResourcePath())
    ) {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      reader.lines()
          .map(line -> line.strip())
          .limit(limit == 0 ? Long.MAX_VALUE : limit)
          .forEach(this::add);
    }
  }

}
