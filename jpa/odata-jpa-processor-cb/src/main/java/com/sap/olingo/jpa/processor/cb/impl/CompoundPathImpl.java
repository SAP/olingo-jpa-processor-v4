package com.sap.olingo.jpa.processor.cb.impl;

import java.util.List;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;

import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

final record CompoundPathImpl(@Nonnull List<Path<Comparable<?>>> paths) implements CompoundPath {
  public static final String OPENING_BRACKET = "(";
  public static final String CLOSING_BRACKET = ")";

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    statement.append(OPENING_BRACKET);
    statement.append(paths
        .stream()
        .map(path -> ((Expression<?>) path)) // NOSONAR
        .collect(new StringBuilderCollector.ExpressionCollector(statement, ", ")));
    statement.append(CLOSING_BRACKET);
    return statement;
  }

  @Override
  public boolean isEmpty() {
    return paths.isEmpty();
  }

  @Override
  public Path<?> getFirst() throws IllegalStateException {
    if (isEmpty())
      throw new IllegalStateException();
    return paths.get(0);
  }

}
