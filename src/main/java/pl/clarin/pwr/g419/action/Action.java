package pl.clarin.pwr.g419.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import pl.clarin.pwr.g419.HasLogger;
import pl.clarin.pwr.g419.action.options.ActionOption;

@Data
abstract public class Action implements HasLogger {

  private final String name;
  private final String description;
  protected List<ActionOption> options = Lists.newArrayList();

  public Action(final String name, final String description) {
    this.name = name;
    this.description = description;
  }

  public void addOption(final ActionOption option) {
    this.options.add(option);
  }

  public void parseOptions(final String[] args) throws Exception {
    final CommandLine line = new DefaultParser().parse(getOptions(), args);
    checkOptionRepetition(line);
    this.options.stream().forEach(o -> o.setCommandLine(line));
  }

  public Options getOptions() {
    final Options options = new Options();
    this.options.stream().map(ActionOption::get).forEach(o -> options.addOption(o));
    return options;
  }

  public void checkOptionRepetition(final CommandLine line) {
    try {
      final Set<String> argNames = Sets.newHashSet();
      for (final Option opt : line.getOptions()) {
        final String argName = opt.getOpt();
        if (!opt.hasArgs() && argNames.contains(argName)) {
          throw new Exception("Repeated argument: " + argName);
        } else {
          argNames.add(argName);
        }
      }
    } catch (final Exception e) {
      System.out.println(e);
      System.exit(1);
    }
  }

  abstract public void run() throws Exception;

}
