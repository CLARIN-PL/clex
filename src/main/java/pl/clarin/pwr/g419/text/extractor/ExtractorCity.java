package pl.clarin.pwr.g419.text.extractor;

import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.struct.AnnotationList;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.text.annotator.AnnotatorCity;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPostalCode;
import java.util.Optional;

@Slf4j
public class ExtractorCity implements IExtractor<FieldContext<String>> {

  @Override
  public Optional<FieldContext<String>> extract(final HocrDocument document) {
    return getCity(document);
  }


  private Optional<FieldContext<String>> getCity(final HocrDocument document) {

    document.getAllPagesAnnotations()
        .filterByType(AnnotatorCity.CITY).forEach(an -> an.calculateScore(null));

    AnnotationList cityCandidates = document.getAllPagesAnnotations()
        .filterByType(AnnotatorCity.CITY);

    final AnnotationList firstPage = cityCandidates.filterByPageNo(1);
    if (firstPage.size() > 0) {
      cityCandidates = firstPage;
    }

    Optional<FieldContext<String>> result = cityCandidates
        .topScore()
        .sortByPos()
        .getFirst()
        .map(vc -> new FieldContext<>(vc.getField(), vc.getContext(), vc.getRule()));

    if (result.isPresent()) {
      document.getDocContextInfo().setPageWithFoundCity(result.get().getPage());
      document.getDocContextInfo().setFoundCity(result.get().getField());
    }

    return result;
  }


}

