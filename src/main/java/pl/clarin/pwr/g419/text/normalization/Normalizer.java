package pl.clarin.pwr.g419.text.normalization;

abstract public class Normalizer<T> {

  public abstract String normalize(T value);

}
