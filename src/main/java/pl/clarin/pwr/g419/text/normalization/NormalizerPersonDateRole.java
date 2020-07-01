package pl.clarin.pwr.g419.text.normalization;

import pl.clarin.pwr.g419.struct.Person;

public class NormalizerPersonDateRole extends Normalizer<Person> {

  NormalizerDate normalizerDate = new NormalizerDate();

  public String normalize(final Person person) {
    if (person == null) {
      return "";
    }
    return String.format("%s_%s_%s",
        normalizerDate.normalize(person.getDate()),
        normalizeRole(person.getRole().toLowerCase()),
        normalizeName(person.getName())
    );
  }

  private String normalizeRole(final String value) {
    return value.replaceAll("główna księgowa", "główny księgowy")
        .replaceAll("zarzadu", "zarządu")
        .replaceAll("wieceprezes", "wiceprezes")
        .replaceAll("wiceprezez", "wiceprezes")
        .replace("czlonek", "członek")
        .replaceAll(" (spółki|banku)", "")
        .replaceAll(" (zarządu)", "")
        .replaceAll(" ds[.]", "")
        .replaceAll(",[^_]+", "");
  }

  private String normalizeName(final String value) {
    return value.replaceAll("[ ]*-[ ]*", "-");
  }
}
