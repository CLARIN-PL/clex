package pl.clarin.pwr.g419.action.options;

public class ActionOptionThreads extends ActionOption {

  public ActionOptionThreads() {
    super("T");
    setLongOpt("threads");
    setDescription("number of threads");
    setHasArg(true);
    setDefaultValue("4");
  }

}
