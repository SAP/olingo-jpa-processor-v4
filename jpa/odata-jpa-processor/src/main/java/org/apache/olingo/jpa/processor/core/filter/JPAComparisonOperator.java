package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

//
class JPAComparisonOperator<T extends Comparable<T>> implements JPAExpressionOperator {
  private final JPAOperationConverter converter;
  private final BinaryOperatorKind operator;
  private final JPAOperator left;
  private final JPAOperator right;

  public JPAComparisonOperator(final JPAOperationConverter converter, final BinaryOperatorKind operator,
      final JPAOperator left,
      final JPAOperator right) {
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

  @Override
  public BinaryOperatorKind getOperator() {
    return operator;
  }

  @SuppressWarnings("unchecked")
  public Expression<T> getLeft() throws ODataApplicationException {
    if (left instanceof JPALiteralOperator)
      return (Expression<T>) right.get();
    return (Expression<T>) left.get();
  }

  public Object getRight() {
    if (left instanceof JPALiteralOperator)
      return left;
    return right;
  }

  @SuppressWarnings("unchecked")
  public Comparable<T> getRightAsComparable() throws ODataApplicationException {
    if (left instanceof JPALiteralOperator) {
      if (right instanceof JPAMemberOperator)
        return (Comparable<T>) ((JPALiteralOperator) left).get(((JPAMemberOperator) right).determineAttribute());
      else
        return (Comparable<T>) left.get();
    }
    if (right instanceof JPALiteralOperator) {
      if (left instanceof JPAMemberOperator)
        return (Comparable<T>) ((JPALiteralOperator) right).get(((JPAMemberOperator) left).determineAttribute());

      else {
        return (Comparable<T>) right.get();
      }
    }
    return (Comparable<T>) right.get();
  }

  @SuppressWarnings("unchecked")
  public Expression<T> getRightAsExpression() throws ODataApplicationException {
    return (Expression<T>) right.get();
  }
}