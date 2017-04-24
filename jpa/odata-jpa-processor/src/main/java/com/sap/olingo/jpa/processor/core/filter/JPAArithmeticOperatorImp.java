package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

class JPAArithmeticOperatorImp implements JPAArithmeticOperator {
  private final JPAOperationConverter converter;
  private final BinaryOperatorKind operator;
  private final JPAOperator left;
  private final JPAOperator right;

  public JPAArithmeticOperatorImp(final JPAOperationConverter converter, final BinaryOperatorKind operator,
      final JPAOperator left, final JPAOperator right) {
    super();
    this.converter = converter;
    this.operator = operator;
    this.left = left;
    this.right = right;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAArithmeticOperator#get()
   */
  @Override
  public Expression<Number> get() throws ODataApplicationException {
    return converter.convert(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAArithmeticOperator#getOperator()
   */
  @Override
  public BinaryOperatorKind getOperator() {
    return operator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAArithmeticOperator#getRight()
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
   * @see com.sap.olingo.jpa.processor.core.filter.JPAArithmeticOperator#getLeft(javax.persistence.criteria.
   * CriteriaBuilder)
   */
  @Override
  @SuppressWarnings("unchecked")
  public Expression<Number> getLeft(final CriteriaBuilder cb) throws ODataApplicationException {
    if (left instanceof JPALiteralOperator) {
      if (right instanceof JPALiteralOperator)
        return cb.literal((Number) left.get());
      else
        return (Expression<Number>) right.get();
    }
    return (Expression<Number>) left.get();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAArithmeticOperator#getRightAsNumber(javax.persistence.criteria.
   * CriteriaBuilder)
   */
  @Override
  public Number getRightAsNumber(final CriteriaBuilder cb) throws ODataApplicationException {
    // Determine attribute in order to determine type of literal attribute and correctly convert it
    if (left instanceof JPALiteralOperator) {
      if (right instanceof JPALiteralOperator)
        return (Number) right.get();
      else if (right instanceof JPAMemberOperator)
        return (Number) ((JPALiteralOperator) left).get(((JPAMemberOperator) right).determineAttribute());
      else
        throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR_TYPE,
            HttpStatusCode.NOT_IMPLEMENTED);

    } else if (left instanceof JPAMemberOperator) {
      if (right instanceof JPALiteralOperator)
        return (Number) ((JPALiteralOperator) right).get(((JPAMemberOperator) left).determineAttribute());
      else
        throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR_TYPE,
            HttpStatusCode.NOT_IMPLEMENTED);

    } else if (left instanceof JPAFunctionOperator) {
      if (right instanceof JPALiteralOperator)
        return (Number) ((JPALiteralOperator) right).get(((JPAFunctionOperator) left).getReturnType());
      else
        throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR_TYPE,
            HttpStatusCode.NOT_IMPLEMENTED);
    } else
      throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR_TYPE,
          HttpStatusCode.NOT_IMPLEMENTED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAArithmeticOperator#getRightAsExpression()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Expression<Number> getRightAsExpression() throws ODataApplicationException {
    return (Expression<Number>) ((JPAMemberOperator) right).get();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAArithmeticOperator#getLeftAsIntExpression()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Expression<Integer> getLeftAsIntExpression() throws ODataApplicationException {
    if (left instanceof JPALiteralOperator)
      return (Expression<Integer>) right.get();
    return (Expression<Integer>) left.get();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAArithmeticOperator#getRightAsIntExpression()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Expression<Integer> getRightAsIntExpression() throws ODataApplicationException {
    return (Expression<Integer>) ((JPAMemberOperator) right).get();
  }

}
