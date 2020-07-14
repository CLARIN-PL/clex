package pl.clarin.pwr.g419.struct;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = "page")
@ToString(exclude = "page")
public class Range {

  HocrPage page;

  // nazwy zmienione by pasowały do konwencji używanej w Boxach
  int topBound;
  int bottomBound;

  int firstBoxInRangeIndex;
  int lastBoxInRangeIndex;

  public Range(final int tBound, final int bBound) {
    topBound = tBound;
    bottomBound = bBound;
  }


  public Range(final HocrPage p, final int tBound, final int bBound) {
    page = p;
    topBound = tBound;
    bottomBound = bBound;
  }


  public double overlap(final Range range) {
    if (range.bottomBound < topBound || range.topBound > bottomBound) {
      return 0.0;
    }
    final double top = Math.max(topBound, range.topBound);
    final double bottom = Math.min(bottomBound, range.bottomBound);
    final double length = Math.abs(bottom - top);
    final double maxLength = Math.max(getLength(), range.getLength());
    return length / maxLength;
  }

  public double within(final Range range) {
    if (range.bottomBound < topBound || range.topBound > bottomBound) {
      return 0.0;
    }
    final double top = Math.max(topBound, range.topBound);
    final double bottom = Math.min(bottomBound, range.bottomBound);
    final double length = Math.abs(bottom - top);
    return length / getLength();
  }

  public void merge(final int top, final int bottom) {
    topBound = Math.min(topBound, top);
    bottomBound = Math.max(bottomBound, bottom);
  }

  public int getLength() {
    return bottomBound - topBound;
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

  public String toCoords() {
    return " [t:" + topBound + ",b:" + bottomBound + "] " +
        "[l:" + getFirstBBox().getBox().getLeft() + ",r:" + getLastBBox().getBox().getRight() + "]";
  }

  public Bbox getFirstBBox() {
    return this.getPage().get(firstBoxInRangeIndex);
  }

  public Bbox getLastBBox() {
    return this.getPage().get(lastBoxInRangeIndex);
  }


  public double overlapX(final Range range) {
    if ((this.getLastBBox().getBox().getRight() < range.getFirstBBox().getBox().getLeft())
        ||
        (range.getLastBBox().getBox().getRight() < this.getFirstBBox().getBox().getLeft())) {
      return 0;
    }

    final double outerLeft = Math.min(this.getFirstBBox().getBox().getLeft(), range.getFirstBBox().getBox().getLeft());
    final double outerRight = Math.max(this.getLastBBox().getBox().getRight(), range.getLastBBox().getBox().getRight());
    final double outerWidth = Math.abs(outerRight - outerLeft);

    final double innerLeft = Math.max(this.getFirstBBox().getBox().getLeft(), range.getFirstBBox().getBox().getLeft());
    final double innerRight = Math.min(this.getLastBBox().getBox().getRight(), range.getLastBBox().getBox().getRight());
    final double innerWidth = Math.abs(innerRight - innerLeft);

    return innerWidth / outerWidth;
  }

  public double overlapY(final Range range) {
    if ((this.bottomBound <
        range.topBound)
        || (range.bottomBound <
        this.topBound)) {
      return 0;
    }

    final double outerTop = Math.min(this.topBound, range.topBound);
    final double outerBottom = Math.max(this.bottomBound, range.bottomBound);
    final double outerHeight = Math.abs(outerBottom - outerTop);

    final double innerTop = Math.max(this.topBound, range.topBound);
    final double innerBottom = Math.min(this.bottomBound, range.bottomBound);
    final double innerHeight = Math.abs(innerBottom - innerTop);

    return innerHeight / outerHeight;
  }

//  public double withinY(Range range) {
//
//  }
//
//  public double withinX(Range range) {
//
//  }


}
