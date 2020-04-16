package com.pfm.transaction.csvimport;

public class TransactionsParsingException extends Exception {

  private static final long serialVersionUID = 2L;

  public TransactionsParsingException(String message, Exception cause) {
    super(message, cause);
  }
}
