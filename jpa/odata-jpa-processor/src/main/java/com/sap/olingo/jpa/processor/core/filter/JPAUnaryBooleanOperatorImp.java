package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

final class JPAUnaryBooleanOperatorImp implements JPAUnaryBooleanOperator {

  private final JPAOperationConverter converter;
  private final UnaryOperatorKind operator;
  private final JPAExpressionOperator left;

  public JPAUnaryBooleanOperatorImp(final JPAOperationConverter converter, final UnaryOperatorKind operator,
      final JPAExpressionOperator left) {
    super();
    this.converter = converter;
    this.operator = operator;
    this.left = left;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAUnaryBooleanOperator#get()
   */
  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    return converter.convert(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAUnaryBooleanOperator#getLeft()
   */
  @Override
  public Expression<Boolean> getLeft() throws ODataApplicationException {
    return left.get();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAUnaryBooleanOperator#getOperator()
   */
  @Override
  public UnaryOperatorKind getOperator() {
    return operator;
  }

}
