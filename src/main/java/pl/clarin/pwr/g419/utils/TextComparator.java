package pl.clarin.pwr.g419.utils;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class TextComparator {

  private final double threshold;
  final LevenshteinDistance distance = new LevenshteinDistance();

  public TextComparator(final double threshold) {
    this.threshold = threshold;
  }

  public boolean equals(final String t1, final String t2) {
    final String t1n = t1.toLowerCase();
    final String t2n = t2.toLowerCase();
    if (t1n.equals(t2n)) {
      return true;
    }
    final double dist = distance.apply(t1n, t2n);
    final double sim = (t1n.length() + t2n.length() - dist) / (t1n.length() + t2n.length());
    return sim > threshold;
  }
}
