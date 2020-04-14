package com.sap.olingo.jpa.processor.cb.joiner;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.processor.cb.api.SqlConvertable;

final class StringBuilderJoiner<T> {
  private final StringBuilder statment;
  private final String delimiter;
  private final int initLength;

  StringBuilderJoiner(@Nonnull final StringBuilder statment, @Nonnull final String delimiter) {
    this.statment = Objects.requireNonNull(statment);
    this.delimiter = Objects.requireNonNull(delimiter);
    this.initLength = statment.length();
  }

  public StringBuilderJoiner<T> add(final T newElement) {
    ((SqlConvertable) newElement).asSQL(prepareStatment());
    return this;
  }

  public StringBuilderJoiner<T> merge() {
    return this;
  }

  public StringBuilder finish() {
    return statment;
  }

  private StringBuilder prepareStatment() {
    if (statment.length() != initLength) {
      statment.append(delimiter);
    }
    return statment;
  }
}
