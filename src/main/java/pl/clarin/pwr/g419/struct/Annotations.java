package pl.clarin.pwr.g419.struct;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Annotations extends ArrayList<Annotation> {

  Map<Integer, Set<Annotation>> indexToAnnotations = Maps.newHashMap();

  @Override
  public boolean add(final Annotation annotation) {
    IntStream.range(annotation.getIndexBegin(), annotation.getIndexEnd())
        .forEach(n -> indexToAnnotations.computeIfAbsent(n, v -> Sets.newHashSet()).add(annotation));
    return super.add(annotation);
  }

  public AnnotationList getStartingAt(final int index) {
    return new AnnotationList(indexToAnnotations.getOrDefault(index, Collections.emptySet())
        .stream().filter(a -> a.getIndexBegin() == index).collect(Collectors.toList()));
  }
}
