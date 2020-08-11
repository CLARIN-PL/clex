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
      // kolejność ma znaczenie 
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

      new AnnotatorStreetNrLok(),
      new AnnotatorStreetNameStartAbbreviation(),
      new AnnotatorStreetOnly(),
      new AnnotatorStreet(),
//
      new AnnotatorHeadQuarters()
  );

  ExtractorPeople extractorPeople = new ExtractorPeople();
  ExtractorSignsPage extractorSignsPage = new ExtractorSignsPage();
  ExtractorPeriod extractorPeriod = new ExtractorPeriod();
  ExtractorCompany extractorCompany = new ExtractorCompany();
  ExtractorStreet extractorStreet = new ExtractorStreet();
  ExtractorPostalCode extractorPostalCode = new ExtractorPostalCode();
  ExtractorCity extractorCity = new ExtractorCity();
  ExtractorDrawingDate extractorDrawingDate = new ExtractorDrawingDate();


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
    extractorDrawingDate.extract(document).ifPresent(metadata::setDrawingDate);
    extractorCompany.extract(document).ifPresent(metadata::setCompany);
    extractorPeople.extract(document).ifPresent(metadata::setPeople);
    extractorPostalCode.extract(document).ifPresent(metadata::setPostalCode);
    extractorCity.extract(document).ifPresent(metadata::setCity);
    extractorStreet.extract(document).ifPresent(p -> {
      metadata.setStreet(p.getLeft());
      if (p.getRight().isPresent()) {
        metadata.setStreetNo(p.getRight().get());
      }
    });

    assignDefaultSignDate(metadata);

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


}
