package pl.clarin.pwr.g419.kbase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.utils.TextComparator;

public class FirstNameLexicon extends HashSet<String> implements HasLogger {

  private final String resourcePath = "/first_names.txt";
  private final TextComparator comparator = new TextComparator(0.9);

  public FirstNameLexicon() {
    try {
      load();
    } catch (final Exception ex) {
      getLogger().error("Failed to load CompanyLexicon from {}", resourcePath);
    }
  }

  protected void load() throws IOException {
    try (
        final InputStream stream = NeLexicon2.class.getResourceAsStream(resourcePath);
    ) {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      reader.lines()
          .map(line -> line.strip())
          .forEach(this::add);
    }
  }

}
