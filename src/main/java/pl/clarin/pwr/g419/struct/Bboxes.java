package pl.clarin.pwr.g419.struct;

import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class Bboxes extends ArrayList<Bbox> {

  public Bboxes() {
  }

  public String getText() {
    return this.stream().map(Bbox::getText).collect(Collectors.joining(" "));
  }
}
