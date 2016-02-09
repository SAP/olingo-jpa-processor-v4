package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

public class JPAArithmeticOperator implements JPAOperator {
  private final JPAOperationConverter converter;
  private final BinaryOperatorKind operator;
  private final JPAOperator left;
  private final JPAOperator right;

  public JPAArithmeticOperator(JPAOperationConverter converter, BinaryOperatorKind operator, JPAOperator left,
      JPAOperator right) {
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
      return (Number) ((JPALiteralOperator) left).get((JPAAttribute) ((JPAMemberOperator) right)
          .determineAttributePath()
          .getLeaf());
    return (Number) ((JPALiteralOperator) right).get((JPAAttribute) ((JPAMemberOperator) left)
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
