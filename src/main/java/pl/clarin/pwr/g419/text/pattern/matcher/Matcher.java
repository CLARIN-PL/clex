package pl.clarin.pwr.g419.text.pattern.matcher;

import java.util.Optional;
import pl.clarin.pwr.g419.struct.HocrPage;

public abstract class Matcher {

  public abstract Optional<Integer> matchesAt(HocrPage page, int index);

}
