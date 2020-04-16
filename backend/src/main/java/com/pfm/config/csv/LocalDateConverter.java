package com.pfm.config.csv;

import static com.pfm.transaction.csvimport.ParsedTransactionPropertyFilter.notEmpty;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.pfm.transaction.csvimport.ParsedFromIngCsv;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalDateConverter<T> extends AbstractBeanField<ParsedFromIngCsv, LocalDate> {

  @Override
  public Object convert(String stringDate) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
    Optional<String> date = Optional.of(stringDate);
    return date
        .filter(notEmpty())
        .map(LocalDate::parse)
        .orElse(null);
  }
}

