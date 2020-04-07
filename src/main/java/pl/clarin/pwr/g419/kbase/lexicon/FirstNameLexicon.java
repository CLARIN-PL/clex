package pl.clarin.pwr.g419.kbase.lexicon;

public class FirstNameLexicon extends ResourceLexicon {

  private final String resourcePath = "/first_names.txt";

  @Override
  protected String getResourcePath() {
    return resourcePath;
  }
}
