package com.pfm.transaction.csvimport;

import static com.pfm.config.csv.CsvIngParserConfig.COLUMN_SEPARATOR;
import static com.pfm.config.csv.CsvIngParserConfig.ENCODING;
import static com.pfm.config.csv.CsvIngParserConfig.MODEL_CLASS;
import static com.pfm.config.csv.CsvIngParserConfig.NUMBER_OF_LEADING_ROWS_INCLUDING_HEADER;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@NoArgsConstructor
public class CsvParser {

  public List<ParsedFromIngCsv> parse(MultipartFile multipartFile) throws TransactionsParsingException {

    List<ParsedFromIngCsv> parsedValuesFromCsv;
    try (Reader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), ENCODING))) {

      CsvToBean<ParsedFromIngCsv> csvToBean = new CsvToBeanBuilder<ParsedFromIngCsv>(reader)
          .withType(MODEL_CLASS)
          .withSkipLines(NUMBER_OF_LEADING_ROWS_INCLUDING_HEADER)
          .withSeparator(COLUMN_SEPARATOR)
          .withIgnoreQuotations(true)
          .withIgnoreLeadingWhiteSpace(true)
          .build();

      parsedValuesFromCsv = csvToBean.parse();
    } catch (IOException ex) {
      log.error("An error occurred during parsing csv file {} {}", multipartFile.getOriginalFilename(), ex.getMessage());
      throw new TransactionsParsingException("Some exception", ex);
    }

    return parsedValuesFromCsv;
  }
}
