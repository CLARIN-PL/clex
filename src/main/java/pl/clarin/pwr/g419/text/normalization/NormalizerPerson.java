package pl.clarin.pwr.g419.text.normalization;

import pl.clarin.pwr.g419.struct.Person;

public class NormalizerPerson extends Normalizer<Person> {

  public String normalize(final Person person) {
    if (person == null) {
      return "";
    }
    return String.format("%s_%s_%s",
        //formatDate(person.getDate()),
        "",
        person.getRole().toLowerCase(),
        person.getName()
    );
  }

}
