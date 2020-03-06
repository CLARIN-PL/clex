package pl.clarin.pwr.g419;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    try {
      final String actionName = getActionName(args);
      final Action action = getAction(actionName);
      System.out.println(getHelpHeader());
      action.run();
    } catch (final CommandLineRunnerException ex) {
      System.out.println("[ERROR] " + ex.getMessage());
      System.out.println("Run: ");
      System.out.println("  ./clex [ACTION] [ACTION_PARAMETERS]");
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
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
    info.append("* Author : Michał Marcińczuk                                         *\n");
    info.append("* Contact: michal.marcinczuk@pwr.wroc.pl                             *\n");
    info.append("*                                                                    *\n");
    info.append("* G4.19 Research Group, Wrocław University of Science and Technology *\n");
    info.append("*--------------------------------------------------------------------*\n");
    return info.toString();
  }
}
