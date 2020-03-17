package pl.clarin.pwr.g419.text.normalization;

import java.util.Map;

public class NormalizerMap extends Normalizer {

  Map<String, String> mapValue;

  public NormalizerMap(final Map<String, String> mapValue) {
    this.mapValue = mapValue;
  }

  @Override
  public String normalize(final String value) {
    return mapValue.getOrDefault(value, value);
  }
}
