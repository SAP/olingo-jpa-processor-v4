package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

class JPAArithmeticOperator implements JPAOperator {
  private final JPAOperationConverter converter;
  private final BinaryOperatorKind operator;
  private final JPAOperator left;
  private final JPAOperator right;

  public JPAArithmeticOperator(final JPAOperationConverter converter, final BinaryOperatorKind operator,
      final JPAOperator left, final JPAOperator right) {
    super();
    this.converter = converter;
    this.operator = operator;
    this.left = left;
    this.right = right;
  }

  @Override
  public Expression<Number> get() throws ODataApplicationException {
    return converter.convert(this);
  }

  public BinaryOperatorKind getOperator() {
    return operator;
  }

  public Object getRight() {
    if (left instanceof JPALiteralOperator)
      return left;
    return right;
  }

  @SuppressWarnings("unchecked")
  public Expression<Number> getLeft() throws ODataApplicationException {
    if (left instanceof JPALiteralOperator)
      return (Expression<Number>) right.get();
    return (Expression<Number>) left.get();
  }

  public Number getRightAsNumber() throws ODataApplicationException {
    if (left instanceof JPALiteralOperator)
      return (Number) ((JPALiteralOperator) left).get(((JPAMemberOperator) right)
          .determineAttributePath()
          .getLeaf());
    return (Number) ((JPALiteralOperator) right).get(((JPAMemberOperator) left)
        .determineAttributePath()
        .getLeaf());
  }

  @SuppressWarnings("unchecked")
  public Expression<Number> getRightAsExpression() {
    return (Expression<Number>) ((JPAMemberOperator) right).get();
  }

  @SuppressWarnings("unchecked")
  public Expression<Integer> getLeftAsIntExpression() throws ODataApplicationException {
    if (left instanceof JPALiteralOperator)
      return (Expression<Integer>) right.get();
    return (Expression<Integer>) left.get();
  }

  @SuppressWarnings("unchecked")
  public Expression<Integer> getRightAsIntExpression() {
    return (Expression<Integer>) ((JPAMemberOperator) right).get();
  }
}
