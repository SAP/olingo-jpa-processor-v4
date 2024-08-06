package com.sap.olingo.jpa.processor.cb.impl;

enum SqlStringFunctions {

  LOWER("LOWER"),
  UPPER("UPPER"),
  TRIM("TRIM"),
  LENGTH("LENGTH"),
  SUBSTRING("SUBSTRING"),
  CONCAT("CONCAT"),
  LOCATE("LOCATE");

  private final String keyWord;

  private SqlStringFunctions(final String keyWord) {
    this.keyWord = keyWord;
  }

  @Override
  public String toString() {
    return keyWord;
  }
}
