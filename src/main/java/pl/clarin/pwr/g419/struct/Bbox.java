package pl.clarin.pwr.g419.struct;

import lombok.Data;

@Data
public class Bbox {

  private int no;
  private String text;
  private Box box;
  private boolean lineBegin = false;
  private boolean lineEnd = false;
  private boolean blockEnd = false;

  public Bbox(final int no, final String text, final Box box) {
    this.no = no;
    this.text = text;
    this.box = box;
  }

  public String getLowNiceText() {
    return text
        .toLowerCase()
        .replaceFirst("^[^\\p{L}]+", "")
        .replaceAll("[^\\p{L}]+$", "");
  }


  public double overlapX(final Bbox bbox) {
    if ((bbox.getBox().getLeft() >
        this.getBox().getRight())
        || (bbox.getBox().getRight() <
        this.getBox().getLeft())) {
      return 0;
    }

    final double outerLeft = Math.min(this.getBox().getLeft(), bbox.getBox().getLeft());
    final double outerRight = Math.max(this.getBox().getRight(), bbox.getBox().getRight());
    final double outerWidth = Math.abs(outerRight - outerLeft);

    final double innerLeft = Math.max(this.getBox().getLeft(), bbox.getBox().getLeft());
    final double innerRight = Math.min(this.getBox().getRight(), bbox.getBox().getRight());
    final double innerWidth = Math.abs(innerRight - innerLeft);

    return innerWidth / outerWidth;
  }


  public double overlapY(final Bbox bbox) {
    if ((bbox.getBox().getTop() >
        this.getBox().getBottom())
        || (bbox.getBox().getBottom() <
        this.getBox().getTop())) {
      return 0;
    }

    final double outerTop = Math.min(this.getBox().getTop(), bbox.getBox().getTop());
    final double outerBottom = Math.max(this.getBox().getBottom(), bbox.getBox().getBottom());
    final double outerHeight = Math.abs(outerBottom - outerTop);

    final double innerTop = Math.max(this.getBox().getTop(), bbox.getBox().getTop());
    final double innerBottom = Math.min(this.getBox().getBottom(), bbox.getBox().getBottom());
    final double innerHeight = Math.abs(innerBottom - innerTop);

    return innerHeight / outerHeight;
  }


}
