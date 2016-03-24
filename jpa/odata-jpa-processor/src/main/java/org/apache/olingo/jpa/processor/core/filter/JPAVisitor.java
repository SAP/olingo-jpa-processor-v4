package org.apache.olingo.jpa.processor.core.filter;

import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
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
      return new JPANavigationOperation(this.jpaComplier.getOdata(), this.jpaComplier.getSd(), this.jpaComplier
          .getEntityManager(), this.jpaComplier.getUriResourceParts(), this.jpaComplier.getConverter(), operator, left,
          right, this.jpaComplier.getParent());
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
      throw new ODataApplicationException("Operator " + operator + " not supported",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  boolean hasNavigation(final JPAOperator operand) {
    if (operand instanceof JPAMemberOperator) {
      final List<UriResource> uriResourceParts = ((JPAMemberOperator) operand).getMember().getUriResourceParts();
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
      throw new ODataApplicationException("Operator " + operator + " not supported",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
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
    throw new ODataApplicationException("Lambda Expression not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public JPAOperator visitLiteral(final Literal literal) throws ExpressionVisitException, ODataApplicationException {
    // TODO Logging
    return new JPALiteralOperator(this.jpaComplier.getOdata(), literal);
  }

  @Override
  public JPAOperator visitMember(final UriInfoResource member) throws ExpressionVisitException,
      ODataApplicationException {
    // TODO Logging
    if (getLambdaType(member) == UriResourceKind.lambdaAny)
      return new JPALambdaAnyOperation(this.jpaComplier.getOdata(), this.jpaComplier.getSd(), this.jpaComplier
          .getEntityManager(), this.jpaComplier.getUriResourceParts(), this.jpaComplier.getConverter(), member,
          this.jpaComplier.getParent());
    else if (getLambdaType(member) == UriResourceKind.lambdaAll)
      return new JPALambdaAllOperation(this.jpaComplier.getOdata(), this.jpaComplier.getSd(), this.jpaComplier
          .getEntityManager(), this.jpaComplier.getUriResourceParts(), this.jpaComplier.getConverter(), member,
          this.jpaComplier.getParent());
    return new JPAMemberOperator(this.jpaComplier.getJpaEntityType(), this.jpaComplier.getParent(), member);
  }

  UriResourceKind getLambdaType(UriInfoResource member) {
    for (UriResource r : member.getUriResourceParts()) {
      if (r.getKind() == UriResourceKind.lambdaAny
          || r.getKind() == UriResourceKind.lambdaAll)
        return r.getKind();
    }
    return null;
  }

  @Override
  public JPAOperator visitAlias(final String aliasName) throws ExpressionVisitException, ODataApplicationException {
    throw new ODataApplicationException("Alias not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public JPAOperator visitTypeLiteral(final EdmType type) throws ExpressionVisitException, ODataApplicationException {
    throw new ODataApplicationException("Type Literal not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public JPAOperator visitLambdaReference(final String variableName) throws ExpressionVisitException,
      ODataApplicationException {
    throw new ODataApplicationException("Lambda Reference not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public JPAOperator visitEnum(final EdmEnumType type, final List<String> enumValues) throws ExpressionVisitException,
      ODataApplicationException {
    throw new ODataApplicationException("Enumerations not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

}