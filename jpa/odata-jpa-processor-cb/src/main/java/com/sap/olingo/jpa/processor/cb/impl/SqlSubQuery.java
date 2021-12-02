package com.sap.olingo.jpa.processor.cb.impl;

enum SqlSubQuery {

  EXISTS("EXISTS"),
  SOME("SOME"),
  ALL("ALL"),
  ANY("ANY");

  private String keyWord;

  private SqlSubQuery(final String keyWord) {
    this.keyWord = keyWord;
  }

  @Override
  public String toString() {
    return keyWord;
  }
}
