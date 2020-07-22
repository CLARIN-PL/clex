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
      new AnnotatorSignsPage()
  );

  CompanyLexicon companyLexicon = new CompanyLexicon();
  NormalizerCompany companyNormalizer = new NormalizerCompany();
  CompanyLemmatizer companyLemmatizer = new CompanyLemmatizer();


  ExtractorPeople extractorPeople = new ExtractorPeople();
  ExtractorSignsPage extractorSignsPage = new ExtractorSignsPage();
  ExtractorPeriod extractorPeriod = new ExtractorPeriod();

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
    getCompany(document).ifPresent(metadata::setCompany);

    extractorPeople.extract(document).ifPresent(metadata::setPeople);

    //assignDefaultSignDate(metadata);

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

  private Optional<FieldContext<String>> getCompany(final HocrDocument document) {
    final Optional<FieldContext<String>> value = document.getAnnotations()
        .filterByType(AnnotatorCompany.COMPANY)
        .topScore()
        .sortByLoc()
        .getFirst();
    value.ifPresent(vc -> {
      final String nameLem = companyLemmatizer.lemmatize(vc.getField().toUpperCase());
      final String nameNorm = companyNormalizer.normalize(nameLem);
      final String nameApprox = companyLexicon.approximate(nameNorm);
      if (!nameNorm.equals(nameApprox)) {
        vc.setField(nameApprox);
        vc.setRule(String.format("%s; %s -> %s", vc.getRule(), nameNorm, nameApprox));
      } else {
        vc.setField(nameLem);
      }
    });
    return value;
  }


}
