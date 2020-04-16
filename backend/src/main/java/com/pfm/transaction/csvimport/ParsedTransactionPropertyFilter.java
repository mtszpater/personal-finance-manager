package com.pfm.transaction.csvimport;

import java.util.function.Predicate;

public interface ParsedTransactionPropertyFilter {
  static Predicate<String> notEmpty() {
    return stringProperty -> !stringProperty.isEmpty();
  }
}
