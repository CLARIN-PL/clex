package pl.clarin.pwr.g419.struct;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = "page")
@ToString(exclude = "page")
public class HocrLine implements Contour {

  HocrPage page;

  // nazwy zmienione by pasowały do konwencji używanej w Boxach
  int topBound;
  int bottomBound;

  int firstBoxInRangeIndex;
  int lastBoxInRangeIndex;

  public HocrLine() {
  }

  public HocrLine(final int tBound, final int bBound) {
    topBound = tBound;
    bottomBound = bBound;
  }

  public HocrLine(final HocrPage p, final int tBound, final int bBound) {
    page = p;
    topBound = tBound;
    bottomBound = bBound;
  }

  public Bbox getFirstBBox() {
    return this.getPage().get(firstBoxInRangeIndex);
  }

  public Bbox getLastBBox() {
    return this.getPage().get(lastBoxInRangeIndex);
  }

  @Override
  public int getLeft() {
    return getFirstBBox().getBox().getLeft();
  }

  @Override
  public int getRight() {
    return getLastBBox().getBox().getRight();
  }

  @Override
  public int getTop() {
    return topBound;
  }

  @Override
  public int getBottom() {
    return bottomBound;
  }


  public double overlap(final HocrLine hocrLine) {
    if (hocrLine.bottomBound < topBound || hocrLine.topBound > bottomBound) {
      return 0.0;
    }
    final double top = Math.max(topBound, hocrLine.topBound);
    final double bottom = Math.min(bottomBound, hocrLine.bottomBound);
    final double length = Math.abs(bottom - top);
    final double maxLength = Math.max(getHeight(), hocrLine.getHeight());
    return length / maxLength;
  }

  public double within(final HocrLine hocrLine) {
    if (hocrLine.bottomBound < topBound || hocrLine.topBound > bottomBound) {
      return 0.0;
    }
    final double top = Math.max(topBound, hocrLine.topBound);
    final double bottom = Math.min(bottomBound, hocrLine.bottomBound);
    final double length = Math.abs(bottom - top);
    return length / getHeight();
  }

  public void merge(final int top, final int bottom) {
    topBound = Math.min(topBound, top);
    bottomBound = Math.max(bottomBound, bottom);
  }

  public String getText() {
    final StringBuilder sb = new StringBuilder();
    for (int i = getFirstBoxInRangeIndex(); i <= getLastBoxInRangeIndex(); i++) {
      sb.append(page.get(i).getText()).append(" ");
    }
    return sb.toString();
  }

  public boolean containsBboxWithIndex(final int index) {
    return (this.getFirstBoxInRangeIndex() <= index
        &&
        this.getLastBoxInRangeIndex() >= index);
  }

  public List<Bbox> getBboxes() {
    return page.subList(firstBoxInRangeIndex, lastBoxInRangeIndex + 1);
  }


}
