package pl.clarin.pwr.g419.kbase.lexicon;

import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.clarin.pwr.g419.utils.TextComparator;

public class CompanyLexicon extends ResourceLexicon {

  private final String resourcePath = "/companies.txt";
  private final TextComparator comparator = new TextComparator(0.9);

  @Override
  protected String getResourcePath() {
    return resourcePath;
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
