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


    Optional<FieldContext<String>> companyFromHeader = document.getHeaderAndFooterAnnotations()
        .filterByType(AnnotatorCompany.COMPANY).getFirst();

    companyFromHeader.ifPresent(vc -> {
          final String nameLem = companyLemmatizer.lemmatize(vc.getField().toUpperCase());
          final String nameNorm = companyNormalizer.normalize(nameLem);
          final String nameApprox = companyLexicon.approximate(nameNorm);
          if (!nameNorm.equals(nameApprox)) {
            vc.setField(nameApprox);
            vc.setRule(String.format("%s; %s -> %s", vc.getRule(), nameNorm, nameApprox));
          } else {
            vc.setField(nameLem);
          }
        }
    );


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

    log.trace(" Company FromHeader=" + companyFromHeader);
    log.trace(" Company FromDocument=" + value);

    var resultFromHeader = companyFromHeader;
    var result = value;

    if (result.isEmpty() && resultFromHeader.isPresent()) {
      log.debug(" Niezgodność dla Company ! Nie ma dla dokuemntu jest dla nagłówków. Doc Id=" + document.getId());
      // normalne wyszukiwanie nie znalazło - wyszukiwanie po nagłówku znalazło - podstawiamy je

      result = resultFromHeader;
    } else if (result.isPresent() && resultFromHeader.isPresent()) {
      if (
          (result.get().getField().equals(resultFromHeader.get().getField()))
              && (result.get().getField().equals(resultFromHeader.get().getField()))
      ) {
        log.trace(" Zgodność dla Company z nagłówków i dokumentu !!! ");
      } else {
        log.debug(" Niezgodność dla Company ! Są obecne ale inne dla nagłówków a inne dla dokumentu.DOC ID=" + document.getId());
        // bierzemy tą z nagłówka
        result = resultFromHeader;
      }
    }

    return result;
  }


}

