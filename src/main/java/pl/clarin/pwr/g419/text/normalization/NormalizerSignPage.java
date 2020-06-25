package pl.clarin.pwr.g419.text.normalization;

public class NormalizerSignPage extends NormalizerString {
  public String normalize(final String signPage) {
    if ((signPage == null)) {
      return "0";
    }
    return signPage;
  }

}
