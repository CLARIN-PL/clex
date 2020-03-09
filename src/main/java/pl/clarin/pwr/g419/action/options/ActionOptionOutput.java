package pl.clarin.pwr.g419.action.options;

public class ActionOptionOutput extends ActionOption {

  public ActionOptionOutput() {
    super("o");
    setLongOpt("output");
    setDescription("path to an output file (json)");
    setRequired(true);
    setHasArg(true);
  }

}
