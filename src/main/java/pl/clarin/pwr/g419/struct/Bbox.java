package pl.clarin.pwr.g419.struct;

import lombok.Data;

@Data
public class Bbox implements Contour {

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
  public int getBottom() {
    return getBox().getBottom();
  }

}
