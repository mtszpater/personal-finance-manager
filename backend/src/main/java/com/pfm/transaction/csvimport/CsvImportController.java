package com.pfm.transaction.csvimport;

import com.pfm.auth.UserProvider;
import com.pfm.transaction.Transaction;
import com.pfm.transaction.TransactionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@AllArgsConstructor
@RestController
public class CsvImportController {

  public static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
  private static final String BEARER = "Bearer";
  private CsvParserService csvParserService;
  private TransactionService transactionService;
  private UserProvider userProvider;

  @ApiOperation(value =
      "Upload csv file with transactions to parse from", response = Transaction.class, responseContainer = "List", authorizations = {
      @Authorization(value = BEARER)})
  @PostMapping("/csvImport")
  ResponseEntity<?> uploadCsvFileAndParseTransactionsToAccount(@RequestParam("file") MultipartFile file, @RequestParam("accountId") long accountId) {
    // fixme lukasz   validate content type
    long userId = userProvider.getCurrentUserId();
    if (!file.getContentType().equals(APPLICATION_VND_MS_EXCEL)) {
      return ResponseEntity.badRequest().body("Uploaded file has wrong content type, only files with " + APPLICATION_VND_MS_EXCEL + " accepted");
    }
    log.info("Uploaded file: {}, with size: {}, with content type: {}", file.getOriginalFilename(), file.getSize(), file.getContentType());
    List<Transaction> transactions;
    try {
      transactions = csvParserService.convertToTransactions(file);

    } catch (TransactionsParsingException ex) {
      log.error("An error occurred during parsing transactions from csv file {}", ex.getMessage());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    log.info("All {} successfully parsed", transactions.size());

    for (Transaction transaction : transactions) {
      transactionService.addTransaction(userId, transaction, true);
    }

    return ResponseEntity.ok().body(transactions);
  }
}
