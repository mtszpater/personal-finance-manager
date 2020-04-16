package com.pfm.export;

import static com.pfm.export.ImportHelper.CATEGORY_NAMED_IMPORTED;
import static com.pfm.helpers.TestCategoryProvider.categoryFood;
import static com.pfm.helpers.TestCategoryProvider.categoryHome;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pfm.category.Category;
import com.pfm.category.CategoryRepository;
import com.pfm.category.CategoryService;
import com.pfm.export.ExportResult.ExportAccount;
import com.pfm.export.ExportResult.ExportAccountPriceEntry;
import com.pfm.export.ExportResult.ExportCategory;
import com.pfm.export.ExportResult.ExportFundsSummary;
import com.pfm.export.ExportResult.ExportPeriod;
import com.pfm.export.ExportResult.ExportTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImportHelperTest {
  private static final long MOCK_USER_ID = 999;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private CategoryService categoryService;

  @InjectMocks
  private ImportHelper importHelper;

  @Test
  void shouldThrowExceptionWhenMoreThanOneCategoryWithNameImportedFoundDuringImportingData() {
    //Given
    final Category importedCategory1 = Category.builder()
        .id(1L)
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

    final ExportResult input = new ExportResult();
    input.setCategories(List.of(
        ExportCategory.builder()
            .name(CATEGORY_NAMED_IMPORTED)
            .build(),
        ExportCategory.builder()
            .name(categoryHome().getName())
            .build(),
        ExportCategory.builder()
            .name(categoryFood().getName())
            .parentCategoryName(categoryHome().getName())
            .build()
        )
    );

    ExportAccount aliorAccount = ExportAccount.builder()
        .name("Alior Bank")
        .balance(BigDecimal.TEN)
        .currency("USD")
        .build();

    ExportAccount ideaBankAccount = ExportAccount.builder()
        .name("Idea Bank")
        .balance(BigDecimal.ZERO)
        // should default to PLN
        .build();

    input.setInitialAccountsState(List.of(aliorAccount, ideaBankAccount));
    input.setFinalAccountsState(List.of(aliorAccount, ideaBankAccount));

    ExportAccountPriceEntry entry = ExportAccountPriceEntry.builder()
        .account(aliorAccount.getName())
        .price(BigDecimal.valueOf(-124))
        .build();

    ExportTransaction transaction = ExportTransaction.builder()
        .category(categoryFood().getName())
        .date(LocalDate.now())
        .description("McDonalds")
        .accountPriceEntries(Collections.singletonList(entry))
        .build();

    ExportPeriod period = ExportPeriod.builder()
        .accountStateAtTheBeginningOfPeriod(List.of(aliorAccount, ideaBankAccount))
        .accountStateAtTheEndOfPeriod(List.of(aliorAccount, ideaBankAccount))
        .startDate(LocalDate.MIN)
        .endDate(LocalDate.MAX)
        .transactions(Collections.singletonList(transaction))
        .sumOfAllFundsAtTheBeginningOfPeriod(ExportFundsSummary.builder().sumOfAllFundsInBaseCurrency(BigDecimal.TEN).build())
        .sumOfAllFundsAtTheEndOfPeriod(ExportFundsSummary.builder().sumOfAllFundsInBaseCurrency(BigDecimal.TEN).build())
        .build();

    input.setPeriods(Collections.singletonList(period));

    when(categoryRepository.findByNameIgnoreCaseAndUserId(CATEGORY_NAMED_IMPORTED, MOCK_USER_ID))
        .thenReturn(List.of(importedCategory1, importedCategory2));

    //Then
    assertThrows(IllegalStateException.class, () -> importHelper.deleteCategoryNamedImportedFromDbIfAlreadyExistToPreventDuplication(MOCK_USER_ID));

  }

  @Test
  void shouldDeleteExistingCategoryWithNameImportedDuringImportingDataWhenImportedDataContainCategoryWithNameImported() {
    //Given
    final long userId = 1L;

    final Category importedCategory = Category.builder()
        .id(1L)
        .name(CATEGORY_NAMED_IMPORTED)
        .parentCategory(null)
        .userId(userId)
        .build();

    final ExportResult input = new ExportResult();
    input.setCategories(List.of(
        ExportCategory.builder()
            .name(CATEGORY_NAMED_IMPORTED)
            .build(),
        ExportCategory.builder()
            .name(categoryHome().getName())
            .build(),
        ExportCategory.builder()
            .name(categoryFood().getName())
            .parentCategoryName(categoryHome().getName())
            .build()
        )
    );

    ExportAccount aliorAccount = ExportAccount.builder()
        .name("Alior Bank")
        .balance(BigDecimal.TEN)
        .currency("USD")
        .build();

    ExportAccount ideaBankAccount = ExportAccount.builder()
        .name("Idea Bank")
        .balance(BigDecimal.ZERO)
        // should default to PLN
        .build();

    input.setInitialAccountsState(List.of(aliorAccount, ideaBankAccount));
    input.setFinalAccountsState(List.of(aliorAccount, ideaBankAccount));

    ExportAccountPriceEntry entry = ExportAccountPriceEntry.builder()
        .account(aliorAccount.getName())
        .price(BigDecimal.valueOf(-124))
        .build();

    ExportTransaction transaction = ExportTransaction.builder()
        .category(categoryFood().getName())
        .date(LocalDate.now())
        .description("McDonalds")
        .accountPriceEntries(Collections.singletonList(entry))
        .build();

    ExportPeriod period = ExportPeriod.builder()
        .accountStateAtTheBeginningOfPeriod(List.of(aliorAccount, ideaBankAccount))
        .accountStateAtTheEndOfPeriod(List.of(aliorAccount, ideaBankAccount))
        .startDate(LocalDate.MIN)
        .endDate(LocalDate.MAX)
        .transactions(Collections.singletonList(transaction))
        .sumOfAllFundsAtTheBeginningOfPeriod(ExportFundsSummary.builder().sumOfAllFundsInBaseCurrency(BigDecimal.TEN).build())
        .sumOfAllFundsAtTheEndOfPeriod(ExportFundsSummary.builder().sumOfAllFundsInBaseCurrency(BigDecimal.TEN).build())
        .build();

    input.setPeriods(Collections.singletonList(period));

    when(categoryRepository.findByNameIgnoreCaseAndUserId(CATEGORY_NAMED_IMPORTED, userId))
        .thenReturn(List.of(importedCategory));
    doNothing().when(categoryService).deleteCategory(anyLong());

    //Then
    importHelper.deleteCategoryNamedImportedFromDbIfAlreadyExistToPreventDuplication(userId);
  }

}
