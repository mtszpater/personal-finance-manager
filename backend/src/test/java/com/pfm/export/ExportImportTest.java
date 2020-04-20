package com.pfm.export;

import static com.pfm.export.ImportService.CATEGORY_NAMED_IMPORTED;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pfm.account.AccountService;
import com.pfm.account.type.AccountTypeService;
import com.pfm.auth.UserProvider;
import com.pfm.category.Category;
import com.pfm.category.CategoryRepository;
import com.pfm.category.CategoryService;
import com.pfm.currency.CurrencyService;
import com.pfm.export.ExportResult.ExportCategory;
import com.pfm.filter.FilterService;
import com.pfm.history.HistoryEntryRepository;
import com.pfm.transaction.TransactionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportImportTest {

  private static final long MOCK_USER_ID = 99L;

  @Mock
  private CategoryService categoryService;

  @Mock
  private UserProvider userProvider;

  @Mock
  private CurrencyService currencyService;

  @Mock
  private AccountService accountService;

  @Mock
  private AccountTypeService accountTypeService;

  @Mock
  private FilterService filterService;

  @Mock
  private TransactionService transactionService;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private HistoryEntryRepository historyEntryRepository;

  @Spy
  @InjectMocks
  private ImportService importService;

  //FIXME lukasz is that righ place/name for theese tests
  @Test
  void shouldThrowExceptionWhenMoreThanOneCategoryWithNameImportedFoundDuringImportingData() {
    //Given
    final Category importedCategory1 = Category.builder()
        .name(CATEGORY_NAMED_IMPORTED)
        .parentCategory(null)
        .userId(MOCK_USER_ID)
        .build();

    final Category importedCategory2 = Category.builder()
        .id(2L)
        .name(CATEGORY_NAMED_IMPORTED)
        .parentCategory(null)
        .userId(MOCK_USER_ID)
        .build();

    when(categoryRepository.findByNameIgnoreCaseAndUserId(CATEGORY_NAMED_IMPORTED, MOCK_USER_ID))
        .thenReturn(List.of(importedCategory1, importedCategory2));

    //Then
    assertThrows(IllegalStateException.class, () -> importService.deleteCategoryNamedImportedFromDbIfAlreadyExistToPreventDuplication(MOCK_USER_ID));

  }

  @Test
  void shouldDeleteExistingCategoryWithNameImportedDuringImportingDataWhenImportedDataContainCategoryWithNameImported() throws Exception {
    //Given
    final Category importedCategory = Category.builder()
        .name(CATEGORY_NAMED_IMPORTED)
        .priority(1000)
        .parentCategory(null)
        .userId(MOCK_USER_ID)
        .build();
    ExportResult input = new ExportResult();
    input.setCategories(List.of(
        ExportCategory.builder()
            .name(importedCategory.getName())
            .build()
        )
    );
    when(importService.sortCategoriesTopologically(input.getCategories())).thenReturn(input.getCategories());
    doNothing().when(importService).deleteCategoryNamedImportedFromDbIfAlreadyExistToPreventDuplication(MOCK_USER_ID);
    when(categoryService.addCategory(importedCategory, MOCK_USER_ID)).thenReturn(importedCategory);

    //When
    importService.importData(input, MOCK_USER_ID);

    //Then
    verify(importService, times(1)).sortCategoriesTopologically(input.getCategories());
    verify(importService, times(1)).deleteCategoryNamedImportedFromDbIfAlreadyExistToPreventDuplication(MOCK_USER_ID);
    verify(categoryService, times(1)).addCategory(importedCategory, MOCK_USER_ID);
    verify(currencyService, times(1)).getCurrencies(MOCK_USER_ID);
    verify(accountTypeService, times(1)).getAccountTypes(MOCK_USER_ID);

    verify(userProvider, never()).getCurrentUserId();
    verify(accountService, never()).getAccounts(MOCK_USER_ID);
    verify(filterService, never()).getAllFilters(MOCK_USER_ID);
    verify(transactionService, never()).getTransactions(MOCK_USER_ID);
    verify(historyEntryRepository, never()).findByUserId(MOCK_USER_ID);

  }

}
