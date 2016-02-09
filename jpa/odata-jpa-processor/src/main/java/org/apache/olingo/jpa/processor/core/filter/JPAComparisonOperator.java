package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

//
class JPAComparisonOperator<T extends Comparable<T>> implements JPAExpressionOperator {
  private final JPAOperationConverter converter;
  private final BinaryOperatorKind operator;
  private final JPAOperator left;
  private final JPAOperator right;

  public JPAComparisonOperator(JPAOperationConverter converter, BinaryOperatorKind operator, JPAOperator left,
      JPAOperator right) {
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
  public Expression<T> getLeft() {
    if (left instanceof JPALiteralOperator)
      return (Expression<T>) ((JPAMemberOperator) right).get();
    return (Expression<T>) ((JPAMemberOperator) left).get();
  }

  @SuppressWarnings("unchecked")
  public Comparable<T> getRight() {
    if (left instanceof JPALiteralOperator)
      return (Comparable<T>) ((JPALiteralOperator) left).get((JPAAttribute) ((JPAMemberOperator) right)
          .determineAttributePath()
          .getLeaf());
    if (right instanceof JPALiteralOperator)
      return (Comparable<T>) ((JPALiteralOperator) right).get((JPAAttribute) ((JPAMemberOperator) left)
          .determineAttributePath()
          .getLeaf());
    return (Comparable<T>) ((JPAMemberOperator) right).get();
  }

}