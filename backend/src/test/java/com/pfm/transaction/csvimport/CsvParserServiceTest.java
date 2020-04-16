package com.pfm.transaction.csvimport;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pfm.account.AccountRepository;
import com.pfm.auth.UserProvider;
import com.pfm.category.Category;
import com.pfm.category.CategoryRepository;
import com.pfm.helpers.TestCategoryProvider;
import com.pfm.transaction.TransactionRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class CsvParserServiceTest {

  private static final long MOCK_USER_ID = 99;
  private static final long MOCK_TARGET_ACCOUNT_ID = 7364L;
  private static final String MOCK_CATEGORY_NAMED_IMPORTED = "Imported";
  private static final Set<String> MOCK_ALL_INTERNAL_IDS = Set.of("1", "3", "5");

  @Mock
  private UserProvider userProvider;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private CsvParser csvParser;

  @Mock
  private MultipartFile file;

  @InjectMocks
  private CsvParserService csvParserService;

  @Test
  void shouldPropagateUpExceptionThrownByCsvParser() throws TransactionsParsingException {
    //Given
    Category categoryAnimals = TestCategoryProvider.categoryAnimals();
    Category categoryFood = TestCategoryProvider.categoryFood();

    when(userProvider.getCurrentUserId()).thenReturn(MOCK_USER_ID);
    when(accountRepository.getAccountIdByName(MOCK_USER_ID)).thenReturn(MOCK_TARGET_ACCOUNT_ID);
    when(categoryRepository.findByNameIgnoreCaseAndUserId(MOCK_CATEGORY_NAMED_IMPORTED, MOCK_USER_ID))
        .thenReturn(List.of(categoryAnimals, categoryFood));
    when(transactionRepository.getAllInternalIds(MOCK_USER_ID)).thenReturn(MOCK_ALL_INTERNAL_IDS);
    when(csvParser.parse(any())).thenThrow(TransactionsParsingException.class);

    //Then
    assertThrows(TransactionsParsingException.class, () -> csvParserService.convertToTransactions(file));
    verify(userProvider, times(1)).getCurrentUserId();
    verify(accountRepository, times(1)).getAccountIdByName(MOCK_USER_ID);
    verify(categoryRepository, times(1)).findByNameIgnoreCaseAndUserId(MOCK_CATEGORY_NAMED_IMPORTED, MOCK_USER_ID);
    verify(transactionRepository, times(1)).getAllInternalIds(MOCK_USER_ID);
    verify(csvParser, times(1)).parse(any());
  }

}
