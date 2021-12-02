package com.sap.olingo.jpa.processor.cb.impl;

enum SqlArithmetic {

  SUM("+"),
  PROD("*"),
  DIFF("-"),
  MOD("%"),
  QUOT("/");

  private String keyWord;

  private SqlArithmetic(final String keyWord) {
    this.keyWord = keyWord;
  }

  @Override
  public String toString() {
    return keyWord;
  }
}
