package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Optional;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.api.SqlConvertible;
import com.sap.olingo.jpa.processor.cb.api.SqlKeyWords;
import com.sap.olingo.jpa.processor.cb.exeptions.InternalServerError;

class OrderImpl implements Order, SqlConvertible {

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
                  .append(", ");
            } catch (final ODataJPAModelException e) {
              throw new InternalServerError(e);
            }
          });
      return statement.delete(statement.length() - 2, statement.length());
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
