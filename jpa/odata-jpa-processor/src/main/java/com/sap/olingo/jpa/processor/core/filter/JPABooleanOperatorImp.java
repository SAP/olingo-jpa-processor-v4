package com.sap.olingo.jpa.processor.core.filter;

import jakarta.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

class JPABooleanOperatorImp implements JPABooleanOperator {

  private final JPAOperationConverter converter;
  private final BinaryOperatorKind operator;
  private final JPAExpression left;
  private final JPAExpression right;

  public JPABooleanOperatorImp(final JPAOperationConverter converter, final BinaryOperatorKind operator,
      final JPAExpression left, final JPAExpression right) {
    super();
    this.converter = converter;
    this.operator = operator;
    this.left = left;
    this.right = right;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.processor.core.filter.JPABooleanOperator#get()
   */
  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    return converter.convert(this);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.processor.core.filter.JPABooleanOperator#getOperator()
   */
  @SuppressWarnings("unchecked")
  @Override
  public BinaryOperatorKind getOperator() {
    return operator;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.processor.core.filter.JPABooleanOperator#getLeft()
   */
  @Override
  public Expression<Boolean> getLeft() throws ODataApplicationException {
    return left.get();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.processor.core.filter.JPABooleanOperator#getRight()
   */
  @Override
  public Expression<Boolean> getRight() throws ODataApplicationException {
    return right.get();
  }

  @Override
  public String getName() {
    return operator.name();
  }
}