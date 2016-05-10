package org.apache.olingo.jpa.processor.core.filter;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

class JPAVisitor implements ExpressionVisitor<JPAOperator> {

  /**
   * 
   */
  private final JPAFilterComplierAccess jpaComplier;

  /**
   * @param jpaFilterCrossComplier
   */
  JPAVisitor(final JPAFilterComplierAccess jpaFilterCrossComplier) {
    this.jpaComplier = jpaFilterCrossComplier;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public JPAOperator visitBinaryOperator(final BinaryOperatorKind operator, final JPAOperator left,
      final JPAOperator right) throws ExpressionVisitException, ODataApplicationException {

    // TODO Logging
    if (hasNavigation(left) || hasNavigation(right))
      return new JPANavigationOperation(this.jpaComplier, operator, left, right);
    if (operator == BinaryOperatorKind.EQ
        || operator == BinaryOperatorKind.NE
        || operator == BinaryOperatorKind.GE
        || operator == BinaryOperatorKind.GT
        || operator == BinaryOperatorKind.LT
        || operator == BinaryOperatorKind.LE)
      return new JPAComparisonOperator(this.jpaComplier.getConverter(), operator, left, right);
    else if (operator == BinaryOperatorKind.AND || operator == BinaryOperatorKind.OR)
      return new JPABooleanOperator(this.jpaComplier.getConverter(), operator, (JPAExpressionOperator) left,
          (JPAExpressionOperator) right);
    else if (operator == BinaryOperatorKind.ADD
        || operator == BinaryOperatorKind.SUB
        || operator == BinaryOperatorKind.MUL
        || operator == BinaryOperatorKind.DIV
        || operator == BinaryOperatorKind.MOD)
      return new JPAArithmeticOperator(this.jpaComplier.getConverter(), operator, left, right);
    else
      throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
          HttpStatusCode.NOT_IMPLEMENTED, operator.name());
  }

  boolean hasNavigation(final JPAOperator operand) {
    if (operand instanceof JPAMemberOperator) {
      final List<UriResource> uriResourceParts = ((JPAMemberOperator) operand).getMember().getResourcePath()
          .getUriResourceParts();
      for (int i = uriResourceParts.size() - 1; i >= 0; i--) {
        if (uriResourceParts.get(i) instanceof UriResourceNavigation)
          return true;
      }
    }
    return false;
  }

  @Override
  public JPAOperator visitUnaryOperator(final UnaryOperatorKind operator, final JPAOperator operand)
      throws ExpressionVisitException, ODataApplicationException {
    // TODO Logging
    if (operator == UnaryOperatorKind.NOT)
      return new JPAUnaryBooleanOperator(this.jpaComplier.getConverter(), operator, (JPAExpressionOperator) operand);
    else
      throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
          HttpStatusCode.NOT_IMPLEMENTED, operator.name());
  }

  @Override
  public JPAOperator visitMethodCall(final MethodKind methodCall, final List<JPAOperator> parameters)
      throws ExpressionVisitException, ODataApplicationException {
    // TODO Logging
    return new JPAFunctionCall(this.jpaComplier.getConverter(), methodCall, parameters);
  }

  @Override
  public JPAOperator visitLambdaExpression(final String lambdaFunction, final String lambdaVariable,
      final org.apache.olingo.server.api.uri.queryoption.expression.Expression expression)
      throws ExpressionVisitException, ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER,
        HttpStatusCode.NOT_IMPLEMENTED, "Lambda Expression");
  }

  @Override
  public JPAOperator visitLiteral(final Literal literal) throws ExpressionVisitException, ODataApplicationException {
    // TODO Logging
    return new JPALiteralOperator(this.jpaComplier.getOdata(), literal);
  }

  @Override
  public JPAOperator visitMember(final Member member) throws ExpressionVisitException, ODataApplicationException {

    // TODO Logging
    if (getLambdaType(member.getResourcePath()) == UriResourceKind.lambdaAny)
      return new JPALambdaAnyOperation(this.jpaComplier, member);
    else if (getLambdaType(member.getResourcePath()) == UriResourceKind.lambdaAll)
      return new JPALambdaAllOperation(this.jpaComplier, member);
    else if (isAggregation(member.getResourcePath())) {
      return new JPAAggregationOperation(jpaComplier.getParent().getRoot(), jpaComplier.getConverter());
    }
    return new JPAMemberOperator(this.jpaComplier.getJpaEntityType(), this.jpaComplier.getParent(), member);
  }

  private boolean isAggregation(final UriInfoResource member) {
    if (member.getUriResourceParts().size() == 1 && member.getUriResourceParts().get(0)
        .getKind() == UriResourceKind.count)
      return true;
    return false;
  }

  UriResourceKind getLambdaType(final UriInfoResource member) {
    for (final UriResource r : member.getUriResourceParts()) {
      if (r.getKind() == UriResourceKind.lambdaAny
          || r.getKind() == UriResourceKind.lambdaAll)
        return r.getKind();
    }
    return null;
  }

  @Override
  public JPAOperator visitAlias(final String aliasName) throws ExpressionVisitException, ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER,
        HttpStatusCode.NOT_IMPLEMENTED, "Alias");
  }

  @Override
  public JPAOperator visitTypeLiteral(final EdmType type) throws ExpressionVisitException, ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER,
        HttpStatusCode.NOT_IMPLEMENTED, "Type Literal");
  }

  @Override
  public JPAOperator visitLambdaReference(final String variableName) throws ExpressionVisitException,
      ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER,
        HttpStatusCode.NOT_IMPLEMENTED, "Lambda Reference");
  }

  @Override
  public JPAOperator visitEnum(final EdmEnumType type, final List<String> enumValues) throws ExpressionVisitException,
      ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER,
        HttpStatusCode.NOT_IMPLEMENTED, "Enumerations");
  }

}