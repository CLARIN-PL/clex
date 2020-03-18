package pl.clarin.pwr.g419.text.pattern.matcher;

import java.util.Optional;
import pl.clarin.pwr.g419.struct.Annotation;
import pl.clarin.pwr.g419.struct.HocrPage;

public class MatcherNotAnnotationType extends Matcher {

  String type;

  public MatcherNotAnnotationType(final String type) {
    this.type = type;
  }

  @Override
  public Optional<MatcherResult> matchesAt(final HocrPage page, final int index) {
    final Optional<Annotation> annotation = page.getAnnotations().getStartingAt(index)
        .filterByType(type)
        .sortByLengthDesc()
        .stream().findFirst();
    if (!annotation.isPresent() && index < page.size()) {
      return Optional.of(postprocessMatcherResult(
          new MatcherResult(1), page.get(index).getText()));
    } else {
      return Optional.empty();
    }
  }
}
