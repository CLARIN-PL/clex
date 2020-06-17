package pl.clarin.pwr.g419.struct;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = "page")
public class Range {

  HocrPage page;

  int lowerBound;
  int upperBound;

  int firstBoxInRangeIndex;
  int lastBoxInRangeIndex;

  public Range(final int lBound, final int uBound) {
    lowerBound = lBound;
    upperBound = uBound;
  }


  public Range(final HocrPage p, final int lBound, final int uBound) {
    page = p;
    lowerBound = lBound;
    upperBound = uBound;
  }


  public double overlap(final Range range) {
    if (range.upperBound < lowerBound || range.lowerBound > upperBound) {
      return 0.0;
    }
    final double lower = Math.max(lowerBound, range.lowerBound);
    final double upper = Math.min(upperBound, range.upperBound);
    final double length = Math.abs(upper - lower);
    final double maxLength = Math.max(getLength(), range.getLength());
    return length / maxLength;
  }

  public double within(final Range range) {
    if (range.upperBound < lowerBound || range.lowerBound > upperBound) {
      return 0.0;
    }
    final double lower = Math.max(lowerBound, range.lowerBound);
    final double upper = Math.min(upperBound, range.upperBound);
    final double length = Math.abs(upper - lower);
    return length / getLength();
  }

  public void merge(final int lower, final int upper) {
    lowerBound = Math.min(lowerBound, lower);
    upperBound = Math.max(upperBound, upper);
  }

  public int getLength() {
    return upperBound - lowerBound;
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


}
