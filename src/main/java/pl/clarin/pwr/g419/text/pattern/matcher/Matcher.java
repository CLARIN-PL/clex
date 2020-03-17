package pl.clarin.pwr.g419.text.pattern.matcher;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import pl.clarin.pwr.g419.struct.HocrPage;
import pl.clarin.pwr.g419.text.normalization.Normalizer;

public abstract class Matcher {

  boolean optional = false;
  Optional<String> groupName = Optional.empty();
  Map<String, Normalizer> normalizerMap = Maps.newHashMap();

  public abstract Optional<MatcherResult> matchesAt(HocrPage page, int index);

  public Matcher optional() {
    optional = true;
    return this;
  }

  public Matcher group(final String name) {
    groupName = Optional.of(name);
    return this;
  }

  public Matcher normalizer(final Normalizer normalizer) {
    normalizerMap.put(groupName.get(), normalizer);
    return this;
  }

  public Matcher normalizer(final String groupName, final Normalizer normalizer) {
    normalizerMap.put(groupName, normalizer);
    return this;
  }

  public boolean isOptional() {
    return this.optional;
  }

  public boolean isGroup() {
    return this.groupName.isPresent();
  }

  protected MatcherResult postprocessMatcherResult(final MatcherResult mr,
                                                   final String matcherValue) {
    if (isGroup()) {
      mr.getGroups().put(groupName.get(), matcherValue);
    }
    for (final Map.Entry<String, String> entry : mr.getGroups().entrySet()) {
      if (normalizerMap.containsKey(entry.getKey())) {
        entry.setValue(normalizerMap.get(entry.getKey()).normalize(entry.getValue()));
      }
    }
    return mr;
  }

}
