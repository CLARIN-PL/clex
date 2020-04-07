package pl.clarin.pwr.g419.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class Bboxes extends ArrayList<Bbox> {

  public Bboxes() {
  }

  public Bboxes(final Collection<Bbox> bboxes) {
    this.addAll(bboxes);
  }

  public Bbox getFirst() {
    return this.get(0);
  }

  public Bbox getLast() {
    return this.get(size() - 1);
  }

  public String getText() {
    return this.stream().map(Bbox::getText).collect(Collectors.joining(" "));
  }

  public OptionalInt getBottom() {
    return this.stream().mapToInt(b -> b.getBox().getBottom()).max();
  }

  public OptionalInt getTop() {
    return this.stream().mapToInt(b -> b.getBox().getTop()).min();
  }
}
