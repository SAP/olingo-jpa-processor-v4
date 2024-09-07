package com.sap.olingo.jpa.processor.cb.impl;

import static com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider.VALUE_PLACEHOLDER;

import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;

enum SqlPagingFunctions {
  LIMIT(Integer.MAX_VALUE),
  OFFSET(0);

  private final int defaultValue;

  private SqlPagingFunctions(final int defaultValue) {
    this.defaultValue = defaultValue;
  }

  public String toString(final ProcessorSqlPatternProvider sqlPattern, final Integer value) {
    if (this == LIMIT) {
      return sqlPattern.getMaxResultsPattern().replace(VALUE_PLACEHOLDER, value.toString());
    }
    return sqlPattern.getFirstResultPattern().replace(VALUE_PLACEHOLDER, value.toString());
  }

  int defaultValue() {
    return defaultValue;
  }
}
