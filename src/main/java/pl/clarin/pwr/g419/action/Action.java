package pl.clarin.pwr.g419.action;

import lombok.Data;
import pl.clarin.pwr.g419.HasLogger;

@Data
abstract public class Action implements HasLogger {

  private final String name;
  private final String description;

  public Action(final String name, final String description) {
    this.name = name;
    this.description = description;
  }

  abstract public void run() throws Exception;

}
