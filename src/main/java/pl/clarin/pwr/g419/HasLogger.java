package pl.clarin.pwr.g419;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HasLogger {

  default Logger getLogger() {
    return LoggerFactory.getLogger(getClass());
  }
}