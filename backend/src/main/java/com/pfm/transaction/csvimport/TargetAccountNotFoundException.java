package com.pfm.transaction.csvimport;

public class TargetAccountNotFoundException extends Exception {

  private static final long serialVersionUID = 3L;

  public TargetAccountNotFoundException(String message) {
    super(message);
  }
}
