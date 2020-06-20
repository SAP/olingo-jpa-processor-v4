package com.sap.olingo.jpa.processor.cb.joiner;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate.BooleanOperator;

final class ExpressionJoiner {

  private final CriteriaBuilder cb;
  private final BooleanOperator operator;
  private boolean isFirst;
  private Expression<Boolean> expression;

  ExpressionJoiner(@Nonnull final CriteriaBuilder cb, @Nonnull final BooleanOperator operator) {
    this.cb = Objects.requireNonNull(cb);
    this.operator = Objects.requireNonNull(operator);
    this.isFirst = true;
  }

  public ExpressionJoiner add(final Expression<Boolean> newExpression) {
    if (isFirst) {
      this.expression = newExpression;
      isFirst = false;
    } else if (operator == BooleanOperator.AND) {
      this.expression = cb.and(expression, newExpression);
    } else {
      this.expression = cb.or(expression, newExpression);
    }
    return this;
  }

  public ExpressionJoiner merge() {
    return this;
  }

  public Expression<Boolean> finish() {
    return expression;
  }
}
