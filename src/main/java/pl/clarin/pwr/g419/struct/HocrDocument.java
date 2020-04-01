package pl.clarin.pwr.g419.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class HocrDocument extends ArrayList<HocrPage> {

  String id;

  Metadata metadata;

  public AnnotationList getAnnotations() {
    return new AnnotationList(this.stream().map(HocrPage::getAnnotations)
        .flatMap(Collection::stream).collect(Collectors.toList()));
  }
}
