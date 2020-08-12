package pl.clarin.pwr.g419.action.options;

public class ActionOptionReport extends ActionOption {

  public ActionOptionReport() {
    super("r");
    setLongOpt("report");
    setDescription("path to a report file (json)");
    setRequired(false);
    setHasArg(true);
  }

}
