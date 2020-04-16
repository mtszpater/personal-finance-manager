package com.pfm.config.csv;

import com.opencsv.bean.processor.StringProcessor;

public class PreAssigmentProcessor implements StringProcessor {

  @Override
  public String processString(String value) {
    return value.trim();
  }

  @Override
  public void setParameterString(String value) { }
}
