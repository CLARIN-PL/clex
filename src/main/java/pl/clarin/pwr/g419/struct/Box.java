package pl.clarin.pwr.g419.struct;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Box {
  private int left;

  private int top;

  private int right;

  private int bottom;

  public int getWidth() {
    return right - left;
  }
}
