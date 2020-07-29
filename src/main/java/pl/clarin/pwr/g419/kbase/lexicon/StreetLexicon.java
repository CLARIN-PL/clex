package pl.clarin.pwr.g419.kbase.lexicon;

public class StreetLexicon extends ResourceLexicon {

  private final String resourcePath = "/nazwy_ulic_do_miast.txt";

  @Override
  protected String getResourcePath() {
    return resourcePath;
  }
}
