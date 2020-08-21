package pl.clarin.pwr.g419.text;

import java.util.HashSet;
import java.util.Set;

public class Test {

  public static void main(final String[] args) {

    final String line = "Podpisy, wszystkich ale to wszystkich .. członków zarządu :";
    final String[] tmp = line.trim().toLowerCase().split("[ :.,']");
    final Set<String> words = new HashSet();
    for (final String s : tmp) {
      System.out.println(s.length() + " " + s);
      if (s.length() > 0) {
        words.add(s);
      }
    }


    final Set<String> endWords = Set.of("podpisy", "wszystkich", "członków", "zarządu", "osób", "odpowiedzialnych", "reprezentujących", "s.a.");

    final Set<String> rest = new HashSet<>();

    words.forEach((w) -> {
      if (!endWords.contains(w)) {
        rest.add(w);
      }
    });


    System.out.println(rest);

  }


}
