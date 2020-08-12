package pl.clarin.pwr.g419.text.normalization;

public class NormalizerStringToUpperCaseCutPreLastSpace extends Normalizer<String> {


  @Override
  public String normalize(final String value) {
    if (value == null) return null;

    String result = value; //.toUpperCase();
    if (
        (result.indexOf("LOK") == -1)
            && (result.indexOf("Lok") == -1)
            && (result.indexOf("lok") == -1)) {
      if (result.length() > 2) {
        final int index = result.length() - 1 - 1;
        if (result.charAt(index) == ' ') {
          result = result.substring(0, index) + result.substring(index + 1);
        }
      }
    }

    return result;
  }


}
