package pl.clarin.pwr.g419.kbase.lexicon;

public class CityLexicon extends ResourceLexicon {

  private final String resourcePath = "/nazwy_miast_1000v2.txt";

  public CityLexicon(int limit) {
    super(limit);
  }

  @Override
  protected String getResourcePath() {
    return resourcePath;
  }
}
