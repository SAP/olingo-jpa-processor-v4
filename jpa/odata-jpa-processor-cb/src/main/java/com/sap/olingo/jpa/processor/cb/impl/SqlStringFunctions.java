package com.sap.olingo.jpa.processor.cb.impl;

enum SqlStringFunctions {

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
