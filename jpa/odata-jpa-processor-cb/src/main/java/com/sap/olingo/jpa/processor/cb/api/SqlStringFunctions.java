package com.sap.olingo.jpa.processor.cb.api;

public enum SqlStringFunctions {

  LOWER("LOWER"),
  UPPER("UPPER"),
  LENGTH("LENGTH"),
  TRIM("TRIM"),
  SUBSTRING("SUBSTRING"),
  CONCAT("CONCAT"),
  LOCATE("LOCATE");

  private String keyWord;

  private SqlStringFunctions(final String keyWord) {
    this.keyWord = keyWord;
  }

  @Override
  public String toString() {
    return keyWord;
  }
}
