package com.sap.olingo.jpa.processor.cb.impl;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;

import com.sap.olingo.jpa.processor.cb.api.SqlConvertible;
import com.sap.olingo.jpa.processor.cb.api.SqlKeyWords;

class OrderImpl implements Order, SqlConvertible {

  private final boolean isAscending;
  private SqlConvertible expression;

  OrderImpl(boolean isAscending, final SqlConvertible expression) {
    super();
    this.isAscending = isAscending;
    this.expression = expression;
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    return expression.asSQL(statement).append(" ").append(isAscending ? SqlKeyWords.ASC : SqlKeyWords.DESC);
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
