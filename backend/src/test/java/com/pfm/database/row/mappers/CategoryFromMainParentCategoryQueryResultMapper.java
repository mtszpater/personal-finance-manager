package com.pfm.database.row.mappers;

import com.pfm.database.query.result.CategoryFromMainParentCategoryQueryResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class CategoryFromMainParentCategoryQueryResultMapper implements RowMapper<CategoryFromMainParentCategoryQueryResult> {

  @Override
  public CategoryFromMainParentCategoryQueryResult mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
    return CategoryFromMainParentCategoryQueryResult.builder()
        .name(resultSet.getString("name"))
        .build();
  }
}
