package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

public class JPAUnaryBooleanOperator implements JPAExpressionOperator {

  private final JPAOperationConverter converter;
  private final UnaryOperatorKind operator;
  private final JPAExpressionOperator left;

  public JPAUnaryBooleanOperator(JPAOperationConverter converter, UnaryOperatorKind operator,
      JPAExpressionOperator left) {
    super();
    this.converter = converter;
    this.operator = operator;
    this.left = left;
  }

  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    return converter.convert(this);
  }

  public Expression<Boolean> getLeft() throws ODataApplicationException {
    return left.get();
  }

  @Override
  public UnaryOperatorKind getOperator() {
    return operator;
  }

}
