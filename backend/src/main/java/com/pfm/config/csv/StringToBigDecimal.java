package com.pfm.config.csv;

import static com.pfm.transaction.csvimport.ParsedTransactionPropertyFilter.notEmpty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class StringToBigDecimal {

  public static BigDecimal convert(String amount) {
    Optional<String> amountOptional = Optional.of(amount);
    return amountOptional
        .filter(notEmpty())
        .map(str -> Double.parseDouble(str.replace(',', '.')))
        .map(doubleNum -> BigDecimal.valueOf(doubleNum).setScale(2, RoundingMode.HALF_UP))
        .orElse(BigDecimal.ZERO);
  }

}
