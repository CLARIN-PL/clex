package pl.clarin.pwr.g419.struct;

import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@EqualsAndHashCode(exclude = {"document", "annotations"})
@ToString(exclude = "document")
public class HocrPage extends Bboxes implements Contour {

  int no;
  Annotations annotations = new Annotations();
  List<HocrLine> lines = new ArrayList<>();
  HocrDocument document;
  int numberOfOriginalLines;
  int numberOfOriginalBlocks;
  int numberOfOriginalInlineBlocks;
  Box box;

  LineHeightHistogram histogram;


  public HocrPage(final HocrDocument doc) {
    this.document = doc;
  }

  public HocrPage(final HocrDocument doc, final List<Bbox> bboxes) {
    this(doc);
    this.addAll(bboxes);
  }

  public void addAnnotation(final Annotation annotation) {
    this.annotations.add(annotation);
  }

  public AnnotationList getAnnotationList() {
    return new AnnotationList(annotations);
  }


  public List<String> getTextLinesFromBBoxes() {
    final List<String> result = new LinkedList<>();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < size(); i++) {
      final Bbox bb = get(i);
      sb.append(bb.getText()).append(" ");
      if (bb.isLineEnd()) {
        result.add(sb.toString());
        sb = new StringBuilder();
      }
    }
    return result;
  }

  public List<String> getTextLinesFromMergedLines() {
    return lines.stream().map(line -> line.getText()).collect(Collectors.toList());
  }

  public void sortLinesByTop() {
    Collections.sort(lines, (o1, o2) -> (o1.getBottomBound() == o2.getBottomBound() ? 0 : (o1.getBottomBound() < o2.getBottomBound() ? -1 : 1)));
  }

  public Bboxes generateBboxesFromSortedLines() {
    List<Bbox> result = lines.stream().flatMap(line -> line.getBboxes().stream()).collect(Collectors.toList());
    return new Bboxes(result);
  }

  public List<Integer> findBBoxesAboveBBox(int bboxNr, int howMany) {
    Bbox referencedBbox = this.get(bboxNr);
    List<Integer> bboxesAbove = new LinkedList<>();
    int counter = 0;

    for (int i = bboxNr - 1; i >= 0 && counter < howMany; i--) {
      if (this.get(i).overlapX(referencedBbox) > 0) {
        bboxesAbove.add(i);
        counter++;
      }
    }
    return bboxesAbove;
  }

  public List<Integer> findBBoxesBelowBBox(int bboxNr, int howMany) {
    Bbox referencedBbox = this.get(bboxNr);
    List<Integer> bboxesBelow = new LinkedList<>();
    int counter = 0;

    for (int i = bboxNr + 1; i < this.size() && counter < howMany; i++) {
      if (this.get(i).overlapX(referencedBbox) > 0) {
        bboxesBelow.add(i);
        counter++;
      }
    }
    return bboxesBelow;
  }

  public Optional<Integer> findLinesNrForBboxIndex(int bboxIndex) {
    for (int i = 0; i < lines.size(); i++)
      if (lines.get(i).containsBboxWithIndex(bboxIndex))
        return Optional.of(i);

    return Optional.empty();
  }


  // ----------------------------------- diagnostyka ------------------------------

  public void dumpTextLinesFromBBoxes() {
    final List<String> linesFromBBoxes = getTextLinesFromBBoxes();
    for (int i = 0; i < linesFromBBoxes.size(); i++) {
      log.debug(" Line(BBox) nr " + i + " is: '" + linesFromBBoxes.get(i) + "' > " + lines.get(i).toCoords());
    }
  }

  public void dumpTextLinesFromMergedLines() {
    final List<String> linesFromMergedLines = getTextLinesFromMergedLines();
    for (int i = 0; i < linesFromMergedLines.size(); i++) {
      log.debug(" Line(mergedLine) nr " + i + " is: '" + linesFromMergedLines.get(i) + "' > " + lines.get(i).toCoords());
    }
  }

  public boolean verifyLinesStructsAreCorrectlySynchronized() {
    final List<String> linesFromMergedLines = getTextLinesFromMergedLines();
    final List<String> linesFromBBoxes = getTextLinesFromBBoxes();
    if (linesFromBBoxes.size() != linesFromMergedLines.size()) {
      log.debug(" page " + getNo() + " lines structures have different size: " + linesFromBBoxes.size() + " vs " + linesFromMergedLines.size());
      return false;
    }

    boolean outOfSync = false;
    for (int i = 0; i < getTextLinesFromMergedLines().size(); i++) {
      if (!linesFromBBoxes.get(i).equals(linesFromMergedLines.get(i))) {
        log.debug(" page " + getNo() + " niezgodność w lini nr " + i);
        outOfSync = true;
      }
    }

    if (!outOfSync) {
      log.debug(" page " + getNo() + " linie zgodnie");
      return true;
    }

    return false;
  }

  public void dumpNrOfLinesAndBlocks() {
    log.debug(" [p=" + getNo() + "] lines= " + getNumberOfOriginalLines() + "  blocks=" + getNumberOfOriginalBlocks() + "  " + getNumberOfOriginalInlineBlocks());
  }

  public double isWithTableProb() {
    return 2 * getNumberOfOriginalInlineBlocks() / getNumberOfOriginalBlocks();
  }

  @Override
  public int getLeft() {
    return getBox().getLeft();
  }

  @Override
  public int getRight() {
    return getBox().getRight();
  }

  @Override
  public int getTop() {
    return getBox().getTop();
  }

  @Override
  public int getBottom() { return getBox().getBottom(); }


}
