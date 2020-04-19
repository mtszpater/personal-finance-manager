package com.pfm.auth;

import static com.pfm.export.ImportService.CATEGORY_NAMED_IMPORTED;

import com.pfm.account.type.AccountTypeService;
import com.pfm.category.Category;
import com.pfm.category.CategoryService;
import com.pfm.currency.CurrencyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserInitializationService {

  private CurrencyService currencyService;
  private AccountTypeService accountTypeService;
  private CategoryService categoryService;

  public void initializeUser(long userId) {
    currencyService.addDefaultCurrencies(userId);
    accountTypeService.addDefaultAccountTypes(userId);

    initializeCategoriesByCreatingCategoryWithNameImported(userId);
  }

  private void initializeCategoriesByCreatingCategoryWithNameImported(long userId) {
    final boolean categoryNamedImportedDoesNotExist = !categoryService.isCategoryNameAlreadyUsed(CATEGORY_NAMED_IMPORTED, userId);
    if (categoryNamedImportedDoesNotExist) {
      categoryService.addCategory(
          Category.builder()
              .name(CATEGORY_NAMED_IMPORTED)
              .parentCategory(null)
              .build(),
          userId
      );
    }
  }

}
