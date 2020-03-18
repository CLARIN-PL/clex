package pl.clarin.pwr.g419.utils;

public class TrueFalseCounter {

  int counterTrue = 0;
  int counterFalse = 0;

  synchronized public void addTrue() {
    counterTrue++;
  }

  synchronized public void addFalse() {
    counterFalse++;
  }

  public int getTrue() {
    return counterTrue;
  }

  public int getFalse() {
    return counterFalse;
  }

  public double getAccuracy() {
    final double sum = counterTrue + counterFalse;
    return sum == 0.0 ? 0.0 : counterTrue * 100.0 / sum;
  }
}
