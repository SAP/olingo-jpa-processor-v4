package com.sap.olingo.jpa.processor.cb.impl;

enum SqlTimeFunctions {

  TIMESTAMP("CURRENT_TIMESTAMP"),
  DATE("CURRENT_DATE"),
  TIME("CURRENT_TIME");

  private String keyWord;

  private SqlTimeFunctions(final String keyWord) {
    this.keyWord = keyWord;
  }

  @Override
  public String toString() {
    return keyWord;
  }
}
