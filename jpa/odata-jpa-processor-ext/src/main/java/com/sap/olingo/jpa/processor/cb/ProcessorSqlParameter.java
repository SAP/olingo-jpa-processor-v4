package com.sap.olingo.jpa.processor.cb;

public record ProcessorSqlParameter(String keyword, String parameter, boolean isOptional) {

  public ProcessorSqlParameter(final String parameter, final boolean isOptional) {
    this("", parameter, isOptional);
  }
}
