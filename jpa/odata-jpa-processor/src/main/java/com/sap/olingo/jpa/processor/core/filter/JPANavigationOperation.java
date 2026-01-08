package com.sap.olingo.jpa.processor.core.filter;

import java.util.List;

import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

/**
 * In case the query result shall be filtered on an attribute of navigation target a sub-select will be generated.
 * <p>
 * E.g.<br>
 * <ul>
 * <li>Organizations?$select=ID&$filter=Roles/$count eq 2</li>
 * <li>CollectionDeeps?$filter=FirstLevel/SecondLevel/Comment/$count eq 2&$select=ID</li>
 * <li>Organizations?$filter=AdministrativeInformation/Created/User/LastName eq 'Mustermann'</li>
 * <li>AdministrativeDivisions?$filter=Parent/Parent/CodeID eq 'NUTS1' and DivisionCode eq 'BE212'</li>
 * <li>AssociationOneToOneSources?$filter=ColumnTarget eq null</li>
 * </ul>
 * @author Oliver Grande
 *
 */
final class JPANavigationOperation extends JPAAbstractNavigationOperation {

  final JPALiteralOperator operand;

  private static final OperatorPair determineOperatorsLeftRight(final JPAOperator left, final JPAOperator right) {
    if (left instanceof final JPAMemberOperator memberOperator)
      return new OperatorPair((JPALiteralOperator) right, memberOperator);
    else
      return new OperatorPair((JPALiteralOperator) left, (JPAMemberOperator) right);
  }

  private static final JPAMemberOperator determineOperatorsByParameters(final List<JPAOperator> parameters) {
    if (parameters.get(0) instanceof final JPAMemberOperator memberOperator)
      return memberOperator;
    else
      return (JPAMemberOperator) parameters.get(1);
  }

  public JPANavigationOperation(final BinaryOperatorKind operator,
      final JPANavigationOperation jpaNavigationOperation, final JPALiteralOperator operand,
      final JPAFilterComplierAccess jpaComplier) {
    super(jpaComplier, jpaNavigationOperation.methodCall, operator, jpaNavigationOperation.jpaMember);
    this.operand = operand;
  }

  JPANavigationOperation(final JPAFilterComplierAccess jpaComplier, final BinaryOperatorKind operator,
      final JPAOperator left, final JPAOperator right) {

    super(jpaComplier, null, operator, determineOperatorsLeftRight(left, right).member);
    this.operand = determineOperatorsLeftRight(left, right).literal;
  }

  public JPANavigationOperation(final JPAFilterComplierAccess jpaComplier, final MethodKind methodCall,
      final List<JPAOperator> parameters) {

    super(jpaComplier, methodCall, null, determineOperatorsByParameters(parameters));
    if (parameters.get(0) instanceof JPAMemberOperator) {
      operand = parameters.size() > 1 ? (JPALiteralOperator) parameters.get(1) : null;
    } else {
      operand = (JPALiteralOperator) parameters.get(0);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enum<?> getOperator() {
    return null;
  }

  @Override
  VisitableExpression createExpression() throws ODataJPAFilterException {
    if (operator != null && methodCall == null) {
      final List<UriResource> parts = jpaMember.getMember().getResourcePath().getUriResourceParts();
      if (UriResourceKind.count == parts.get(parts.size() - 1).getKind())
        return new JPACountExpression(getMember(), operand.getLiteral(),
            operator);
      if ("null".equals(operand.getLiteral().getText()))
        return new JPANullExpression(getMember(), operand.getLiteral(),
            operator);
      return new JPAFilterExpression(getMember(), operand.getLiteral(),
          operator);
    }
    if (operator == null && methodCall != null) {
      return new JPAMethodExpression(getMember(), operand, this.methodCall);
    } else {
      final JPAVisitableExpression methodExpression = new JPAMethodExpression(getMember(), operand, this.methodCall);
      return new JPABinaryExpression(methodExpression, operand.getLiteral(), operator);
    }
  }

  @Override
  public String toString() {
    return "JPANavigationOperation [operator=" + operator + ", jpaMember=" + jpaMember + ", operand=" + operand
        + ", methodCall=" + methodCall + "]";
  }

  private static record OperatorPair(JPALiteralOperator literal, JPAMemberOperator member) {

  }
}
