package pl.clarin.pwr.g419.action.options;

import lombok.Data;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

@Data
public abstract class ActionOption {

  protected String name = null;
  protected String longOpt = null;
  protected String description = null;
  protected boolean required = false;
  protected boolean hasArg = false;

  Option option = null;
  private CommandLine line;

  public ActionOption(final String name) {
    this.name = name;
  }

  protected Option create() {
    Option.Builder ob = Option.builder(name);
    if (longOpt != null) {
      ob = ob.longOpt(longOpt);
    }
    if (description != null) {
      ob = ob.desc(description);
    }
    if (required) {
      ob = ob.required();
    }
    if (hasArg) {
      ob = ob.hasArg();
    }
    return ob.build();
  }

  public Option get() {
    if (option == null) {
      option = create();
    }
    return option;
  }

  public void setCommandLine(final CommandLine line) {
    this.line = line;
  }

  public String getString() {
    return line.getOptionValue(name);
  }
}
