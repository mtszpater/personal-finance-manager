package com.pfm.export;

import com.pfm.auth.UserProvider;
import com.pfm.export.ExportResult.ExportPeriod;
import com.pfm.export.ExportResult.ExportTransaction;
import com.pfm.history.HistoryEntry;
import java.util.Map;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@Getter
@AllArgsConstructor
public class ImportService {

  private ImportHelper importHelper;
  private UserProvider userProvider;

  @Transactional
  void importData(@RequestBody ExportResult inputData, long userId) throws ImportFailedException {
    Map<String, Long> categoryNameToIdMap = importHelper.importCategoriesAndMapCategoryNamesToIds(inputData, userId);
    Map<String, Long> accountNameToIdMap = importHelper.importAccountsAndMapAccountNamesToIds(inputData, userId);
    importHelper.importFilters(inputData, userId, accountNameToIdMap, categoryNameToIdMap);

    for (ExportPeriod period : inputData.getPeriods()) {
      for (ExportTransaction transaction : period.getTransactions()) {
        importHelper.importTransaction(categoryNameToIdMap, accountNameToIdMap, transaction, userId);
      }
    }

    for (HistoryEntry historyEntry : inputData.getHistoryEntries()) {
      historyEntry.setUserId(userProvider.getCurrentUserId());
      importHelper.saveHistoryEntry(historyEntry);
    }
  }
}
