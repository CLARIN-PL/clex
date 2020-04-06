package pl.clarin.pwr.g419.kbase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.utils.TextComparator;

public class CompanyLexicon extends HashSet<String> implements HasLogger {

  private final String resourcePath = "/companies.txt";
  private final TextComparator comparator = new TextComparator(0.9);

  public CompanyLexicon() {
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

  public String approximate(final String name) {
    if (name.length() == 0) {
      return name;
    }
    return this.stream()
        .map(n -> new ImmutablePair<Double, String>(comparator.sim(n, name), n))
        .sorted((o1, o2) -> Double.compare(o2.left, o1.left))
        .filter(o -> o.left > 0.9 || withinOneOrAnother(o.right, name))
        .map(ImmutablePair::getRight)
        .findFirst().orElse(name);
  }

  public boolean withinOneOrAnother(final String a, final String b) {
    final String ap = " " + a + " ";
    final String bp = " " + b + " ";
    return ap.contains(bp) || bp.contains(ap);
  }
}
