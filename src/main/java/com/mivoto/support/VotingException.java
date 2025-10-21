package com.mivoto.support;

public class VotingException extends RuntimeException {
  public VotingException(String message) {
    super(message);
  }

  public VotingException(String message, Throwable cause) {
    super(message, cause);
  }
}
