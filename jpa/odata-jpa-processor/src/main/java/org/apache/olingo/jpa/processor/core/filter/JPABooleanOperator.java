package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

class JPABooleanOperator implements JPAExpressionOperator {

  private final JPAOperationConverter converter;
  private final BinaryOperatorKind operator;
  private final JPAExpressionOperator left;
  private final JPAExpressionOperator right;

  public JPABooleanOperator(final JPAOperationConverter converter, final BinaryOperatorKind operator,
      final JPAExpressionOperator left,
      final JPAExpressionOperator right) {
    super();
    this.converter = converter;
    this.operator = operator;
    this.left = left;
    this.right = right;
  }

  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    return converter.convert(this);
  }

  public BinaryOperatorKind getOperator() {
    return operator;
  }

  public Expression<Boolean> getLeft() throws ODataApplicationException {
    return left.get();
  }

  public Expression<Boolean> getRight() throws ODataApplicationException {
    return right.get();
  }

}