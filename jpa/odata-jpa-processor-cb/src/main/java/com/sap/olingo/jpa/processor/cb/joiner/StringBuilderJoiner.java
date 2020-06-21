package com.sap.olingo.jpa.processor.cb.joiner;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.processor.cb.api.SqlConvertible;

final class StringBuilderJoiner<T> {
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

  public StringBuilder finish() {
    return statement;
  }

  private StringBuilder prepareStatement() {
    if (statement.length() != initLength) {
      statement.append(delimiter);
    }
    return statement;
  }
}
