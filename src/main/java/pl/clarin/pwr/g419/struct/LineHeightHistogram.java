package pl.clarin.pwr.g419.struct;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public class LineHeightHistogram {

  private final Map<Integer, Set<Pair<Integer, Integer>>> data = new HashMap<>();

  public LineHeightHistogram(final HocrPage page) {
    this.buildHistogramOfLinesHeightsForPage(page);
  }

  public LineHeightHistogram(final HocrDocument document) {
    this.buildHistogramOfLinesHeightsForDocument(document);
  }


  private void buildHistogramOfLinesHeightsForPage(final HocrPage page) {

    final List<HocrLine> lines = page.getLines();

    for (int lineNr = 0; lineNr < page.lines.size(); lineNr++) {
      final HocrLine line = lines.get(lineNr);
      Set<Pair<Integer, Integer>> lineNrs = this.data.get(line.getHeight());
      if (lineNrs == null) {
        lineNrs = new HashSet<>();
        this.data.put(line.getHeight(), lineNrs);
      }
      lineNrs.add(Pair.of(page.getNo(), lineNr));
    }
    ;
  }

  private void buildHistogramOfLinesHeightsForDocument(final HocrDocument document) {
    document.stream()
        .forEach(page ->
        {
          final LineHeightHistogram pageHistogram = new LineHeightHistogram(page);
          pageHistogram.data.forEach((k, v) -> this.data.merge(k, v, (v1, v2) -> {
            v2.addAll(v1);
            return v2;
          }));
        });
  }

  public int findMostCommonHeightOfLine() {
    return this.data.entrySet().stream().max((entry1, entry2) -> entry1.getValue().size() > entry2.getValue().size() ? 1 : -1).get().getKey();
  }


  //------------------ diagnostyka -----------------------------------


  public void printHistogramOfLinesHeightsForDoc(
      final Map<Integer, Set<Pair<Integer, Integer>>> histogram) {
    final List<Integer> keys = new LinkedList<>(histogram.keySet());
    keys.sort((o1, o2) -> o1 < o2 ? -1 : 1);
    log.debug(" ---------- Lines Heights histogram for document ");

    keys.stream().forEach(key ->
        log.debug(" Key : " + key + "  counter: " + histogram.get(key).size() + " [" + histogram.get(key).stream().limit(5).map(v -> v.toString()).collect(Collectors.joining()) + " ]")
    );
  }

  public void printLinesWithGivenHeigth(final int height,
                                        final Set<Pair<Integer, Integer>> lines, final HocrDocument document) {
    lines.stream().forEach(pair ->
        {
          final HocrPage page = document.get(pair.getLeft() - 1); // !! W pair jest numer strony a nie indeks tablicy
          final List<HocrLine> linesOfPage = page.getLines();
          final int pageNumber = pair.getRight();
          final String text = linesOfPage.get(pageNumber).getText();

          log.info("Doc:" + document.getId() + " Wysokość: " + height + " Strona: " + page.getNo() + " linia: " + (pair.getRight() + 1) + " : " + text);
        }
    );
  }

  public void printLinesWithHeightBiggerThanMostCommon(
      final Map<Integer, Set<Pair<Integer, Integer>>> histogram,
      final HocrDocument document) {
    log.debug(" --- Document: " + document.getId());
    final int mostCommonHeightOfLine = document.getDocContextInfo().getHistogram().findMostCommonHeightOfLine();

    final List<Integer> keys = new LinkedList<>(histogram.keySet());
    keys.sort((o1, o2) -> o1 < o2 ? -1 : 1);
    keys.stream().filter(n -> n > mostCommonHeightOfLine).forEach(key -> printLinesWithGivenHeigth(key, histogram.get(key), document));

  }


  public void printHistogramOfLinesHeightsForPage(final HocrPage page) {
    final LineHeightHistogram histogram = new LineHeightHistogram(page);

    final List<Integer> keys = new LinkedList<>(histogram.data.keySet());
    keys.sort((o1, o2) -> o1 < o2 ? -1 : 1);
    log.debug(" ---------- Lines Heights histogram for page " + page.getNo());
    keys.stream().forEach(key -> log.debug(" Key : " + key + "  counter: " + histogram.data.get(key).size()));
  }


}
