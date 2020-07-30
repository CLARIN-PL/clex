package pl.clarin.pwr.g419.text;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.*;
import pl.clarin.pwr.g419.text.extractor.*;

import static pl.clarin.pwr.g419.utils.DateUtils.parseDate;

@Slf4j
public class InformationExtractor implements HasLogger {

  List<Annotator> annotators = Lists.newArrayList(
      new AnnotatorDate(),
      new AnnotatorPeriod(),
      new AnnotatorCompanyPrefix(),
      new AnnotatorCompanySuffix(),
      new AnnotatorCompany(),
      new AnnotatorRole(),
      new AnnotatorPersonHorizontal(),
      new AnnotatorPersonVertical(),
      new AnnotatorDrawingDate(),
      new AnnotatorSignsPage(),

      new AnnotatorPostalCode(),
      new AnnotatorCity(),
      new AnnotatorStreetPrefix(),
      new AnnotatorStreet()
  );

  ExtractorPeople extractorPeople = new ExtractorPeople();
  ExtractorSignsPage extractorSignsPage = new ExtractorSignsPage();
  ExtractorPeriod extractorPeriod = new ExtractorPeriod();
  ExtractorCompany extractorCompany = new ExtractorCompany();
  ExtractorStreet extractorStreet = new ExtractorStreet();


  public MetadataWithContext extract(final HocrDocument document) {

    final LineHeightHistogram documentHistogram = new LineHeightHistogram(document);
    document.getDocContextInfo().setHistogram(documentHistogram);

    // wartość do wykorzystania przy znajdywaniu "dużych" linii ...
    final int mostCommonHeightOfLineInDocument = documentHistogram.findMostCommonHeightOfLine();
    document.getDocContextInfo().setMostCommonHeightOfLine(mostCommonHeightOfLineInDocument);

    log.debug(" XXXXXXXXXXXXXXXXXXX Annotating XXXXXXXXXXXXXXXXXXXXXXX");
    // poprzez wykorzystanie getAllPages annotujemy także sztucznie wygenerowane strony dla nagłówków i stopek
    document.getAllPages().stream()
        .forEach(page -> annotators.forEach(an -> an.annotate(page)));

    return extractFromAnnotationsToMetadata(document);
  }


  private MetadataWithContext extractFromAnnotationsToMetadata(HocrDocument document) {

    final MetadataWithContext metadata = new MetadataWithContext();

    extractorSignsPage.extract(document).ifPresent(metadata::setSignsPage);

    extractorPeriod.extract(document).ifPresent(p -> {
      metadata.setPeriodFrom(p.getLeft());
      metadata.setPeriodTo(p.getRight());
    });

    getDrawingDate(document).ifPresent(metadata::setDrawingDate);
    extractorCompany.extract(document).ifPresent(metadata::setCompany);

    extractorPeople.extract(document).ifPresent(metadata::setPeople);

    //assignDefaultSignDate(metadata);
    getPostalCode(document).ifPresent(metadata::setPostalCode);
    getCity(document).ifPresent(metadata::setCity);

    extractorStreet.extract(document).ifPresent(p -> {
      metadata.setStreet(p.getLeft());
      if (p.getRight().isPresent()) {
        metadata.setStreetNo(p.getRight().get());
      }
    });

    return metadata;
  }

  private void assignDefaultSignDate(final MetadataWithContext metadata) {
    if (metadata.getDrawingDate() != null) {
      metadata.getPeople().stream()
          .filter(f -> f.getField().getDate() == null)
          .forEach(f -> {
            f.getField().setDate(metadata.getDrawingDate().getField());
            f.setRule(f.getRule() + "; date=drawing_date");
          });
    }
  }


  private Optional<FieldContext<Date>> getDrawingDate(final HocrDocument document) {
    AnnotationList drawingDateCandidates = document.getAnnotations()
        .filterByType(AnnotatorDrawingDate.DRAWING_DATE);

    final AnnotationList firstPage = drawingDateCandidates.filterByPageNo(1);
    if (firstPage.size() > 0) {
      drawingDateCandidates = firstPage;
    }

    return drawingDateCandidates
        .topScore()
        .sortByPos()
        .getFirst()
        .map(vc -> new FieldContext<>(parseDate(vc.getField()), vc.getContext(), vc.getRule()));
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
