package com.sap.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

/**
 * Expression for filter on navigation is null. E.g.:<br>
 * $filter=navigation eq null
 * @author Oliver Grande
 * @since 1.1.1
 * 01.08.2023
 */
public class JPANullExpression extends JPAInvertibleVisitableExpression {

  public JPANullExpression(final Member member, final Literal literal, final BinaryOperatorKind operator)
      throws ODataJPAFilterException {
    super(literal, operator, member);
  }

  @Override
  public <T> T accept(final ExpressionVisitor<T> visitor) throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  protected BinaryOperatorKind determineOperator(final BinaryOperatorKind operator, final Literal literal)
      throws ODataJPAFilterException {
    if (operator == BinaryOperatorKind.EQ)
      setInversionRequired(true);
    return operator;
  }

}
