package pl.clarin.pwr.g419.struct;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Bbox {

  private String text;

  private Box box;
}
