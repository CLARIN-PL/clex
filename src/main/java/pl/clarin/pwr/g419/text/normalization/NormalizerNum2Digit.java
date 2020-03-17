package pl.clarin.pwr.g419.text.normalization;

public class NormalizerNum2Digit extends Normalizer {

  @Override
  public String normalize(final String value) {
    return String.format("%02d", Integer.parseInt(value));
  }
}
