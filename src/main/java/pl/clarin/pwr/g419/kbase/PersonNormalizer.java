package pl.clarin.pwr.g419.kbase;

import org.apache.commons.text.WordUtils;
import pl.clarin.pwr.g419.kbase.lexicon.PersonNameLexicon;

public class PersonNormalizer {

  PersonNameLexicon personNameLexicon = new PersonNameLexicon();

  public String normalize(final String name) {
    if (name == null || name.length() == 0) {
      return name;
    }

    final String[] parts = name.split("_");
    if (parts.length > 1) {
      parts[1] = parts[1].replaceAll("_główna księgowa_", "_główny księgowy_")
          .replaceAll("zarzadu", "zarządu")
          .replaceAll("wieceprezes", "wiceprezes")
          .replaceAll("wiceprezez", "wiceprezes")
          .replace("czlonek", "członek")
          .replaceAll(" (spółki|banku)", "")
          .replaceAll(" (zarządu)", "")
          .replaceAll(" ds[.][^_]+", "_")
          .replaceAll(",[^_]+", "")
      ;
    }
    if (parts.length > 2) {
      parts[2] = personNameLexicon.
          getFullName(parts[2].replaceAll("( )*([-])( )*", "$2"));

      parts[2] = WordUtils.capitalizeFully(parts[2], new char[] {' ', '-'});
    }

    return String.join("_", parts);
  }

}
