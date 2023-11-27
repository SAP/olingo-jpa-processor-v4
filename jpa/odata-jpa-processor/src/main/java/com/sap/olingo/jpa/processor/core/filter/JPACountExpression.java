package com.sap.olingo.jpa.processor.core.filter;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

/**
 * Expression for filter on navigation count. E.g.:<br>
 * $filter=navigation/$count ge 2
 * @author Oliver Grande
 * @since 1.1.1
 * 13.07.2023
 */
public class JPACountExpression extends JPAInvertibleVisitableExpression {

  public JPACountExpression(final Member member, final Literal literal, final BinaryOperatorKind operator)
      throws ODataJPAFilterException {
    super(literal, operator, member);
  }

  @Override
  public <T> T accept(final ExpressionVisitor<T> visitor) throws ExpressionVisitException, ODataApplicationException {
    final T left = visitor.visitMember(member);
    final T right = visitor.visitLiteral(literal);
    return visitor.visitBinaryOperator(operator, left, right);
  }

  BinaryOperatorKind getOperator() {
    return operator;
  }

  @Override
  protected BinaryOperatorKind determineOperator(final BinaryOperatorKind operator, final Literal literal)
      throws ODataJPAFilterException {
    if ("0".equals(literal.getText())) {
      if (operator == BinaryOperatorKind.EQ
          || operator == BinaryOperatorKind.LE) {
        setInversionRequired(true);
        return BinaryOperatorKind.NE;
      } else if (operator == BinaryOperatorKind.GE) {
        throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER,
            HttpStatusCode.BAD_REQUEST, ".../$count ge 0");
      }
    }
    return operator;
  }
}
