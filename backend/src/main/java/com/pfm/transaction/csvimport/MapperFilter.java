package com.pfm.transaction.csvimport;

import java.math.BigDecimal;
import java.util.Set;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class MapperFilter {

  public boolean discard(ParsedFromIngCsv parsed, Set<String> allInternalIds) {
    String internalId = parsed.getInternalId();
    return internalId.isEmpty()
        || parsed.getTransactionAmount().equals(BigDecimal.ZERO)
        || allInternalIds.contains(internalId);
  }
}
