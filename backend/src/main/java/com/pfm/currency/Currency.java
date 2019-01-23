package com.pfm.currency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Currency {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ApiModelProperty(value = "Currency id (generated by application)", required = true, example = "1")
  private Long id;

  @ApiModelProperty(value = "Currency name", required = true, example = "EUR")
  private String name;

  @ApiModelProperty(value = "Currency exchange rate", required = true, example = "4.24")
  private BigDecimal exchangeRate;

  @JsonIgnore
  private Long userId;
}