package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

//
class JPAComparisonOperatorImp<T extends Comparable<T>> implements JPAComparisonOperator<T> {
  private final JPAOperationConverter converter;
  private final BinaryOperatorKind operator;
  private final JPAOperator left;
  private final JPAOperator right;

  public JPAComparisonOperatorImp(final JPAOperationConverter converter, final BinaryOperatorKind operator,
      final JPAOperator left,
      final JPAOperator right) {
    super();
    this.converter = converter;
    this.operator = operator;
    this.left = left;
    this.right = right;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAComparisonOperator#get()
   */
  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    return converter.convert(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAComparisonOperator#getOperator()
   */
  @Override
  public BinaryOperatorKind getOperator() {
    return operator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAComparisonOperator#getLeft()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Expression<T> getLeft() throws ODataApplicationException {
    if (left instanceof JPALiteralOperator)
      return (Expression<T>) right.get();
    return (Expression<T>) left.get();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAComparisonOperator#getRight()
   */
  @Override
  public Object getRight() {
    if (left instanceof JPALiteralOperator)
      return left;
    return right;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAComparisonOperator#getRightAsComparable()
   */
  @Override
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

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAComparisonOperator#getRightAsExpression()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Expression<T> getRightAsExpression() throws ODataApplicationException {
    return (Expression<T>) right.get();
  }
}