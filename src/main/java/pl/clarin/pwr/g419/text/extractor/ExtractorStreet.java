package pl.clarin.pwr.g419.text.extractor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.kbase.lexicon.CompanyLexicon;
import pl.clarin.pwr.g419.struct.AnnotationList;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.text.annotator.AnnotatorCompany;
import pl.clarin.pwr.g419.text.annotator.AnnotatorStreet;
import pl.clarin.pwr.g419.text.lemmatizer.CompanyLemmatizer;
import pl.clarin.pwr.g419.text.normalization.NormalizerCompany;
import java.util.Date;
import java.util.Optional;

import static pl.clarin.pwr.g419.utils.DateUtils.parseDate;

@Slf4j
public class ExtractorStreet implements IExtractor<Pair<FieldContext<String>, Optional<FieldContext<String>>>> {

  @Override
  //public Optional<FieldContext<String>> extract(final HocrDocument document) {   return getStreet(document); }
  public Optional<Pair<FieldContext<String>, Optional<FieldContext<String>>>> extract(final HocrDocument document) {
    return getStreetAndNo(document);
  }

  private Optional<FieldContext<String>> getStreet(final HocrDocument document) {

    AnnotationList streetCandidates = document.getAnnotations()
        .filterByType(AnnotatorStreet.STREET);

    final AnnotationList firstPage = streetCandidates.filterByPageNo(1);
    if (firstPage.size() > 0) {
      streetCandidates = firstPage;
    }

    return streetCandidates
        .topScore()
        .sortByPos()
        .getFirst()
        .map(vc -> new FieldContext<>(vc.getField(), vc.getContext(), vc.getRule()));
  }

  private Optional<Pair<FieldContext<String>, Optional<FieldContext<String>>>>
  getStreetAndNo(final HocrDocument document) {

    AnnotationList streetCandidates = document.getAnnotations()
        .filterByType(AnnotatorStreet.STREET);

    final AnnotationList firstPage = streetCandidates.filterByPageNo(1);
    if (firstPage.size() > 0) {
      streetCandidates = firstPage;
    }

    Optional<FieldContext<String>> streetAndNo = streetCandidates
        .topScore()
        .sortByPos()
        .getFirst()
        .map(vc -> new FieldContext<>(vc.getField(), vc.getContext(), vc.getRule()));

    var resultStreetAndNo = decomposeStreetAndNo(streetAndNo);
    // przetwórzmy i zwrócmy
    return resultStreetAndNo;
  }

  private Optional<Pair<FieldContext<String>, Optional<FieldContext<String>>>>
  decomposeStreetAndNo(Optional<FieldContext<String>> streetAndNo) {
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

