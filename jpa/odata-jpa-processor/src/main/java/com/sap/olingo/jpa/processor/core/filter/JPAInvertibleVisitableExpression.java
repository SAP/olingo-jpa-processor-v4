package com.sap.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

/**
 *
 * @author Oliver Grande
 * @since 1.1.1
 * 01.08.2023
 */
public abstract class JPAInvertibleVisitableExpression implements JPAVisitableExpression {

  // Indicates that the expression has inverted the operand e.g. from EQ to NE. client may need to adjust e.g. perform
  // NOT EXISTS instead of EXISTS
  private boolean inversionRequired;

  protected final Literal literal;
  protected final BinaryOperatorKind operator;
  protected final Member member;

  protected JPAInvertibleVisitableExpression(final Literal literal, final BinaryOperatorKind operator,
      final Member member) throws ODataJPAFilterException {
    super();
    this.literal = literal;
    this.operator = determineOperator(operator, literal);
    this.member = member;
  }

  @Override
  public UriInfoResource getMember() {
    return member.getResourcePath();
  }

  @Override
  public Literal getLiteral() {
    return literal;
  }

  /**
   * Expression has performed an inversion of the operator. A client has to reaction on this, e.g. by using NOT EXISTS
   * instead of EXISTS.
   */
  public boolean isInversionRequired() {
    return inversionRequired;
  }

  /**
   * Client has performed an inversion, usually from EXISTS to NOT EXISTS. No need for others to do that as well
   */
  public void inversionPerformed() {
    inversionRequired = false;
  }

  protected abstract BinaryOperatorKind determineOperator(final BinaryOperatorKind operator, final Literal literal)
      throws ODataJPAFilterException;

  void setInversionRequired(final boolean inversionRequired) {
    this.inversionRequired = inversionRequired;
  }
}