package com.sap.olingo.jpa.processor.cb.impl;

enum SqlNullCheck {

  NULL("IS NULL"),
  NOT_NULL("IS NOT NULL");

  private String keyWord;

  private SqlNullCheck(final String keyWord) {
    this.keyWord = keyWord;
  }

  @Override
  public String toString() {
    return keyWord;
  }
}
