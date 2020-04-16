package com.pfm.config.csv;

import com.pfm.transaction.csvimport.ParsedFromIngCsv;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvIngParserConfig {

  public static final Charset ENCODING = StandardCharsets.UTF_8;
  public static final int NUMBER_OF_LEADING_ROWS_INCLUDING_HEADER = 1;
  public static final char COLUMN_SEPARATOR = ';';
  public static final Class<ParsedFromIngCsv> MODEL_CLASS = ParsedFromIngCsv.class;
  public static final String IMPORT_TARGET_ACCOUNT_NAME = "Inteligo";
}
