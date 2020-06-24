package pl.clarin.pwr.g419.text.extractor;

import java.util.Optional;
import pl.clarin.pwr.g419.struct.HocrDocument;

public interface IExtractor<T> {

  Optional<T> extract(final HocrDocument document);

}
