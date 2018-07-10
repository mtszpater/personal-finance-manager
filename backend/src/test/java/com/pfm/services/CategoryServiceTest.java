package com.pfm.services;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pfm.category.Category;
import com.pfm.category.CategoryRepository;
import com.pfm.category.CategoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CategoryServiceTest {

  private static final Long MOCK_CATEGORY_ID = 1L;
  private static final String MOCK_CATEGORY_NAME = "Food";

  private static final Long MOCK_CATEGORY_WITH_PARENT_CATEGORY_ID = 2L;
  private static final String MOCK_CATEGORY_WITH_PARENT_CATEGORY_NAME = "Meat";

  private static Category mockCategory =
      new Category(MOCK_CATEGORY_ID, MOCK_CATEGORY_NAME, null);
  private static Category mockCategoryWithParentCategory =
      new Category(MOCK_CATEGORY_WITH_PARENT_CATEGORY_ID, MOCK_CATEGORY_WITH_PARENT_CATEGORY_NAME,
          mockCategory);

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategoryService categoryService;

  @Test
  public void shouldGetCategoryById() {
    //given
    when(categoryRepository.findById(MOCK_CATEGORY_ID)).thenReturn(Optional.of(mockCategory));
    //when
    categoryService.getCategoryById(MOCK_CATEGORY_ID);
    //then
    verify(categoryRepository).findById(MOCK_CATEGORY_ID);
  }

  @Test
  public void shouldGetCategories() {
    //given
    when(categoryRepository.findAll()).thenReturn(Collections.singletonList(mockCategory));
    //when
    categoryService.getCategories();
    //then
    verify(categoryRepository).findAll();
  }

  @Test
  public void shouldAddCategory() {
    //given
    when(categoryRepository.save(mockCategory)).thenReturn(mockCategory);
    when(categoryRepository.findById(MOCK_CATEGORY_ID))
        .thenReturn(Optional.of(mockCategory));
    when(categoryRepository.save(mockCategoryWithParentCategory))
        .thenReturn(mockCategoryWithParentCategory);
    //when
    categoryService.addCategory(mockCategory);
    categoryService.addCategory(mockCategoryWithParentCategory);
    //then
    verify(categoryRepository).save(mockCategory);
    verify(categoryRepository).findById(MOCK_CATEGORY_ID);
    verify(categoryRepository).save(mockCategoryWithParentCategory);
  }

  @Test
  public void shouldDeleteCategory() {
    //given
    doNothing().when(categoryRepository).deleteById(MOCK_CATEGORY_ID);
    //when
    categoryService.removeCategory(MOCK_CATEGORY_ID);
    //then
    verify(categoryRepository).deleteById(MOCK_CATEGORY_ID);
  }

  @Test
  public void shouldUpdateCategory() {
    //given
    when(categoryRepository.save(mockCategory)).thenReturn(mockCategory);
    when(categoryRepository.findById(MOCK_CATEGORY_ID))
        .thenReturn(Optional.of(mockCategory));
    when(categoryRepository.findById(MOCK_CATEGORY_WITH_PARENT_CATEGORY_ID))
        .thenReturn(Optional.of(mockCategoryWithParentCategory));
    when(categoryRepository.save(mockCategoryWithParentCategory))
        .thenReturn(mockCategoryWithParentCategory);
    //when
    categoryService.updateCategory(mockCategory);
    categoryService.updateCategory(mockCategoryWithParentCategory);
    //then
    verify(categoryRepository).save(mockCategory);
    verify(categoryRepository, times(2)).findById(MOCK_CATEGORY_ID);
    verify(categoryRepository).findById(MOCK_CATEGORY_WITH_PARENT_CATEGORY_ID);
    verify(categoryRepository).save(mockCategoryWithParentCategory);
  }

  @Test
  public void shouldCheckIfCategoryExist() {
    //given
    when(categoryRepository.existsById(MOCK_CATEGORY_ID)).thenReturn(true);
    //when
    categoryService.idExist(MOCK_CATEGORY_ID);
    //then
    verify(categoryRepository).existsById(MOCK_CATEGORY_ID);
  }


}
