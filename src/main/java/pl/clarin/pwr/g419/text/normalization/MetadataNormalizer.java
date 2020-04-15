package pl.clarin.pwr.g419.text.normalization;

import java.util.Date;
import lombok.Data;
import pl.clarin.pwr.g419.struct.Person;

@Data
public class MetadataNormalizer {

  Normalizer<Date> date = new NormalizerDate();
  Normalizer<String> company = new NormalizerCompany();
  //Normalizer<Person> person = new NormalizerPerson();
  Normalizer<Person> person = new NormalizerPersonRole();

}