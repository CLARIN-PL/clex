package pl.clarin.pwr.g419.struct;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Range {

  double lowerBound;
  double upperBound;

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

  public void merge(final double lower, final double upper) {
    lowerBound = Math.min(lowerBound, lower);
    upperBound = Math.max(upperBound, upper);
  }

  public double getLength() {
    return upperBound - lowerBound;
  }
}
