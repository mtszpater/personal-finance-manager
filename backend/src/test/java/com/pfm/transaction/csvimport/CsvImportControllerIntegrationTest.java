package com.pfm.transaction.csvimport;

import static com.pfm.config.csv.CsvIngParserConfig.IMPORT_TARGET_ACCOUNT_NAME;
import static com.pfm.helpers.TestHelper.convertDoubleToBigDecimal;
import static com.pfm.helpers.TestUsersProvider.userMarian;
import static com.pfm.transaction.csvimport.CsvImportController.APPLICATION_VND_MS_EXCEL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.pfm.account.Account;
import com.pfm.account.type.AccountType;
import com.pfm.currency.Currency;
import com.pfm.helpers.IntegrationTestsBase;
import com.pfm.transaction.Transaction;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

class CsvImportControllerIntegrationTest extends IntegrationTestsBase {

  public static final long NOT_EXISTING_TARGET_ACCOUNT = 9085534L;

  @BeforeEach
  public void beforeEach() throws Exception {
    userId = callRestToRegisterUserAndReturnUserId(userMarian());
    token = callRestToAuthenticateUserAndReturnToken(userMarian());
  }

  @Test
  void shouldReturnEmptyListByIgnoringTransactionsNotHavingInternalTransactionNumberOrAccountingDate() throws Exception {
    // Transactions in the file having accountingDate (Data ksiegowania) and internalTransactionId (Nr transakcji) blank are not eligible for parsing
    // as they do not mean real transactions, they mean only that some amount was locked/secured on the account - the actual transaction comes after
    // and it has accountingDate and internalTransactionId filled accordingly.
    final String path = "src/test/resources/csvIntegrationTestNoPolishLettersNoTransactionNumbersNoAccountingDates.csv";
    //fixme lukasz solve encoding problem then add more transactions/
    //Given
    //    final Transaction expected1 = Transaction.builder()
    //        .date(LocalDate.of(2019, 10, 9))
    //        .description("ZABKA Z4083 K2 KRAKOW PL#Platnosc karta 09.10.2019 Nr karty 4246xx9675#\"")
    //        .categoryId(13L)
    //        .accountPriceEntries(List.of(
    //            AccountPriceEntry.builder()
    //                .accountId(6L)
    //                .price(convertDoubleToBigDecimal(0))
    //                .id(null)
    //                .build())
    //        ).build();

    Account importTargetAccount = Account.builder()
        .name(IMPORT_TARGET_ACCOUNT_NAME)
        .type(AccountType.builder().id(16L).name("Credit").build())
        .balance(convertDoubleToBigDecimal(1000))
        .currency(Currency.builder().id(13L).name("Pln").exchangeRate(BigDecimal.valueOf(1.00)).build())
        .build();
    final long importTargetAccountId = callRestServiceToAddAccountAndReturnId(importTargetAccount, token);

    final byte[] fileBytes = Files.readAllBytes(Paths.get(path));
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "file", APPLICATION_VND_MS_EXCEL, fileBytes);

    //When
    List<Transaction> parsedTransactions = callRestToImportTransactionsFromCsvFileAndReturnParsedTransactions(mockMultipartFile,
        importTargetAccountId);

    //Then
    assertThat(parsedTransactions.size(), is(equalTo(0)));
  }

  @Test
  void shouldReturnListOfTransactionsWhenUploadingCsvFile() throws Exception {
    //Given
    final String path = "src/test/resources/onlyValidTransactionsCsvIntegrationTestNoPolishLetters.csv";
    //fixme lukasz solve encoding problem then add more transactions/

    Account importTargetAccount = Account.builder()
        .name(IMPORT_TARGET_ACCOUNT_NAME)
        .type(AccountType.builder().id(16L).name("Credit").build())
        .balance(convertDoubleToBigDecimal(1000))
        .currency(Currency.builder().id(13L).name("Pln").exchangeRate(BigDecimal.valueOf(1.00)).build())
        .build();
    final long importTargetAccountId = callRestServiceToAddAccountAndReturnId(importTargetAccount, token);

    final byte[] fileBytes = Files.readAllBytes(Paths.get(path));
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "file", APPLICATION_VND_MS_EXCEL, fileBytes);

    //When
    List<Transaction> parsedTransactions = callRestToImportTransactionsFromCsvFileAndReturnParsedTransactions(mockMultipartFile,
        importTargetAccountId);

    //Then
    assertThat(parsedTransactions.size(), is(equalTo(6)));
  }

  @Test
  void shouldNotAddAgainTransactionsOnAttemptOfImportTransactionsAlreadyImported() throws Exception {
    //Given
    final String path = "src/test/resources/onlyValidTransactionsCsvIntegrationTestNoPolishLetters.csv";
    //fixme lukasz solve encoding problem then add more transactions/

    Account importTargetAccount = Account.builder()
        .name(IMPORT_TARGET_ACCOUNT_NAME)
        .type(AccountType.builder().id(16L).name("Credit").build())
        .balance(convertDoubleToBigDecimal(1000))
        .currency(Currency.builder().id(13L).name("Pln").exchangeRate(BigDecimal.valueOf(1.00)).build())
        .build();
    final long importTargetAccountId = callRestServiceToAddAccountAndReturnId(importTargetAccount, token);

    final byte[] fileBytes = Files.readAllBytes(Paths.get(path));
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "file", APPLICATION_VND_MS_EXCEL, fileBytes);

    List<Transaction> parsedTransactions = callRestToImportTransactionsFromCsvFileAndReturnParsedTransactions(mockMultipartFile,
        importTargetAccountId);
    final List<Transaction> allTransactionsFromDbAfterFirstFileImport = callRestToGetAllTransactionsFromDatabase(token);
    assertThat(parsedTransactions.size(), is(equalTo(6)));
    assertThat(allTransactionsFromDbAfterFirstFileImport.size(), is(equalTo(6)));

    //When
    List<Transaction> parsedTransactionsAfterAnotherAttemptOfParsingTheSameCsvFile =
        callRestToImportTransactionsFromCsvFileAndReturnParsedTransactions(mockMultipartFile, importTargetAccountId);
    final List<Transaction> allTransactionsFromDbAfterSecondImportTheSameFile = callRestToGetAllTransactionsFromDatabase(token);

    //Then
    assertThat(parsedTransactionsAfterAnotherAttemptOfParsingTheSameCsvFile.size(), is(equalTo(0)));
    assertThat(allTransactionsFromDbAfterSecondImportTheSameFile.size(), is(equalTo(6)));
    assertThat(allTransactionsFromDbAfterSecondImportTheSameFile, is(equalTo(allTransactionsFromDbAfterFirstFileImport)));
  }

  @Test
  void shouldReturnBadRequestWhenUploadingCsvFileForNotExistingTargetAccount() throws Exception {
    //Given
    final String path = "src/test/resources/onlyValidTransactionsCsvIntegrationTestNoPolishLetters.csv";
    final byte[] fileBytes = Files.readAllBytes(Paths.get(path));
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "file", APPLICATION_VND_MS_EXCEL, fileBytes);

    //When
    int status = callRestToImportTransactionsFromCsvFileAndReturnStatus(NOT_EXISTING_TARGET_ACCOUNT, mockMultipartFile);

    //Then
    assertThat(status, is(equalTo(HttpStatus.BAD_REQUEST.value())));
  }
}
