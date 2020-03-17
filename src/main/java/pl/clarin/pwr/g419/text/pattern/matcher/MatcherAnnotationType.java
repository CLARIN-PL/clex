package pl.clarin.pwr.g419.text.pattern.matcher;

import java.util.Optional;
import pl.clarin.pwr.g419.struct.Annotation;
import pl.clarin.pwr.g419.struct.HocrPage;

public class MatcherAnnotationType extends Matcher {

  String type;

  public MatcherAnnotationType(final String type) {
    this.type = type;
  }

  @Override
  public Optional<MatcherResult> matchesAt(final HocrPage page, final int index) {
    final Optional<Annotation> annotation = page.getAnnotations().getStartingAt(index)
        .filterByType(type)
        .sortByLengthDesc()
        .stream().findFirst();
    if (annotation.isPresent()) {
      return Optional.of(postprocessMatcherResult(
          new MatcherResult(annotation.get().getLength()), annotation.get().getNorm()));
    } else {
      return Optional.empty();
    }
  }
}
