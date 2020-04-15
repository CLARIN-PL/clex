package pl.clarin.pwr.g419.action.options;

public class ActionOptionInput extends ActionOption {

  public ActionOptionInput() {
    super("i");
    setLongOpt("input");
    setDescription("path to input data (hocr or json)");
    setRequired(true);
    setHasArg(true);
  }

  public ActionOptionInput(final String description) {
    this();
    setDescription(description);
  }
}
