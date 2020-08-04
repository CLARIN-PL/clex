package pl.clarin.pwr.g419.text.normalization;

import pl.clarin.pwr.g419.struct.Person;

public class NormalizerPersonDate extends Normalizer<Person> {

  NormalizerDate normalizerDate = new NormalizerDate();

  public String normalize(final Person person) {
    if (person == null) {
      return "";
    }
    return String.format("%s_%s",
        normalizerDate.normalize(person.getDate()),
        normalizeName(person.getName())
    );
  }

  private String normalizeName(final String value) {
    return value.replaceAll("[ ]*-[ ]*", "-");
  }
}
