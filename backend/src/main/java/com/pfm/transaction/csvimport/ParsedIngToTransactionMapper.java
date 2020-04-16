package com.pfm.transaction.csvimport;

import com.pfm.transaction.AccountPriceEntry;
import com.pfm.transaction.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ParsedIngToTransactionMapper {

  private MapperFilter mapperFilter;

  List<Transaction> map(List<ParsedFromIngCsv> parsedFromIngCsv, long targetAccountId, long importedCategoryId,
      Set<String> allInternalIds) {
    List<Transaction> mapped = new ArrayList<>();

    for (ParsedFromIngCsv parsed : parsedFromIngCsv) {
      if (mapperFilter.discard(parsed, allInternalIds)) {
        continue;
      }
      AccountPriceEntry entries = AccountPriceEntry.builder()
          .accountId(targetAccountId)
          .price(parsed.getTransactionAmount()
          )
          .build();

      Transaction transaction = Transaction.builder()
          .internalId(parsed.getInternalId())
          .categoryId(importedCategoryId)
          .accountPriceEntries(List.of(entries))
          .description(parsed.getDescriptionCandidates()
              .collect(Collectors.joining("#"))
          )
          .date(parsed.getTransactionDate()
          )
          .build();
      mapped.add(transaction);
    }
    return mapped;
  }
}
