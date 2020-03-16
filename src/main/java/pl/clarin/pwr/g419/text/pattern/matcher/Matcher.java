package pl.clarin.pwr.g419.text.pattern.matcher;

import java.util.Optional;
import pl.clarin.pwr.g419.struct.HocrPage;

public abstract class Matcher {

  boolean optional = false;

  public abstract Optional<MatcherResult> matchesAt(HocrPage page, int index);

  public Matcher optional() {
    optional = true;
    return this;
  }

  public boolean isOptional() {
    return this.optional;
  }
}
