package pl.clarin.pwr.g419.text.extractor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.io.reader.HeadersAndFootersHandler;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPeriod;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static pl.clarin.pwr.g419.utils.DateUtils.parseDate;

@Slf4j
public class ExtractorPeriod implements IExtractor<Pair<FieldContext<Date>, FieldContext<Date>>> {

  @Override
  public Optional<Pair<FieldContext<Date>, FieldContext<Date>>> extract(final HocrDocument document) {
    return getPeriod(document);
  }


  private Optional<Pair<FieldContext<Date>, FieldContext<Date>>> getPeriod(
      final HocrDocument document) {

    // TODO - doszlifować wyciąganie i sortowanie annotacji z nagłówków i stopek
    // TODO - np. wg liczby stron na których jest, kolejności w dokumencie, poziomu w drzewie nagłówków
    Optional<FieldContext<String>> periodFromHeader = document.getHeaderAndFooterAnnotations()
        .filterByType(AnnotatorPeriod.PERIOD).getFirst();

    var resultPeriodFromHeader = getDatesPairFromPeriod(periodFromHeader);


//    document.getAllPagesAnnotations()
//        .filterByType(AnnotatorPeriod.PERIOD).forEach(this::calculatePeriodScore);
    document.getAnnotations()
        .filterByType(AnnotatorPeriod.PERIOD).forEach(this::calculatePeriodScore);
/*
    log.debug("================================ just: ");
    document.getAnnotations()
        .filterByType(AnnotatorPeriod.PERIOD)
        .stream().forEach(a -> log.debug(" AnnPeriod: " + a.toFullInfo()));

    log.debug("================================ topScore: ");
    document.getAnnotations()
        .filterByType(AnnotatorPeriod.PERIOD)
        .topScore()
        .stream().forEach(a -> log.debug(" AnnPeriod: " + a.toFullInfo()));

    log.debug("================================ sortByLoc: ");
    document.getAnnotations()
        .filterByType(AnnotatorPeriod.PERIOD)
        .topScore()
        .sortByLoc()
        .stream().forEach(a -> log.debug(" AnnPeriod: " + a.toFullInfo()));
*/

    Optional<FieldContext<String>> period = document.getAnnotations()
        .filterByType(AnnotatorPeriod.PERIOD)
        .topScore().sortByLoc().getFirst();

    var result = getDatesPairFromPeriod(period);


    log.trace(" PeriodFromHeader=" + resultPeriodFromHeader);
    log.trace(" PeriodFromDocument=" + result);

    if (result.isEmpty() && resultPeriodFromHeader.isPresent()) {
      log.debug(" Niezgodność dla Period ! Nie ma dla dokuemntu jest dla nagłówków. Doc Id=" + document.getId());
      // normalne wyszukiwanie nie znalazło - wyszukiwanie po nagłówku znalazło - podstawiamy je
      result = resultPeriodFromHeader;
    } else if (result.isPresent() && resultPeriodFromHeader.isPresent()) {
      if (
          (result.get().getKey().getField().equals(resultPeriodFromHeader.get().getKey().getField()))
              && (result.get().getValue().getField().equals(resultPeriodFromHeader.get().getValue().getField()))
      ) {
        log.trace(" Zgodność dla Period z nagłówków i dokumentu !!! ");
      } else {
        log.debug(" Niezgodność dla Period ! Są obecne ale inne dla nagłówków a inne dla dokumentu.DOC ID=" + document.getId());
        // bierzemy tą z nagłówka
        result = resultPeriodFromHeader;
      }
    }


    return result;
  }

  private void calculatePeriodScore(Annotation a) {
    if (a.getPage().getNo() > HeadersAndFootersHandler.TMP_PAGE_NR_OFFSET_FOR_FOOTERS) {
      a.setScore(1000);
    } else {
      int leadingEmptyPages = a.getPage().getDocument().getDocContextInfo().getLeadingEmptyPages();
      if (a.getPage().getNo() == 1 + leadingEmptyPages) {
        a.setScore(200);  // jak na pierwszej stronie to jednak chyba najlepszy
        return;
      }
      if (a.getPage().getNo() == 2 + leadingEmptyPages) {
        a.setScore(100);  // jak na drugiej stronie to jednak chyba lepszy od tych z następnych
        return;
      }

      Optional<HocrLine> range = a.getLineFromLines();
      if (range.isEmpty())
        a.setScore(1);
      else
        a.setScore(range.get().getHeight());
    }

  }


  private Optional<Pair<FieldContext<Date>, FieldContext<Date>>> getDatesPairFromPeriod(Optional<FieldContext<String>> period) {
    if (period.isPresent()) {
      final String[] parts = period.get().getField().split(":");
      if (parts.length == 2) {
        final FieldContext<Date> periodStart = new FieldContext<>(
            parseDate(parts[0]), period.get().getContext(), period.get().getRule()
        );
        final FieldContext<Date> periodEnd = new FieldContext<>(
            parseDate(parts[1]), period.get().getContext(), period.get().getRule()
        );
        return Optional.of(new ImmutablePair<>(periodStart, periodEnd));
      }
    }
    return Optional.empty();
  }


}

