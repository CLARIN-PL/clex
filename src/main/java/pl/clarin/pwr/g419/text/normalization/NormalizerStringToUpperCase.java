package pl.clarin.pwr.g419.text.normalization;

public class NormalizerStringToUpperCase extends Normalizer<String> {

  @Override
  public String normalize(final String value) { return value == null ? null : value.toUpperCase(); }
}
