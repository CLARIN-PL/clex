package pl.clarin.pwr.g419.text;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.kbase.lexicon.CompanyLexicon;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.*;
import pl.clarin.pwr.g419.text.extractor.ExtractorCompany;
import pl.clarin.pwr.g419.text.extractor.ExtractorPeople;
import pl.clarin.pwr.g419.text.extractor.ExtractorPeriod;
import pl.clarin.pwr.g419.text.extractor.ExtractorSignsPage;
import pl.clarin.pwr.g419.text.lemmatizer.CompanyLemmatizer;
import pl.clarin.pwr.g419.text.normalization.NormalizerCompany;

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
      new AnnotatorStreetNo(),
      new AnnotatorStreet()
  );

  AnnotatorStreet annotatorStreet = new AnnotatorStreet();


  ExtractorPeople extractorPeople = new ExtractorPeople();
  ExtractorSignsPage extractorSignsPage = new ExtractorSignsPage();
  ExtractorPeriod extractorPeriod = new ExtractorPeriod();
  ExtractorCompany extractorCompany = new ExtractorCompany();


  public MetadataWithContext extract(final HocrDocument document) {

    final LineHeightHistogram documentHistogram = new LineHeightHistogram(document);
    document.getDocContextInfo().setHistogram(documentHistogram);

    // wartość do wykorzystania przy znajdywaniu "dużych" linii ...
    final int mostCommonHeightOfLineInDocument = documentHistogram.findMostCommonHeightOfLine();
    document.getDocContextInfo().setMostCommonHeightOfLine(mostCommonHeightOfLineInDocument);

    log.debug(" Size of headers: " + document.getDocContextInfo().getHeaders().size());
    document.getDocContextInfo().getHeaders().stream()
        .forEach(header -> annotators.forEach(an -> an.annotate(header.getTmpPage())));
    document.getDocContextInfo().getHeaders().stream()
        .forEach(header -> header.getTmpPage().getAnnotations().stream()
            .forEach(ann -> log.debug("H:Ann =" + ann)));

    log.debug(" Size of footers: " + document.getDocContextInfo().getFooters().size());
    document.getDocContextInfo().getFooters().stream()
        .forEach(footer -> annotators.forEach(an -> an.annotate(footer.getTmpPage())));

    document.getDocContextInfo().getFooters().stream()
        .forEach(footer -> footer.getTmpPage().getAnnotations().stream()
            .forEach(ann -> log.debug("F:Ann =" + ann)));


    document.stream()
        .forEach(page -> annotators.forEach(an -> an.annotate(page)));


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
    getStreet(document).ifPresent(metadata::setStreet);
    getStreetNo(document).ifPresent(metadata::setStreetNo);


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
    AnnotationList postalCodeCandidates = document.getAnnotations()
        .filterByType(AnnotatorPostalCode.POSTAL_CODE);

    final AnnotationList firstPage = postalCodeCandidates.filterByPageNo(1);
    if (firstPage.size() > 0) {
      postalCodeCandidates = firstPage;
    }

    Optional<FieldContext<String>> result = postalCodeCandidates
        .topScore()
        .sortByPos()
        .getFirst()
        .map(vc -> new FieldContext<>(vc.getField(), vc.getContext(), vc.getRule()));

//    document.getDocContextInfo().setPageWithFoundPostalCode(result.get().getPage());
//    document.getDocContextInfo().setFoundPostalCode(result.get().getField());
    return result;
  }

  private Optional<FieldContext<String>> getCity(final HocrDocument document) {
    AnnotationList cityCandidates = document.getAnnotations()
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

//    document.getDocContextInfo().setPageWithFoundCity(result.get().getPage());
//    document.getDocContextInfo().setFoundCity(result.get().getField());

    return result;
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

  private Optional<FieldContext<String>> getStreetNo(final HocrDocument document) {

    AnnotationList streetNoCandidates = document.getAnnotations()
        .filterByType(AnnotatorStreet.STREET_NO);

    final AnnotationList firstPage = streetNoCandidates.filterByPageNo(1);
    if (firstPage.size() > 0) {
      streetNoCandidates = firstPage;
    }

    return streetNoCandidates
        .topScore()
        .sortByPos()
        .getFirst()
        .map(vc -> new FieldContext<>(vc.getField(), vc.getContext(), vc.getRule()));
  }


}
