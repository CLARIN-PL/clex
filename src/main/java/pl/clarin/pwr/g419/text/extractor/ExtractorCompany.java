package pl.clarin.pwr.g419.text.extractor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.kbase.lexicon.CompanyLexicon;
import pl.clarin.pwr.g419.struct.Annotation;
import pl.clarin.pwr.g419.struct.FieldContext;
import pl.clarin.pwr.g419.struct.HocrDocument;
import pl.clarin.pwr.g419.struct.HocrLine;
import pl.clarin.pwr.g419.text.annotator.AnnotatorCompany;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPeriod;
import pl.clarin.pwr.g419.text.lemmatizer.CompanyLemmatizer;
import pl.clarin.pwr.g419.text.normalization.NormalizerCompany;
import java.util.Date;
import java.util.Optional;

import static pl.clarin.pwr.g419.utils.DateUtils.parseDate;

@Slf4j
public class ExtractorCompany implements IExtractor<FieldContext<String>> {


  CompanyLexicon companyLexicon = new CompanyLexicon();
  NormalizerCompany companyNormalizer = new NormalizerCompany();
  CompanyLemmatizer companyLemmatizer = new CompanyLemmatizer();


  @Override
  public Optional<FieldContext<String>> extract(final HocrDocument document) {
    return getCompany(document);
  }

  private Optional<FieldContext<String>> getCompany(final HocrDocument document) {

    document.getAllPagesAnnotations()
        .filterByType(AnnotatorCompany.COMPANY).forEach(an -> an.calculateScore(null));

    final Optional<FieldContext<String>> value = document.getAllPagesAnnotations()
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

