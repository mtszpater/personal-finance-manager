package com.pfm.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pfm.history.HistoryField;
import com.pfm.history.HistoryField.SpecialFieldType;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonIgnore
  private String internalId;

  // TODO -- add Swagger annotations - type is used in getAllTransactions
  @HistoryField
  private String description;

  @HistoryField(fieldType = SpecialFieldType.CATEGORY)
  private Long categoryId;

  @HistoryField
  private LocalDate date;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @HistoryField(fieldType = SpecialFieldType.ACCOUNT_PRICE_ENTRY)
  private List<AccountPriceEntry> accountPriceEntries;

  @JsonIgnore
  private Long userId;

  private boolean isPlanned;

  @Enumerated(EnumType.STRING)
  private RecurrencePeriod recurrencePeriod;

  public boolean isRecurrent() {
    return recurrencePeriod != null && recurrencePeriod != RecurrencePeriod.NONE;
  }

}
