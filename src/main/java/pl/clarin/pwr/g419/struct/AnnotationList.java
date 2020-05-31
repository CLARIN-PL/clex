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

  public AnnotationList sortByLoc() {
    return new AnnotationList(this.stream()
        .sorted((a1, a2) -> compareByLocation(a1, a2))
        .collect(Collectors.toList()));
  }

  public AnnotationList filterByType(final String type) {
    return new AnnotationList(this.stream()
        .filter(a -> a.getType().equals(type))
        .collect(Collectors.toList()));
  }

  public AnnotationList filterByPageNo(final int pageNo) {
    return new AnnotationList(this.stream()
        .filter(a -> a.getPage().getNo() == pageNo)
        .collect(Collectors.toList()));
  }

  public AnnotationList topScore() {
    if (size() == 0) {
      return this;
    }
    final int topScore = stream().mapToInt(Annotation::getScore).max().getAsInt();
    return new AnnotationList(this.stream()
        .filter(a -> a.getScore() == topScore)
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

  public Optional<FieldContext<String>> getFirst() {
    return stream().findFirst()
        .map(an -> new FieldContext<>(an.getNorm(), an.getContext(), an.getSource()));
  }

  private int compareByLocation(final Annotation a1, final Annotation a2) {
    final int page = Integer.compare(a1.getPage().getNo(), a2.getPage().getNo());
    if (page != 0) {
      return page;
    }
    final int top = Integer.compare(a1.getFirst().getBox().getTop(), a2.getFirst().getBox().getTop());
    if (top != 0) {
      return top;
    }
    return Integer.compare(a1.getFirst().getBox().getLeft(), a2.getFirst().getBox().getLeft());
  }
}
