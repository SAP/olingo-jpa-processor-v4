package com.sap.olingo.jpa.processor.cb.impl;

enum SqlPagingFunctions {
  LIMIT("LIMIT", Integer.MAX_VALUE),
  OFFSET("OFFSET", 0);

  private final String keyWord;
  private final int defaultValue;

  private SqlPagingFunctions(final String keyWord, final int defaultValue) {
    this.keyWord = keyWord;
    this.defaultValue = defaultValue;
  }

  @Override
  public String toString() {
    return keyWord;
  }

  int defaultValue() {
    return defaultValue;
  }
}
