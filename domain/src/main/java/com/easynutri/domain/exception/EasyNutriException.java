package com.easynutri.domain.exception;

/** Base unchecked exception for all EasyNutri domain-level errors. */
public class EasyNutriException extends RuntimeException {
  public EasyNutriException(String message) {
    super(message);
  }

  public EasyNutriException(String message, Throwable cause) {
    super(message, cause);
  }
}
