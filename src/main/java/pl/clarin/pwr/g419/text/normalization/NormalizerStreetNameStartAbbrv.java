package pl.clarin.pwr.g419.text.normalization;

public class NormalizerStreetNameStartAbbrv extends Normalizer<String> {

  @Override
  public String normalize(final String value) {
    return value.replace(" .", ".");
  }
}
