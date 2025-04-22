package com.sap.olingo.jpa.processor.core.filter;

import java.util.List;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

/**
 * In case the query result shall be filtered on an attribute of navigation target a sub-select will be generated.
 * <p>
 * E.g.<br>
 * - AdministrativeDivisions?$filter=Parent/ParentDivisionCode IN ('BE23', 'BE24')<br>
 * @author Oliver Grande
 *
 */
final class JPANavigationInOperation extends JPAAbstractNavigationOperation {

  final List<Literal> operand;

  JPANavigationInOperation(final JPAFilterComplierAccess jpaComplier, final BinaryOperatorKind operator,
      final JPAOperator left, final List<JPAOperator> right) {
    super(jpaComplier, (MethodKind) null, operator, (JPAMemberOperator) left);
    this.operand = right.stream().map(a -> ((JPALiteralOperator) a).getLiteral()).toList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enum<?> getOperator() {
    return BinaryOperatorKind.IN;
  }

  @Override
  VisitableExpression createExpression() {
    return new JPAFilterExpression(getMember(), operand, operator);
  }

  @Override
  public String toString() {
    return "JPANavigationOperation [operator=" + operator + ", jpaMember=" + jpaMember + ", operand=" + operand
        + ", methodCall=" + methodCall + "]";
  }
}
