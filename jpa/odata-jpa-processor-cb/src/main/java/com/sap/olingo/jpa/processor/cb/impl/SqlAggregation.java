package com.sap.olingo.jpa.processor.cb.impl;

enum SqlAggregation {

  AVG("AVG"),
  COUNT("COUNT"),
  SUM("SUM"),
  MAX("MAX"),
  MIN("MIN");

  private String keyWord;

  private SqlAggregation(final String keyWord) {
    this.keyWord = keyWord;
  }

  @Override
  public String toString() {
    return keyWord;
  }
}
