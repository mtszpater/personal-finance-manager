package com.pfm.transaction.csvimport;

import static com.pfm.transaction.csvimport.ParsedTransactionPropertyFilter.notEmpty;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import com.pfm.config.csv.LocalDateConverter;
import com.pfm.config.csv.PreAssigmentProcessor;
import com.pfm.config.csv.StringToBigDecimal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ParsedFromIngCsv {

  @PreAssignmentProcessor(processor = PreAssigmentProcessor.class)
  @CsvCustomBindByPosition(position = 0, converter = LocalDateConverter.class)
  @Getter
  private LocalDate transactionDate;

  @CsvBindByPosition(position = 2)
  @PreAssignmentProcessor(processor = PreAssigmentProcessor.class)
  private String contractorDetails;

  @CsvBindByPosition(position = 3)
  @PreAssignmentProcessor(processor = PreAssigmentProcessor.class)
  private String title;

  @CsvBindByPosition(position = 6)
  @PreAssignmentProcessor(processor = PreAssigmentProcessor.class)
  private String details;

  @CsvBindByPosition(position = 7)
  @Getter
  @PreAssignmentProcessor(processor = PreAssigmentProcessor.class)
  private String internalId;

  @CsvBindByPosition(position = 8)
  @PreAssignmentProcessor(processor = PreAssigmentProcessor.class)
  private String transactionAmount;

  @CsvBindByPosition(position = 10)
  @PreAssignmentProcessor(processor = PreAssigmentProcessor.class)
  private String lockedReleasedAmount;

  public Stream<String> getDescriptionCandidates() {
    List<String> candidates = List.of(
        this.contractorDetails,
        this.title,
        this.details
    );
    return candidates.stream()
        .filter(notEmpty());
  }

  public BigDecimal getTransactionAmount() {
    return Stream.of(this.transactionAmount, this.lockedReleasedAmount)
        .findFirst()
        .map(StringToBigDecimal::convert)
        .get();
  }

}
