package pl.clarin.pwr.g419.struct;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AnnotationList extends ArrayList<Annotation> {

  public AnnotationList(final Collection<Annotation> annotations) {
    this.addAll(annotations);
  }

  public AnnotationList sortByLengthDesc() {
    return new AnnotationList(this.stream()
        .sorted(Comparator.comparing(Annotation::getLength).reversed())
        .collect(Collectors.toList()));
  }

  public AnnotationList sortByPos() {
    return new AnnotationList(this.stream()
        .sorted(Comparator.comparingInt(o -> o.getFirst().getNo()))
        .collect(Collectors.toList()));
  }

  public AnnotationList filterByType(final String type) {
    return new AnnotationList(this.stream()
        .filter(a -> a.getType().equals(type))
        .collect(Collectors.toList()));
  }

  public AnnotationList removeNested() {
    final Map<HocrPage, Set<Integer>> pageTokens = Maps.newHashMap();
    final List<Annotation> selected = Lists.newArrayList();
    this.stream()
        .sorted(Comparator.comparing(Annotation::getLength).reversed())
        .forEach(an -> {
          if (!pageTokens.computeIfAbsent(an.page, n -> Sets.newHashSet()).contains(an.indexBegin)) {
            IntStream.range(an.indexBegin, an.indexEnd).forEach(
                i -> pageTokens.computeIfAbsent(an.page, n -> Sets.newHashSet()).add(i)
            );
            selected.add(an);
          }
        });
    return new AnnotationList(selected);
  }

  public ValueContext getFirstNomContext() {
    final ValueContext vc = new ValueContext();
    stream().findFirst().ifPresent(an -> {
      vc.setValue(an.getNorm());
      vc.setContext(an.getText());
    });
    return vc;
  }
}
