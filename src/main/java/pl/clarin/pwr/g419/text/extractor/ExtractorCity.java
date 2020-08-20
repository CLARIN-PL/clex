package pl.clarin.pwr.g419.text.extractor;

import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.struct.AnnotationList;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.text.annotator.AnnotatorCity;
import pl.clarin.pwr.g419.text.annotator.AnnotatorCityWithDate;
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


    //////// CityWithDate
    //if (result.isEmpty()) {

    //log.debug(" city value is empty ... Checking city_with_date");
    final AnnotationList cityWithDateAnnotationList = document.getAnnotations()
        .filterByType(AnnotatorCityWithDate.CITY_WITH_DATE);

    final int nrOfCityWithDateAnnotations = cityWithDateAnnotationList.size();

    final Optional<FieldContext<String>> cityWithDateAnn =
        cityWithDateAnnotationList
            .topScore()
            .sortByLocDesc()
            .getFirst();

    log.debug("city_with_date ann = " + cityWithDateAnn);

    FieldContext<String> city = new FieldContext<>();
    city.setPage(-1);

    if (cityWithDateAnn.isPresent()) {
      var vc = cityWithDateAnn.get();
      log.debug(" cityWithDate present vc.getPAge =  " + vc.getPage());
      // nie bierzemy getAllPages bo one są z nagłówkami // TODO - trzeba by to jednak zmienić
      if (vc.getPage() == document.size()) {

        String[] splitted = vc.getField().split(":");
        // TODO - tu trzeba wyciągnąć miasto z tej pary
        city.setField(splitted[0]);
        city.setContext(cityWithDateAnn.get().getContext());
        city.setPage(vc.getPage());
        document.getDocContextInfo().setPageNrWithSigns(city.getPage());
      }
    }

    if (city.getPage() == -1) {
      city.setField("");
      document.getDocContextInfo().setPageNrWithSigns(0);
    }

    //log.error("DOCID ; " + document.getId() + "; City result ; " + result.get().getField() + "; city ann ;  " + city.getField());

    if (!city.getField().isBlank()) {
      if (result.isPresent()) {
        String standardResult = result.get().getField().trim().toLowerCase();
        String annCityDateResult = city.getField().trim().toLowerCase();
        // TODO - do poprawki : albo wyeliminować regułę <data>,<miasto> (bo pewnie ona te przypadki dodaje)
        // TODO - albo obsługiwać specjalnie miasta z nazwami takie jak imiona własne - choć nie ma ich dużo jesli chodzi
        // TODO - o większe miasta
        if (!annCityDateResult.equals("jarosław")) {
          if (!(standardResult.equals(annCityDateResult))) {
            return Optional.of(city);
          }
        }
      } else {
        return Optional.of(city);
      }

    }

//    log.debug("returning  = " + city);
//    return Optional.of(city);

    //}
    // CityWithDate


    return result;
  }


}

