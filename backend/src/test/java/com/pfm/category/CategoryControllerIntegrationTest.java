package com.pfm.category;

import static com.pfm.config.MessagesProvider.CANNOT_DELETE_PARENT_CATEGORY;
import static com.pfm.config.MessagesProvider.CATEGORIES_CYCLE_DETECTED;
import static com.pfm.config.MessagesProvider.CATEGORY_WITH_PROVIDED_NAME_ALREADY_EXISTS;
import static com.pfm.config.MessagesProvider.EMPTY_CATEGORY_NAME;
import static com.pfm.config.MessagesProvider.PROVIDED_PARENT_CATEGORY_NOT_EXIST;
import static com.pfm.config.MessagesProvider.getMessage;
import static com.pfm.helpers.TestCategoryProvider.categoryCar;
import static com.pfm.helpers.TestCategoryProvider.categoryHome;
import static com.pfm.helpers.TestCategoryProvider.categoryOil;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pfm.IntegrationTestsBase;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

@RunWith(JUnitParamsRunner.class)
public class CategoryControllerIntegrationTest extends IntegrationTestsBase {

  //TODO change JunitPArams to Junit5 to avoid CLASS RULE
  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Test
  public void shouldAddCategory() throws Exception {
    //given

    //when
    long carCategoryId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryCar()));

    long oilCategoryId = callRestToaddCategoryWithSpecifiedParentCategoryIdAndReturnId(carCategoryId, categoryToCategoryRequest(categoryOil()));

    //then
    Category expectedCarCategory = categoryCar();
    expectedCarCategory.setId(carCategoryId);
    Category expectedOilCategory = categoryOil();
    expectedOilCategory.setId(oilCategoryId);
    expectedOilCategory.setParentCategory(expectedCarCategory);

    List<Category> categories = callRestToGetAllCategories();

    assertThat(categories.size(), is(2));
    assertThat(categories, containsInAnyOrder(expectedCarCategory, expectedOilCategory));
  }

  @Test
  @Parameters(method = "emptyAccountNameParameters")
  public void shouldReturnErrorCauseByEmptyNameFiled(String name) throws Exception {
    //given

    CategoryRequest categoryToAdd = CategoryRequest.builder().name(name).build();

    //when
    mockMvc
        .perform(
            post(CATEGORIES_SERVICE_PATH)
                .content(json(categoryToAdd))
                .contentType(JSON_CONTENT_TYPE)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0]", is(getMessage(EMPTY_CATEGORY_NAME))));
  }

  @SuppressWarnings("unused")
  private Object[] emptyAccountNameParameters() {
    return new Object[]{"", " ", "    ", null};
  }

  @Test
  public void shouldReturnErrorCausedByNameAlreadyExist() throws Exception {
    //given
    callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryCar()));
    CategoryRequest categoryToAdd = CategoryRequest.builder().name(categoryCar().getName())
        .build();

    //when
    mockMvc
        .perform(
            post(CATEGORIES_SERVICE_PATH)
                .content(json(categoryToAdd))
                .contentType(JSON_CONTENT_TYPE)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0]", is(getMessage(CATEGORY_WITH_PROVIDED_NAME_ALREADY_EXISTS))));
  }

  @Test
  public void shouldGetCategories() throws Exception {
    //when
    long categoryCarId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryCar()));
    long categoryHomeId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryHome()));
    List<Category> categories = callRestToGetAllCategories();

    //then
    Category expectedCarCategory = categoryCar();
    expectedCarCategory.setId(categoryCarId);
    Category expectedHomeCategory = categoryHome();
    expectedHomeCategory.setId(categoryHomeId);

    assertThat(categories.size(), is(2));
    assertThat(categories, containsInAnyOrder(expectedCarCategory, expectedHomeCategory));
  }

  @Test
  public void shouldGetCategoryById() throws Exception {

    //given
    long categoryCarId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryCar()));

    //when
    Category actualCarCategory = callRestToGetCategoryById(categoryCarId);
    Category expectedCarCategory = categoryCar();
    expectedCarCategory.setId(categoryCarId);

    assertThat(actualCarCategory, is(equalTo(expectedCarCategory)));
  }

  @Test
  public void shouldReturnErrorCausedWrongIdProvidedGetMethod() throws Exception {

    //when
    mockMvc
        .perform(get(CATEGORIES_SERVICE_PATH + "/" + NOT_EXISTING_ID))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldUpdateCategory() throws Exception {

    //given
    long homeCategoryId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryHome()));
    Category categoryToUpdate = categoryHome();
    categoryToUpdate.setName("Second Home");

    //when
    callRestToUpdateCategory(homeCategoryId, categoryToCategoryRequest(categoryToUpdate));

    //then
    Category expectedCategory = categoryToUpdate;
    expectedCategory.setId(homeCategoryId);
    Category result = callRestToGetCategoryById(homeCategoryId);
    assertThat(result, is(equalTo(expectedCategory)));
  }

  @Test
  public void shouldUpdateSubCategory() throws Exception {
    //given
    long categoryCarId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryCar()));
    long categoryOilId = callRestToaddCategoryWithSpecifiedParentCategoryIdAndReturnId(categoryCarId, categoryToCategoryRequest(categoryOil()));
    CategoryRequest categoryOilToUpdate = CategoryRequest.builder()
        .name("Mannol Oil")
        .build();

    //when
    callRestToUpdateCategory(categoryOilId, categoryOilToUpdate);

    //given
    Category result = callRestToGetCategoryById(categoryOilId);
    assertThat(result, is(equalTo(convertCategoryRequestToCategoryAndSetId(categoryOilId, categoryOilToUpdate))));
  }

  @Test
  public void shouldReturnErrorCausedWrongIdProvidedUpdateMethod() throws Exception {

    //when
    mockMvc
        .perform(put(CATEGORIES_SERVICE_PATH + "/" + NOT_EXISTING_ID)
            .content(json(categoryToCategoryRequest(categoryOil()))).contentType(JSON_CONTENT_TYPE))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldReturnErrorCausedByNotExistingParentCategoryIdProvided() throws Exception {

    //given
    long categoryId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryCar()));
    CategoryRequest categoryToUpdate = categoryToCategoryRequest(categoryCar());
    categoryToUpdate.setParentCategoryId(NOT_EXISTING_ID);

    //when
    mockMvc
        .perform(put(CATEGORIES_SERVICE_PATH + "/" + categoryId)
            .content(json(categoryToUpdate)).contentType(JSON_CONTENT_TYPE))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0]", is(getMessage(PROVIDED_PARENT_CATEGORY_NOT_EXIST))));
  }

  @Test
  public void shouldReturnErrorCausedByCycling()
      throws Exception {
    //given
    long carCategoryId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryCar()));
    long oilCategoryId = callRestToaddCategoryWithSpecifiedParentCategoryIdAndReturnId(carCategoryId, categoryToCategoryRequest(categoryOil()));
    CategoryRequest carCategoryToUpdate = CategoryRequest.builder()
        .parentCategoryId(oilCategoryId)
        .build();
    carCategoryToUpdate.setName("Car");

    //when
    performUpdateRequestAndAssertCycleErrorIsReturned(carCategoryId, carCategoryToUpdate);
  }

  @Test
  public void shouldReturnErrorCausedBySettingCategoryToBeSelfParentCategory()
      throws Exception {
    //given
    long oilCategoryId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryOil()));
    CategoryRequest categoryOilToUpdate = CategoryRequest.builder().name(categoryOil().getName())
        .parentCategoryId(oilCategoryId).build();

    //when
    performUpdateRequestAndAssertCycleErrorIsReturned(oilCategoryId, categoryOilToUpdate);
  }

  private void performUpdateRequestAndAssertCycleErrorIsReturned(long id, CategoryRequest categoryToUpdate) throws Exception {
    mockMvc
        .perform(put(CATEGORIES_SERVICE_PATH + "/" + id)
            .content(json(categoryToUpdate)).contentType(JSON_CONTENT_TYPE))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0]", is(getMessage(CATEGORIES_CYCLE_DETECTED))));

  }

  @Test
  public void shouldDeleteCategory() throws Exception {

    //given
    long homeCategoryId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryHome()));
    callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryOil()));

    //when
    callRestToDeleteCategoryById(homeCategoryId);

    //then
    List<Category> categories = callRestToGetAllCategories();
    Category deletedCategory = convertCategoryRequestToCategoryAndSetId(homeCategoryId, categoryToCategoryRequest(categoryHome()));
    assertThat(categories.size(), is(equalTo(1)));
    assertFalse(categories.contains(deletedCategory));
  }

  @Test
  public void shouldDeleteSubCategoryAndThenParentCategory() throws Exception {

    //given
    long carCategoryId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryCar()));
    long oilCategoryId = callRestToaddCategoryWithSpecifiedParentCategoryIdAndReturnId(carCategoryId, categoryToCategoryRequest(categoryOil()));

    //when
    callRestToDeleteCategoryById(oilCategoryId);
    callRestToDeleteCategoryById(carCategoryId);

    //then
    List<Category> categories = callRestToGetAllCategories();
    assertThat(categories.size(), is(0));
  }

  @Test
  public void shouldReturnErrorCausedByTryingToDeleteParentCategoryOfSubCategory()
      throws Exception {

    //given
    long carCategoryId = callRestToaddCategoryAndReturnId(categoryToCategoryRequest(categoryCar()));
    callRestToaddCategoryWithSpecifiedParentCategoryIdAndReturnId(carCategoryId, categoryToCategoryRequest(categoryOil()));

    //when
    mockMvc.perform(delete(CATEGORIES_SERVICE_PATH + "/" + carCategoryId))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(getMessage(CANNOT_DELETE_PARENT_CATEGORY)));
  }

  @Test
  public void shouldReturnErrorCausedWrongIdProvidedDeleteMethod() throws Exception {

    //when
    mockMvc.perform(delete(CATEGORIES_SERVICE_PATH + "/" + NOT_EXISTING_ID))
        .andExpect(status().isNotFound());
  }
}
