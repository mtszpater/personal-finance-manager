package com.pfm.transaction.csvimport;

import static com.pfm.export.ImportService.CATEGORY_NAMED_IMPORTED;

import com.pfm.account.AccountRepository;
import com.pfm.auth.UserProvider;
import com.pfm.category.CategoryRepository;
import com.pfm.transaction.Transaction;
import com.pfm.transaction.TransactionRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class CsvParserService {

  public static final String TARGET_ACCOUNT_FOR_PARSED_TRANSACTIONS_NOT_FOUND = "Target account for parsed transactions not found";
  private UserProvider userProvider;
  private CategoryRepository categoryRepository;
  private AccountRepository accountRepository;
  private TransactionRepository transactionRepository;
  private CsvParser csvParser;
  private ParsedIngToTransactionMapper parsedIngToTransactionMapper;

  List<Transaction> convertToTransactions(MultipartFile file) throws TransactionsParsingException, TargetAccountNotFoundException {
    long userId = userProvider.getCurrentUserId();
    Optional<Long> targetAccountIdOptional = accountRepository.getAccountIdByName(userId);
    if (targetAccountIdOptional.isEmpty()) {
      throw new TargetAccountNotFoundException(TARGET_ACCOUNT_FOR_PARSED_TRANSACTIONS_NOT_FOUND);
    }
    long importedCategoryId = categoryRepository.findByNameIgnoreCaseAndUserId(CATEGORY_NAMED_IMPORTED, userId).get(0).getId();
    final Set<String> allInternalIds = transactionRepository.getAllInternalIds(userId);

    final List<ParsedFromIngCsv> parsedValuesFromCsv = parse(file);
    long targetAccountId = targetAccountIdOptional.get();
    return parsedIngToTransactionMapper.map(parsedValuesFromCsv, targetAccountId, importedCategoryId, allInternalIds);
  }

  private List<ParsedFromIngCsv> parse(MultipartFile file) throws TransactionsParsingException {

    List<ParsedFromIngCsv> parsedValues;
    parsedValues = csvParser.parse(file);

    return parsedValues;
  }
}
