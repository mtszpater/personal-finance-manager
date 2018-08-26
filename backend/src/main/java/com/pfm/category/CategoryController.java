package com.pfm.category;

import static com.pfm.config.MessagesProvider.CANNOT_DELETE_PARENT_CATEGORY;
import static com.pfm.config.MessagesProvider.getMessage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("categories")
@RestController
@Slf4j
@AllArgsConstructor
@CrossOrigin
@Api(value = "Category", description = "Controller used to list / add / update / delete categories.")
public class CategoryController {

  private CategoryService categoryService;
  private CategoryValidator categoryValidator;

  public static Category convertToCategory(@RequestBody CategoryRequest categoryRequest) {
    Long parentCategoryId = categoryRequest.getParentCategoryId();

    if (parentCategoryId == null) {
      return new Category(null, categoryRequest.getName(), null);
    }

    return new Category(null, categoryRequest.getName(), new Category(parentCategoryId, null, null));
  }

  @ApiOperation(value = "Find category by id", response = Category.class)
  @GetMapping(value = "/{id}")
  public ResponseEntity<Category> getCategoryById(@PathVariable long id) {

    log.info("Retrieving category with id: {}", id);
    Optional<Category> category = categoryService.getCategoryById(id);

    if (!category.isPresent()) {
      log.info("Category with id {} was not found", id);
      return ResponseEntity.notFound().build();
    }

    log.info("Category with id {} was successfully retrieved", id);
    return ResponseEntity.ok(category.get());
  }

  @ApiOperation(value = "Get list of categories", response = Category.class, responseContainer = "List")
  @GetMapping
  public ResponseEntity<List<Category>> getCategories() {

    log.info("Retrieving categories from database");
    List<Category> categories = categoryService.getCategories();
    return ResponseEntity.ok(categories);
  }

  @ApiOperation(value = "Create a new category", response = Long.class)
  @PostMapping
  public ResponseEntity<?> addCategory(@RequestBody CategoryRequest categoryRequest) {

    log.info("Saving category {} to the database", categoryRequest.getName());
    Category category = convertToCategory(categoryRequest);

    List<String> validationResult = categoryValidator.validateCategoryForAdd(category);
    if (!validationResult.isEmpty()) {
      log.info("Category is not valid {}", validationResult);
      return ResponseEntity.badRequest().body(validationResult);
    }

    Category createdCategory = categoryService.addCategory(category);
    log.info("Saving category to the database was successful. Category id is {}", createdCategory.getId());

    return ResponseEntity.ok(createdCategory.getId());
  }

  @ApiOperation(value = "Update an existing category", response = Void.class)
  @PutMapping(value = "/{id}")
  public ResponseEntity<?> updateCategory(@PathVariable long id,
      @RequestBody CategoryRequest categoryRequest) {

    if (!categoryService.idExist(id)) {
      log.info("No category with id {} was found, not able to update", id);
      return ResponseEntity.notFound().build();
    }

    Category category = convertToCategory(categoryRequest);
    category.setId(id);

    List<String> validationResult = categoryValidator.validateCategoryForUpdate(id, category);
    if (!validationResult.isEmpty()) {
      log.error("Category is not valid {}", validationResult);
      return ResponseEntity.badRequest().body(validationResult);
    }

    categoryService.updateCategory(id, category);
    log.info("Category with id {} was successfully updated", id);
    return ResponseEntity.ok().build();
  }

  // TODO deleting category used in transaction / filter fails with ugly error
  @ApiOperation(value = "Delete an existing category", response = Void.class)
  @DeleteMapping(value = "/{id}")
  public ResponseEntity<?> deleteCategory(@PathVariable long id) {

    if (!categoryService.getCategoryById(id).isPresent()) {
      log.info("No category with id {} was found, not able to delete", id);
      return ResponseEntity.notFound().build();
    }
    if (categoryService.isParentCategory(id)) {
      log.info("Category is used as parent. category Delete category {} not possible", id);
      return ResponseEntity.badRequest().body(getMessage(CANNOT_DELETE_PARENT_CATEGORY));
    }
    log.info("Attempting to delete category with id {}", id);
    categoryService.deleteCategory(id);

    log.info("Category with id {} was deleted successfully", id);
    return ResponseEntity.ok().build();
  }

  // TODO separate class
  @JsonIgnoreProperties(ignoreUnknown = true)
  @AllArgsConstructor
  @Builder
  @Setter
  @Getter
  public static class CategoryRequest {

    @ApiModelProperty(value = "Parent category id", example = "1")
    private Long parentCategoryId;

    @ApiModelProperty(value = "Category name", required = true, example = "Eating out")
    private String name;
  }
}