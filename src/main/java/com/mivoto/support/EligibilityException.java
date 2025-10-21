package com.mivoto.support;

public class EligibilityException extends RuntimeException {
  public EligibilityException(String message) {
    super(message);
  }

  public EligibilityException(String message, Throwable cause) {
    super(message, cause);
  }
}
