package com.easynutri.domain.exception;

/** Thrown when domain data violates business or schema constraints. */
public class ValidationException extends EasyNutriException {
  public ValidationException(String message) {
    super(message);
  }
}
