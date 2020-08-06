package pl.clarin.pwr.g419.action.options;

public class ActionOptionPersonEvaluationVariants extends ActionOption {

  public ActionOptionPersonEvaluationVariants() {
    super("pev");
    setLongOpt("personevalvariant");
    setDescription("Persons will be evaluated the way it stands in variant ");
    setRequired(false);
    setHasArg(true);
  }

  public ActionOptionPersonEvaluationVariants(final String description) {
    this();
    setDescription(description);
  }
}
