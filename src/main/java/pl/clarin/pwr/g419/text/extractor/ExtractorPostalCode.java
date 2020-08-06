package pl.clarin.pwr.g419.text.extractor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPeriod;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPostalCode;
import java.util.Date;
import java.util.Optional;

import static pl.clarin.pwr.g419.utils.DateUtils.parseDate;

@Slf4j
public class ExtractorPostalCode implements IExtractor<FieldContext<String>> {

  @Override
  public Optional<FieldContext<String>> extract(final HocrDocument document) {
    return getPostalCode(document);
  }


  private Optional<FieldContext<String>> getPostalCode(final HocrDocument document) {

    document.getAllPagesAnnotations()
        .filterByType(AnnotatorPostalCode.POSTAL_CODE).forEach(an -> an.calculateScore(null));

    AnnotationList postalCodeCandidates = document.getAllPagesAnnotations()
        .filterByType(AnnotatorPostalCode.POSTAL_CODE);

    // TODO - sprawdzić tu i w innych podobnych miejscach czy firstPage nie ma konflikótw z pierwszym z nagłówka/stopki
    final AnnotationList firstPage = postalCodeCandidates.filterByPageNo(1);
    if (firstPage.size() > 0) {
      postalCodeCandidates = firstPage;
    }

    Optional<FieldContext<String>> result = postalCodeCandidates
        .topScore()
        .sortByPos()
        .getFirst()
        .map(vc -> new FieldContext<>(vc.getField(), vc.getContext(), vc.getRule()));

    if (result.isPresent()) {
      document.getDocContextInfo().setPageWithFoundPostalCode(result.get().getPage());
      document.getDocContextInfo().setFoundPostalCode(result.get().getField());
    }
    return result;
  }


}

