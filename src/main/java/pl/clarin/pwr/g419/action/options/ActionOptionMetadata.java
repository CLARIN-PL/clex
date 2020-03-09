package pl.clarin.pwr.g419.action.options;

public class ActionOptionMetadata extends ActionOption {

  public ActionOptionMetadata() {
    super("m");
    setLongOpt("metadata");
    setDescription("path to a csv file with document metadata");
    setRequired(true);
    setHasArg(true);
  }

}
