package pl.clarin.pwr.g419.text.normalization;

import java.util.Map;

public class NormalizerStringMap extends NormalizerString {

  Map<String, String> mapValue;

  public NormalizerStringMap(final Map<String, String> mapValue) {
    this.mapValue = mapValue;
  }

  @Override
  public String normalize(final String value) {
    return mapValue.getOrDefault(value, value);
  }
}
