package pl.clarin.pwr.g419.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public class AnnotationList extends ArrayList<Annotation> {

  public AnnotationList(final Collection<Annotation> annotations) {
    this.addAll(annotations);
  }

  public AnnotationList sortByLengthDesc() {
    return new AnnotationList(this.stream()
        .sorted(Comparator.comparing(Annotation::getLength).reversed())
        .collect(Collectors.toList()));
  }

  public AnnotationList filterByType(final String type) {
    return new AnnotationList(this.stream()
        .filter(a -> a.getType().equals(type))
        .collect(Collectors.toList()));
  }

}
