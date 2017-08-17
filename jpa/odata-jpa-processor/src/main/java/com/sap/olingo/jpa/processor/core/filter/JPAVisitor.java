package com.sap.olingo.jpa.processor.core.filter;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

class JPAVisitor implements JPAExpressionVisitor {

  /**
   * 
   */
  private final JPAFilterComplierAccess jpaComplier;
  private final JPAServiceDebugger debugger;

  /**
   * @param jpaFilterCrossComplier
   */
  JPAVisitor(final JPAFilterComplierAccess jpaFilterCrossComplier) {
    this.jpaComplier = jpaFilterCrossComplier;
    this.debugger = jpaComplier.getParent().getDebugger();
  }

  @Override
  public JPAOperator visitAlias(final String aliasName) throws ExpressionVisitException, ODataApplicationException {
//    int handle = debugger.startRuntimeMeasurement("JPAVisitor", "visitAlias");
//    debugger.stopRuntimeMeasurement(handle);
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER,
        HttpStatusCode.NOT_IMPLEMENTED, "Alias");
  }

  @SuppressWarnings("rawtypes")
  @Override
  public JPAOperator visitBinaryOperator(final BinaryOperatorKind operator, final JPAOperator left,
      final JPAOperator right) throws ExpressionVisitException, ODataApplicationException {
    int handle = debugger.startRuntimeMeasurement("JPAVisitor", "visitBinaryOperator");

    // TODO Logging
    if (hasNavigation(left) || hasNavigation(right))
      return new JPANavigationOperation(this.jpaComplier, operator, left, right);
    if (operator == BinaryOperatorKind.EQ
        || operator == BinaryOperatorKind.NE
        || operator == BinaryOperatorKind.GE
        || operator == BinaryOperatorKind.GT
        || operator == BinaryOperatorKind.LT
        || operator == BinaryOperatorKind.LE) {
      debugger.stopRuntimeMeasurement(handle);
      return new JPAComparisonOperatorImp(this.jpaComplier.getConverter(), operator, left, right);
    } else if (operator == BinaryOperatorKind.AND || operator == BinaryOperatorKind.OR) {
      debugger.stopRuntimeMeasurement(handle);
      return new JPABooleanOperatorImp(this.jpaComplier.getConverter(), operator, (JPAExpressionOperator) left,
          (JPAExpressionOperator) right);
    } else if (operator == BinaryOperatorKind.ADD
        || operator == BinaryOperatorKind.SUB
        || operator == BinaryOperatorKind.MUL
        || operator == BinaryOperatorKind.DIV
        || operator == BinaryOperatorKind.MOD) {
      debugger.stopRuntimeMeasurement(handle);
      return new JPAArithmeticOperatorImp(this.jpaComplier.getConverter(), operator, left, right);
    } else
      debugger.stopRuntimeMeasurement(handle);
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED, operator.name());
  }

  @Override
  public JPAOperator visitEnum(final EdmEnumType type, final List<String> enumValues) throws ExpressionVisitException,
      ODataApplicationException {
//    int handle = debugger.startRuntimeMeasurement("JPAVisitor", "visitEnum");
//    debugger.stopRuntimeMeasurement(handle);
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER,
        HttpStatusCode.NOT_IMPLEMENTED, "Enumerations");
  }

  @Override
  public JPAOperator visitLambdaExpression(final String lambdaFunction, final String lambdaVariable,
      final org.apache.olingo.server.api.uri.queryoption.expression.Expression expression)
      throws ExpressionVisitException, ODataApplicationException {
//    int handle = debugger.startRuntimeMeasurement("JPAVisitor", "visitLambdaExpression");
//    debugger.stopRuntimeMeasurement(handle);
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER,
        HttpStatusCode.NOT_IMPLEMENTED, "Lambda Expression");
  }

  @Override
  public JPAOperator visitLambdaReference(final String variableName) throws ExpressionVisitException,
      ODataApplicationException {
//    int handle = debugger.startRuntimeMeasurement("JPAVisitor", "visitLambdaReference");
//    debugger.stopRuntimeMeasurement(handle);
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER,
        HttpStatusCode.NOT_IMPLEMENTED, "Lambda Reference");
  }

  @Override
  public JPAOperator visitLiteral(final Literal literal) throws ExpressionVisitException, ODataApplicationException {
    final int handle = debugger.startRuntimeMeasurement("JPAVisitor", "visitBinaryOperator");
    debugger.stopRuntimeMeasurement(handle);
    return new JPALiteralOperator(this.jpaComplier.getOdata(), literal);
  }

  @Override
  public JPAOperator visitMember(final Member member) throws ExpressionVisitException, ODataApplicationException {

    final int handle = debugger.startRuntimeMeasurement("JPAVisitor", "visitMember");
    if (getLambdaType(member.getResourcePath()) == UriResourceKind.lambdaAny) {
      debugger.stopRuntimeMeasurement(handle);
      return new JPALambdaAnyOperation(this.jpaComplier, member);
    } else if (getLambdaType(member.getResourcePath()) == UriResourceKind.lambdaAll) {
      debugger.stopRuntimeMeasurement(handle);
      return new JPALambdaAllOperation(this.jpaComplier, member);
    } else if (isAggregation(member.getResourcePath())) {
      debugger.stopRuntimeMeasurement(handle);
      return new JPAAggregationOperationImp(jpaComplier.getParent().getRoot(), jpaComplier.getConverter());
    } else if (isCustomFunction(member.getResourcePath())) {
      final UriResource resource = member.getResourcePath().getUriResourceParts().get(0);
      final JPAFunction jpaFunction = this.jpaComplier.getSd().getFunction(((UriResourceFunction) resource)
          .getFunction());
      final List<UriParameter> odataParams = ((UriResourceFunction) resource).getParameters();
      debugger.stopRuntimeMeasurement(handle);
      return new JPAFunctionOperator(this, odataParams, jpaFunction);
      // , this.jpaComplier.getParent().getRoot(), jpaComplier.getConverter().cb);
    }
    debugger.stopRuntimeMeasurement(handle);
    return new JPAMemberOperator(this.jpaComplier.getJpaEntityType(), this.jpaComplier.getParent(), member);
  }

  @Override
  public JPAOperator visitMethodCall(final MethodKind methodCall, final List<JPAOperator> parameters)
      throws ExpressionVisitException, ODataApplicationException {
    final int handle = debugger.startRuntimeMeasurement("JPAVisitor", "visitMethodCall");
    debugger.stopRuntimeMeasurement(handle);
    return new JPAMethodCallImp(this.jpaComplier.getConverter(), methodCall, parameters);
  }

  @Override
  public JPAOperator visitTypeLiteral(final EdmType type) throws ExpressionVisitException, ODataApplicationException {
//    int handle = debugger.startRuntimeMeasurement("JPAVisitor", "visitTypeLiteral");
//    debugger.stopRuntimeMeasurement(handle);
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER,
        HttpStatusCode.NOT_IMPLEMENTED, "Type Literal");
  }

  @Override
  public JPAOperator visitUnaryOperator(final UnaryOperatorKind operator, final JPAOperator operand)
      throws ExpressionVisitException, ODataApplicationException {
    final int handle = debugger.startRuntimeMeasurement("JPAVisitor", "visitBinaryOperator");
    if (operator == UnaryOperatorKind.NOT) {
      debugger.stopRuntimeMeasurement(handle);
      return new JPAUnaryBooleanOperatorImp(this.jpaComplier.getConverter(), operator, (JPAExpressionOperator) operand);
    } else {
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
          HttpStatusCode.NOT_IMPLEMENTED, operator.name());
    }
  }

  UriResourceKind getLambdaType(final UriInfoResource member) {
    for (final UriResource r : member.getUriResourceParts()) {
      if (r.getKind() == UriResourceKind.lambdaAny
          || r.getKind() == UriResourceKind.lambdaAll)
        return r.getKind();
    }
    return null;
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

  private boolean isAggregation(final UriInfoResource resourcePath) {
    if (resourcePath.getUriResourceParts().size() == 1 && resourcePath.getUriResourceParts().get(0)
        .getKind() == UriResourceKind.count)
      return true;
    return false;
  }

  private boolean isCustomFunction(final UriInfoResource resourcePath) {
    if (resourcePath.getUriResourceParts().size() > 0 && resourcePath.getUriResourceParts().get(
        0) instanceof UriResourceFunction)
      return true;
    return false;
  }

  CriteriaBuilder getCriteriaBuilder() {
    return jpaComplier.getConverter().cb;
  }

  JPAServiceDocument getSd() {
    return jpaComplier.getSd();
  }

  @Override
  public Root<?> getRoot() {
    return jpaComplier.getParent().getRoot();
  }

  @Override
  public OData getOdata() {
    return jpaComplier.getOdata();
  }
}