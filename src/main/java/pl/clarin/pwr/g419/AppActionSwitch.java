package pl.clarin.pwr.g419;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.clarin.pwr.g419.action.Action;

@Component
public class AppActionSwitch implements CommandLineRunner {

  private final Map<String, Action> actions;

  public AppActionSwitch(final List<Action> actions) {
    this.actions = actions.stream().collect(Collectors.toMap(Action::getName, Function.identity()));
  }

  @Override
  public void run(final String[] args) throws IOException {
    Action action = null;
    try {
      final String actionName = getActionName(args);
      action = getAction(actionName);
      action.parseOptions(args);
      action.run();
    } catch (final MissingOptionException ex) {
      printError(ex.getMessage(), action);
    } catch (final CommandLineRunnerException ex) {
      printError(ex.getMessage());
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  public void printError(final String msg) {
    System.out.println(getHelpHeader());
    System.out.println("[ERROR] " + msg + "\n");
    System.out.println("Actions:");
    this.actions.values().stream()
        .sorted(Comparator.comparing(Action::getName))
        .map(a -> String.format("- %-20s -- %s", a.getName(), a.getDescription()))
        .forEach(System.out::println);
  }

  public void printError(final String msg, final Action action) {
    System.out.println("[ERROR] " + msg + "\n");
    final HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(98);
    formatter.printHelp(String.format("./clex %s [ACTION_PARAMETERS]", action.getName()),
        action.getOptions());
  }

  private String getActionName(final String[] args) throws CommandLineRunnerException {
    if (args.length > 0) {
      return args[0];
    } else {
      throw new CommandLineRunnerException("Action name is missing");
    }
  }

  private Action getAction(final String name) throws CommandLineRunnerException {
    final Action action = actions.get(name);
    if (action == null) {
      throw new CommandLineRunnerException(
          String.format("Action name '%s' was not recognized", name));
    }
    return action;
  }

  private String getHelpHeader() {
    final StringBuilder info = new StringBuilder();
    info.append("*--------------------------------------------------------------------*\n");
    info.append("* PolEval 2020 competitor in Task 4: Information extraction          *\n");
    info.append("* and entity typing from long documents with complex layouts         *\n");
    info.append("*                                                                    *\n");
    info.append("* Author : Michał Marcińczuk, Michał Olek                            *\n");
    info.append("* Contact: michal.marcinczuk@pwr.wroc.pl                             *\n");
    info.append("*          michal.olek@pwr.wroc.pl                                   *\n");
    info.append("*                                                                    *\n");
    info.append("* G4.19 Research Group, Wrocław University of Science and Technology *\n");
    info.append("*--------------------------------------------------------------------*\n");
    return info.toString();
  }
}
