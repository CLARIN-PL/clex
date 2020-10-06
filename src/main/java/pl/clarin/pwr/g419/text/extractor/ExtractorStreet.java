package pl.clarin.pwr.g419.text.extractor;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.struct.AnnotationList;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.text.annotator.AnnotatorCity;
import pl.clarin.pwr.g419.text.annotator.AnnotatorStreet;

@Slf4j
public class ExtractorStreet implements IExtractor<Pair<FieldContext<String>, Optional<FieldContext<String>>>> {

  //StreetLemmatizer streetLemmatizer = new StreetLemmatizer();

  @Override
  public Optional<Pair<FieldContext<String>, Optional<FieldContext<String>>>> extract(final HocrDocument document) {
    return getStreetAndNo(document);
  }

  private Optional<Pair<FieldContext<String>, Optional<FieldContext<String>>>>
  getStreetAndNo(final HocrDocument document) {

    document.getAllPagesAnnotations()
        .filterByType(AnnotatorStreet.STREET).forEach(an -> an.calculateScore(null));

    Optional<FieldContext<String>> result = getFirstResult(document, AnnotatorStreet.STREET);

    final var resultStreetAndNo = decomposeStreetAndNo(result);
    // przetwórzmy i zwrócmy
    return resultStreetAndNo;
  }

  private Optional<Pair<FieldContext<String>, Optional<FieldContext<String>>>>
  decomposeStreetAndNo(final Optional<FieldContext<String>> streetAndNo) {
    if (streetAndNo.isPresent()) {
      final String[] parts = streetAndNo.get().getField().split(":");
      if (parts.length >= 1) {
        final FieldContext<String> street = new FieldContext<>(
            parts[0], streetAndNo.get().getContext(), streetAndNo.get().getRule()
        );
        if (parts.length == 2) {
          final FieldContext<String> streetNo = new FieldContext<>(
              parts[1], streetAndNo.get().getContext(), streetAndNo.get().getRule()
          );
          return Optional.of(new ImmutablePair<>(street, Optional.of(streetNo)));
        }
        return Optional.of(new ImmutablePair<>(street, Optional.empty()));
      }
    }
    return Optional.empty();
  }

}

