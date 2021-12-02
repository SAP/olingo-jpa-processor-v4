package com.sap.olingo.jpa.processor.cb.impl;

enum SqlWindowFunctions {

  ROW_NUMBER("ROW_NUMBER");

  private String keyWord;

  private SqlWindowFunctions(final String keyWord) {
    this.keyWord = keyWord;
  }

  @Override
  public String toString() {
    return keyWord;
  }
}
