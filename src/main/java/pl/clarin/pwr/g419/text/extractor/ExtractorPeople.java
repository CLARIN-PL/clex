package pl.clarin.pwr.g419.text.extractor;

import java.util.List;
import java.util.Optional;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.Person;

public class ExtractorPeople implements IExtractor<List<FieldContext<Person>>> {

  @Override
  public Optional<List<FieldContext<Person>>> extract(final HocrDocument document) {
    return Optional.empty();
  }

}
