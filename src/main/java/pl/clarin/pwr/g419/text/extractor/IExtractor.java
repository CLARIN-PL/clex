package pl.clarin.pwr.g419.text.extractor;

import java.util.Optional;
import pl.clarin.pwr.g419.struct.AnnotationList;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPostalCode;

public interface IExtractor<T> {

  Optional<T> extract(final HocrDocument document);


  default Optional<FieldContext<String>> getFirstResult(HocrDocument document, String annotationName) {
    AnnotationList candidates = document.getAllPagesAnnotations()
        .filterByType(annotationName);

    final AnnotationList firstPage = candidates.filterByPageNo(1);
    if (firstPage.size() > 0) {
      candidates = firstPage;
    }

    Optional<FieldContext<String>> result = candidates
        .topScore()
        .sortByPos()
        .getFirst()
        .map(vc -> new FieldContext<>(vc.getField(), vc.getContext(), vc.getRule()));

    return result;
  }


}
