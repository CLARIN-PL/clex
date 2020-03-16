package pl.clarin.pwr.g419.struct;

import java.util.List;
import lombok.Data;

@Data
public class HocrPage extends Bboxes {

  Annotations annotations = new Annotations();

  public HocrPage() {
  }

  public HocrPage(final List<Bbox> bboxes) {
    this.addAll(bboxes);
  }

  public void addAnnotation(final Annotation annotation) {
    this.annotations.add(annotation);
  }
}
