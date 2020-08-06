package pl.clarin.pwr.g419.text.extractor;

import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.struct.AnnotationList;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.text.annotator.AnnotatorDrawingDate;
import java.util.Date;
import java.util.Optional;

import static pl.clarin.pwr.g419.utils.DateUtils.parseDate;

@Slf4j
public class ExtractorDrawingDate implements IExtractor<FieldContext<Date>> {

  @Override
  public Optional<FieldContext<Date>> extract(final HocrDocument document) {
    return getDrawingDate(document);
  }

  private Optional<FieldContext<Date>> getDrawingDate(final HocrDocument document) {
    AnnotationList drawingDateCandidates = document.getAnnotations()
        .filterByType(AnnotatorDrawingDate.DRAWING_DATE);

    final AnnotationList firstPage = drawingDateCandidates.filterByPageNo(1);
    if (firstPage.size() > 0) {
      drawingDateCandidates = firstPage;
    }

    if (firstPage.size() == 0) {
      final AnnotationList signsPage = drawingDateCandidates
          .filterByPageNo(document.getDocContextInfo().getPageNrWithSigns());
      if (signsPage.size() > 0) {
        drawingDateCandidates = signsPage;
      }
    }

    return drawingDateCandidates
        .topScore()
        .sortByPos()
        .getFirst()
        .map(vc -> new FieldContext<>(parseDate(vc.getField()), vc.getContext(), vc.getRule()));
  }

}

