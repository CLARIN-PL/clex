package pl.clarin.pwr.g419.struct;

import lombok.Data;

@Data
public class Bbox {

  private String text;

  private Box box;

  public Bbox(final String text, final Box box) {
    this.text = text;
    this.box = box;
  }
}
