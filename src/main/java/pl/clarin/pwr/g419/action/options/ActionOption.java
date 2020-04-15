package pl.clarin.pwr.g419.action.options;

import java.nio.file.Path;
import java.nio.file.Paths;
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
  protected String defaultValue = null;

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
    if (defaultValue != null) {
      return line.getOptionValue(name, defaultValue);
    } else {
      return line.getOptionValue(name);
    }
  }

  public int getInteger() {
    final String value = getString();
    return Integer.parseInt(value);
  }

  public Path getPath() {
    return Paths.get(getString());
  }
}
