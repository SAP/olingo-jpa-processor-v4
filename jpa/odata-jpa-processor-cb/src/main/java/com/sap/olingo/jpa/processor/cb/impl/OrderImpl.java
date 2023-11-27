package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Optional;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exceptions.InternalServerError;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;

class OrderImpl implements Order, SqlConvertible {

  private static final String SEPARATOR = ", ";
  private static final int SEPARATOR_LENGTH = SEPARATOR.length();
  private final boolean isAscending;
  private final SqlConvertible expression;

  OrderImpl(final boolean isAscending, final SqlConvertible expression) {
    super();
    this.isAscending = isAscending;
    this.expression = expression;
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    if (expression instanceof FromImpl<?, ?>)
      return resolveExpression((FromImpl<?, ?>) expression, statement);
    return expression.asSQL(statement).append(" ").append(isAscending ? SqlKeyWords.ASC : SqlKeyWords.DESC);
  }

  private StringBuilder resolveExpression(final FromImpl<?, ?> from, final StringBuilder statement) {

    try {
      from.st
          .getKey()
          .forEach(a -> {
            try {
              new PathImpl<>(from.st.getPath(a.getExternalName()), Optional.empty(), from.st, from.tableAlias)
                  .asSQL(statement)
                  .append(" ")
                  .append(isAscending ? SqlKeyWords.ASC : SqlKeyWords.DESC)
                  .append(SEPARATOR);
            } catch (final ODataJPAModelException e) {
              throw new InternalServerError(e);
            }
          });
      return statement.delete(statement.length() - SEPARATOR_LENGTH, statement.length());
    } catch (final ODataJPAModelException e) {
      throw new InternalServerError(e);
    }
  }

  @Override
  public Order reverse() {
    return new OrderImpl(!isAscending, expression);
  }

  @Override
  public boolean isAscending() {
    return isAscending;
  }

  @Override
  public Expression<?> getExpression() {
    return (Expression<?>) expression;
  }

}
