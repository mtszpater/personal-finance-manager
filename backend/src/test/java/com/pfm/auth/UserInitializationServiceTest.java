package com.pfm.auth;

import static com.pfm.export.ImportHelper.CATEGORY_NAMED_IMPORTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pfm.account.type.AccountTypeService;
import com.pfm.category.Category;
import com.pfm.category.CategoryService;
import com.pfm.currency.CurrencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserInitializationServiceTest {

  private static final long MOCK_USER_ID = 999;

  @Mock
  private CategoryService categoryService;

  @Mock
  private CurrencyService currencyService;

  @Mock
  private AccountTypeService accountTypeService;

  @InjectMocks
  private UserInitializationService userInitializationService;

  @Test
  void shouldCorrectlyInitializeUserWhenCategoryWithNameImportedAlreadyExists() {
    //Given
    doNothing().when(currencyService).addDefaultCurrencies(anyLong());
    doNothing().when(accountTypeService).addDefaultAccountTypes(anyLong());

    when(categoryService.isCategoryNameAlreadyUsed(CATEGORY_NAMED_IMPORTED, MOCK_USER_ID)).thenReturn(true);

    //When
    userInitializationService.initializeUser(MOCK_USER_ID);

    //Then
    verify(currencyService, times(1)).addDefaultCurrencies(anyLong());
    verify(accountTypeService, times(1)).addDefaultAccountTypes(anyLong());
    verify(categoryService, times(0)).addCategory(any(Category.class), eq(MOCK_USER_ID));
  }

  @Test
  void shouldCorrectlyInitializeUserWhenCategoryWithNameImportedDoesNotExist() {
    //Given
    doNothing().when(currencyService).addDefaultCurrencies(anyLong());
    doNothing().when(accountTypeService).addDefaultAccountTypes(anyLong());

    when(categoryService.isCategoryNameAlreadyUsed(CATEGORY_NAMED_IMPORTED, MOCK_USER_ID)).thenReturn(false);

    //When
    userInitializationService.initializeUser(MOCK_USER_ID);

    //Then
    verify(currencyService, times(1)).addDefaultCurrencies(anyLong());
    verify(accountTypeService, times(1)).addDefaultAccountTypes(anyLong());
    verify(categoryService, times(1)).addCategory(any(Category.class), eq(MOCK_USER_ID));

  }
}
