package pl.clarin.pwr.g419.text.extractor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.clarin.pwr.g419.struct.*;
import pl.clarin.pwr.g419.text.annotator.AnnotatorPeriod;
import java.util.Date;
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

    document.getAllPagesAnnotations()
        .filterByType(AnnotatorPeriod.PERIOD).forEach(an -> an.calculateScore(this::additionalCalculatePeriodScore));

    Optional<FieldContext<String>> period = document.getAllPagesAnnotations()
        .filterByType(AnnotatorPeriod.PERIOD).topScore().sortByLoc().getFirst();

    var resultPeriod = getDatesPairFromPeriod(period);

    log.trace(" PeriodFromTotal=" + resultPeriod);
    return resultPeriod;
  }


  public int additionalCalculatePeriodScore(final Annotation a) {
    int leadingEmptyPages = a.getPage().getDocument().getDocContextInfo().getLeadingEmptyPages();
    if (a.getPage().getNo() == 1 + leadingEmptyPages) {
      return 200;  // jak na pierwszej stronie to jednak chyba najlepszy
    }
    if (a.getPage().getNo() == 2 + leadingEmptyPages) {
      return 100;  // jak na drugiej stronie to jednak chyba lepszy od tych z następnych
    }

    Optional<HocrLine> range = a.getLineFromLines();
    if (range.isEmpty())
      return 1;
    else
      return (range.get().getHeight());

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

