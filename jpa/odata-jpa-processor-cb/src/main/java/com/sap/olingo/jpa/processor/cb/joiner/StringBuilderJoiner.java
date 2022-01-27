package com.sap.olingo.jpa.processor.cb.joiner;

import java.util.Objects;

import javax.annotation.Nonnull;

final class StringBuilderJoiner<T> {
  private static final String EMPTY_RESULT = "";
  private final StringBuilder statement;
  private final String delimiter;
  private final int initLength;

  StringBuilderJoiner(@Nonnull final StringBuilder statement, @Nonnull final String delimiter) {
    this.statement = Objects.requireNonNull(statement);
    this.delimiter = Objects.requireNonNull(delimiter);
    this.initLength = statement.length();
  }

  public StringBuilderJoiner<T> add(final T newElement) {
    ((SqlConvertible) newElement).asSQL(prepareStatement());
    return this;
  }

  public StringBuilderJoiner<T> merge() {
    return this;
  }

  public String finish() {
    return EMPTY_RESULT;
  }

  private StringBuilder prepareStatement() {
    if (statement.length() != initLength) {
      statement.append(delimiter);
    }
    return statement;
  }
}
