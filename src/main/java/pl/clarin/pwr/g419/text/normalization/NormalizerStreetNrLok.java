package pl.clarin.pwr.g419.text.normalization;

public class NormalizerStreetNrLok extends Normalizer<String> {

  @Override
  public String normalize(final String value) {
    return value.replace(" .", ".");
  }

}
