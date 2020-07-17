package pl.clarin.pwr.g419.action.options;

public class ActionOptionSelectOne extends ActionOption {

  public ActionOptionSelectOne() {
    super("s");
    setLongOpt("select");
    setDescription("only one document will be processed and it is in dir number: ");
    setRequired(false);
    setHasArg(true);
  }

  public ActionOptionSelectOne(final String description) {
    this();
    setDescription(description);
  }
}
