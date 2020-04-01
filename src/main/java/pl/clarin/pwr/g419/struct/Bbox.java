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
}
