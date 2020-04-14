package com.sap.olingo.jpa.processor.cb.impl;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;

import com.sap.olingo.jpa.processor.cb.api.SqlConvertable;
import com.sap.olingo.jpa.processor.cb.api.SqlKeyWords;

class OrderImpl implements Order, SqlConvertable {

  private final boolean isAscending;
  private SqlConvertable expression;

  OrderImpl(boolean isAscending, final SqlConvertable expression) {
    super();
    this.isAscending = isAscending;
    this.expression = expression;
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statment) {
    return expression.asSQL(statment).append(" ").append(isAscending ? SqlKeyWords.ASC : SqlKeyWords.DESC);
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
