package pl.clarin.pwr.g419.struct;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.java.Log;

@Data
@Log
public class HocrPage extends Bboxes {

  int no;
  Annotations annotations = new Annotations();
  List<Range> lines;

  public HocrPage() {
  }

  public HocrPage(final List<Bbox> bboxes) {
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
    return lines.stream().map(line -> line.getText(this)).collect(Collectors.toList());
  }

  public void sortLinesByTop() {
    Collections.sort(lines, (o1, o2) -> (o1.getUpperBound() == o2.getUpperBound() ? 0 : (o1.getUpperBound() < o2.getUpperBound() ? -1 : 1)));
  }


  // ----------------------------------- diagnostyka ------------------------------

  public void dumpTextLinesFromBBoxes() {
    final List<String> linesFromBBoxes = getTextLinesFromBBoxes();
    for (int i = 0; i < linesFromBBoxes.size(); i++) {
      log.info(" Line(BBox) nr " + i + " is: '" + linesFromBBoxes.get(i) + "'");
    }
  }

  public void dumpTextLinesFromMergedLines() {
    final List<String> linesFromMergedLines = getTextLinesFromMergedLines();
    for (int i = 0; i < linesFromMergedLines.size(); i++) {
      log.info(" Line(mergedLine) nr " + i + " is: '" + linesFromMergedLines.get(i) + "'");
    }
  }

  public boolean verifyLinesStructsAreCorrectlySynchronized() {
    final List<String> linesFromMergedLines = getTextLinesFromMergedLines();
    final List<String> linesFromBBoxes = getTextLinesFromBBoxes();
    if (linesFromBBoxes.size() != linesFromMergedLines.size()) {
      log.info(" page " + getNo() + " lines structures have different size: " + linesFromBBoxes.size() + " vs " + linesFromMergedLines.size());
      return false;
    }

    boolean outOfSync = false;
    for (int i = 0; i < getTextLinesFromMergedLines().size(); i++) {
      if (!linesFromBBoxes.get(i).equals(linesFromMergedLines.get(i))) {
        log.info(" page " + getNo() + " niezgodność w lini nr " + i);
        outOfSync = true;
      }
    }

    if (!outOfSync) {
      log.info(" page " + getNo() + " linie zgodnie");
      return true;
    }

    return false;
  }


}
