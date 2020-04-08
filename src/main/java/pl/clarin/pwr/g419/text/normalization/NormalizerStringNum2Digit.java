package pl.clarin.pwr.g419.text.normalization;

public class NormalizerStringNum2Digit extends NormalizerString {

  @Override
  public String normalize(final String value) {
    return String.format("%02d", Integer.parseInt(value));
  }
}
