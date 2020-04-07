package pl.clarin.pwr.g419.kbase.lexicon;

import com.google.common.collect.Maps;
import java.util.Map;

public class PersonNameLexicon extends ResourceLexicon {

  private final String resourcePath = "/person_names.txt";

  Map<String, String> simplifiedNames = Maps.newHashMap();

  @Override
  protected String getResourcePath() {
    return resourcePath;
  }

  public PersonNameLexicon() {
    super();
    createSimplifiedDictionary();
  }

  private void createSimplifiedDictionary() {
    for (final String name : this) {
      final String[] parts = name.split(" ");
      if (parts.length == 3) {
        simplifiedNames.put(String.format("%s %s", parts[0], parts[2]), name);
      }
    }
  }

  public String approximate(final String name) {
    if (name == null || name.length() == 0) {
      return name;
    }
    if (this.contains(name)) {
      return name;
    }
    return this.getFullName(name);
  }

  public String getFullName(final String name) {
    return this.simplifiedNames.getOrDefault(name, name);
  }

}
