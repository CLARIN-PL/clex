package pl.clarin.pwr.g419.text.normalization;

import java.util.Date;
import lombok.Data;
import pl.clarin.pwr.g419.struct.Person;

@Data
public class MetadataNormalizer {

  Normalizer<Date> date = new NormalizerDate();
  Normalizer<String> company = new NormalizerCompany();
  Normalizer<Person> person = new NormalizerPersonDateRole();
  Normalizer<String> signPage = new NormalizerSignPage();

  Normalizer<String> postalCode = new NormalizerString();
  Normalizer<String> city = new NormalizerStringToUpperCase();
  Normalizer<String> street = new NormalizerStringToUpperCase();
  Normalizer<String> streetNo = new NormalizerStringToUpperCaseCutPreLastSpace();


}
