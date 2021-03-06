package pl.clarin.pwr.g419.struct;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import pl.clarin.pwr.g419.io.reader.HeadersAndFootersHandler;

@Slf4j
@Data

public class Annotation {

  String type;
  HocrPage page;
  int indexBegin;
  int indexEnd;
  Optional<String> norm = Optional.empty();
  int score;
  String source = "";

  public Annotation(final String type, final HocrPage page,
                    final int indexBegin, final int indexEnd) {
    this.type = type;
    this.page = page;
    this.indexBegin = indexBegin;
    this.indexEnd = indexEnd;
  }

  public Annotation withNorm(final String norm) {
    this.norm = Optional.of(norm);
    return this;
  }

  public Annotation withScore(final int score) {
    this.score = score;
    return this;
  }

  public Annotation withSource(final String source) {
    this.source = source;
    return this;
  }

  public int getLength() {
    return indexEnd - indexBegin;
  }

  public String getText() {
    return IntStream.range(indexBegin, indexEnd).mapToObj(page::get).map(Bbox::getText).
        collect(Collectors.joining(" "));
  }

  public String getWholeLineText() {
    int startLineIndex = indexBegin;
    while (!page.get(startLineIndex).isLineBegin()) {
      startLineIndex--;
    }

    int endLineIndex = indexBegin;
    while (!page.get(endLineIndex).isLineEnd()) {
      endLineIndex++;
    }

    return IntStream.range(startLineIndex, endLineIndex).mapToObj(page::get).map(Bbox::getText).
        collect(Collectors.joining(" "));
  }

  public String getNorm() {
    return norm.orElse(getText());
  }

  public Bbox getFirst() {
    return page.get(indexBegin);
  }

  public String getContext() {
    return String.format("[p=%d] %s", this.getPage().getNo(), this.getText());
  }

  @Override
  public String toString() {
    return String.format("[%s] %s", type, getText());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Annotation that = (Annotation) o;
    return indexBegin == that.indexBegin &&
        indexEnd == that.indexEnd &&
        Objects.equals(type, that.type) &&
        Objects.equals(page, that.page);
  }

  public Contour getContour() {

    HocrLine r = new HocrLine();
    r.setPage(page);
    r.setFirstBoxInRangeIndex(this.getIndexBegin());
    r.setLastBoxInRangeIndex(this.getIndexEnd());
    r.setTopBound(page.stream().skip(indexBegin).limit(indexEnd - indexBegin).map(b -> b.getTop()).min(Comparator.naturalOrder()).get());
    r.setBottomBound(page.stream().skip(indexBegin).limit(indexEnd - indexBegin).map(b -> b.getBottom()).max(Comparator.naturalOrder()).get());

    return r;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, indexBegin, indexEnd);
  }


  public String toFullInfo() {
    return toString() + " - page: " + page.getNo() + " - indStart = " + getIndexBegin();
  }

  public Optional<HocrLine> getLineFromLines() {
    Optional<Integer> lineNr = getPage().findLinesNrForBboxIndex(this.indexBegin);
    if (lineNr.isEmpty())
      return Optional.empty();
    else
      return Optional.of(getPage().getLines().get(lineNr.get()));
  }

  /***
   * Wylicza score dla tej annotacji. Może przyjąć dodatkową, inną metodę wyliczania "z zewnątrz" i użyć jej
   * @param additionalCalcScoreFunction
   * @return
   */
  public int calculateScore(Function<Annotation, Integer> additionalCalcScoreFunction) {
    if (this.getPage().getNo() > HeadersAndFootersHandler.TMP_PAGE_NR_OFFSET_FOR_FOOTERS) {
      this.setScore(1000);
    } else {
      if (additionalCalcScoreFunction != null) {
        int calculatedScore = additionalCalcScoreFunction.apply(this);
        this.setScore(calculatedScore);
      }
    }
    log.trace("Final score :" + getScore() + " for Annotation " + this);
    return getScore();
  }

}
