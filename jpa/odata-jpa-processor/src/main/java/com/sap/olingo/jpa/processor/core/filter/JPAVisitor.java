package com.sap.olingo.jpa.processor.core.filter;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_TRANSIENT;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;
import static org.apache.olingo.commons.api.http.HttpStatusCode.NOT_IMPLEMENTED;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEnumerationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJavaFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurment;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.query.Utility;

class JPAVisitor implements JPAExpressionVisitor { // NOSONAR

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
    this.debugger = jpaComplier.getDebugger();
  }

  @Override
  public OData getOData() {
    return jpaComplier.getOData();
  }

  @Override
  public From<?, ?> getRoot() {
    return jpaComplier.getRoot();
  }

  @Override
  public JPAEntityType getEntityType() {
    return jpaComplier.getJpaEntityType();
  }

  @Override
  public JPAOperator visitAlias(final String aliasName) throws ExpressionVisitException, ODataApplicationException {

    throw new ODataJPAFilterException(NOT_SUPPORTED_FILTER, NOT_IMPLEMENTED, "Alias");
  }

  @Override
  public JPAOperator visitBinaryOperator(final BinaryOperatorKind operator, final JPAOperator left,
      final JPAOperator right) throws ExpressionVisitException, ODataApplicationException {

    try (JPARuntimeMeasurment meassument = debugger.newMeasurement(this, "visitBinaryOperator")) {
      if (operator == BinaryOperatorKind.AND || operator == BinaryOperatorKind.OR) {
        // Connecting operations have to be handled first, as JPANavigationOperation do not need special treatment
        return new JPABooleanOperatorImp(this.jpaComplier.getConverter(), operator, (JPAExpression) left,
            (JPAExpression) right);
      }
      if (left instanceof JPANavigationOperation || right instanceof JPANavigationOperation)
        return handleBinaryWithNavigation(operator, left, right);
      if (hasNavigation(left) || hasNavigation(right))
        return new JPANavigationOperation(this.jpaComplier, operator, left, right);
      if (operator == BinaryOperatorKind.EQ
          || operator == BinaryOperatorKind.NE
          || operator == BinaryOperatorKind.GE
          || operator == BinaryOperatorKind.GT
          || operator == BinaryOperatorKind.LT
          || operator == BinaryOperatorKind.LE
          || operator == BinaryOperatorKind.HAS) {
        return new JPAComparisonOperatorImp<>(this.jpaComplier.getConverter(), operator, left, right);
      }
      if (operator == BinaryOperatorKind.ADD
          || operator == BinaryOperatorKind.SUB
          || operator == BinaryOperatorKind.MUL
          || operator == BinaryOperatorKind.DIV
          || operator == BinaryOperatorKind.MOD) {
        return new JPAArithmeticOperatorImp(this.jpaComplier.getConverter(), operator, left, right);
      }
    }
    throw new ODataJPAFilterException(NOT_SUPPORTED_OPERATOR, NOT_IMPLEMENTED, operator.name());
  }

  @Override
  public JPAOperator visitBinaryOperator(final BinaryOperatorKind operator, final JPAOperator left,
      final List<JPAOperator> right)
      throws ExpressionVisitException, ODataApplicationException {
    throw new ODataJPAFilterException(NOT_SUPPORTED_OPERATOR, NOT_IMPLEMENTED, operator.name());
  }

  @Override
  public JPAEnumerationOperator visitEnum(final EdmEnumType type, final List<String> enumValues)
      throws ExpressionVisitException, ODataApplicationException {

    try (JPARuntimeMeasurment meassument = debugger.newMeasurement(this, "visitEnum")) {
      final JPAEnumerationAttribute jpaEnumerationAttribute = this.jpaComplier.getSd().getEnumType(type);
      try {
        if (jpaEnumerationAttribute == null)
          throw new IllegalArgumentException(type.getFullQualifiedName().getFullQualifiedNameAsString() + " unknown");
        if (!jpaEnumerationAttribute.isFlags() && enumValues.size() > 1)
          throw new ODataJPAFilterException(NOT_SUPPORTED_FILTER, NOT_IMPLEMENTED,
              "Collection of Enumerations if not flags");
      } catch (final ODataJPAModelException | IllegalArgumentException e) {
        throw new ODataJPAFilterException(e, INTERNAL_SERVER_ERROR);
      }
      return new JPAEnumerationOperator(this.jpaComplier.getSd().getEnumType(type), enumValues);
    }
  }

  @Override
  public JPAOperator visitLambdaExpression(final String lambdaFunction, final String lambdaVariable,
      final org.apache.olingo.server.api.uri.queryoption.expression.Expression expression)
      throws ExpressionVisitException, ODataApplicationException {

    throw new ODataJPAFilterException(NOT_SUPPORTED_FILTER, NOT_IMPLEMENTED, "Lambda Expression");
  }

  @Override
  public JPAOperator visitLambdaReference(final String variableName) throws ExpressionVisitException,
      ODataApplicationException {

    throw new ODataJPAFilterException(NOT_SUPPORTED_FILTER, NOT_IMPLEMENTED, "Lambda Reference");
  }

  @Override
  public JPAOperator visitLiteral(final Literal literal) throws ExpressionVisitException, ODataApplicationException {

    try (JPARuntimeMeasurment meassument = debugger.newMeasurement(this, "visitLiteral")) {
      return new JPALiteralOperator(this.jpaComplier.getOData(), literal);
    }
  }

  @Override
  public JPAOperator visitMember(final Member member) throws ExpressionVisitException, ODataApplicationException {

    try (JPARuntimeMeasurment meassument = debugger.newMeasurement(this, "visitMember")) {
      final JPAPath attributePath = determineAttributePath(this.jpaComplier.getJpaEntityType(), member,
          jpaComplier.getAssociation());
      checkTransient(attributePath);
      if (getLambdaType(member.getResourcePath()) == UriResourceKind.lambdaAny) {
        return new JPALambdaAnyOperation(this.jpaComplier, member);
      } else if (getLambdaType(member.getResourcePath()) == UriResourceKind.lambdaAll) {
        return new JPALambdaAllOperation(this.jpaComplier, member);
      } else if (isAggregation(member.getResourcePath())) {
        return new JPAAggregationOperationImp(jpaComplier.getRoot(), jpaComplier.getConverter());
      } else if (isCustomFunction(member.getResourcePath())) {
        final UriResource resource = member.getResourcePath().getUriResourceParts().get(0);
        final JPAFunction jpaFunction = this.jpaComplier.getSd().getFunction(
            ((UriResourceFunction) resource).getFunction());
        if (jpaFunction instanceof JPADataBaseFunction) {
          final List<UriParameter> odataParams = ((UriResourceFunction) resource).getParameters();
          return new JPADBFunctionOperator(this, odataParams, (JPADataBaseFunction) jpaFunction);
        } else
          return new JPAJavaFunctionOperator(this, (UriResourceFunction) resource, (JPAJavaFunction) jpaFunction);
      }
      jpaComplier.getWatchDog().ifPresent(watchDog -> watchDog.watch(attributePath));
      return new JPAMemberOperator(this.jpaComplier.getRoot(), member, jpaComplier
          .getAssociation(), this.jpaComplier.getGroups(), attributePath);
    }
  }

  @Override
  public JPAOperator visitMethodCall(final MethodKind methodCall, final List<JPAOperator> parameters)
      throws ExpressionVisitException, ODataApplicationException {

    try (JPARuntimeMeasurment meassument = debugger.newMeasurement(this, "visitMethodCall")) {
      if (!parameters.isEmpty()) {
        if (parameters.get(0) instanceof JPANavigationOperation ||
            (parameters.size() == 2 && parameters.get(1) instanceof JPANavigationOperation))
          throw new ODataJPAFilterException(NOT_SUPPORTED_FILTER, NOT_IMPLEMENTED,
              "Nested method calls together with navigation");
        if (hasNavigation(parameters.get(0)) || (parameters.size() == 2 && hasNavigation(parameters.get(1)))) {
          return new JPANavigationOperation(this.jpaComplier, methodCall, parameters);
        }
      }
      JPAMethodCall method = new JPAMethodCallImp(this.jpaComplier.getConverter(), methodCall, parameters);
      if (method.get() instanceof Predicate)
        method = new JPAMethodBasedExpression(this.jpaComplier.getConverter(), methodCall, parameters);
      return method;
    }
  }

  @Override
  public JPAOperator visitTypeLiteral(final EdmType type) throws ExpressionVisitException, ODataApplicationException {
    throw new ODataJPAFilterException(NOT_SUPPORTED_FILTER, NOT_IMPLEMENTED, "Type Literal");
  }

  @Override
  public JPAOperator visitUnaryOperator(final UnaryOperatorKind operator, final JPAOperator operand)
      throws ExpressionVisitException, ODataApplicationException {

    try (JPARuntimeMeasurment meassument = debugger.newMeasurement(this, "visitUnaryOperator")) {

      if (operator == UnaryOperatorKind.NOT) {
        return new JPAUnaryBooleanOperatorImp(this.jpaComplier.getConverter(), operator, (JPAExpression) operand);
      } else {
        throw new ODataJPAFilterException(NOT_SUPPORTED_OPERATOR, NOT_IMPLEMENTED, operator.name());
      }
    }
  }

  CriteriaBuilder getCriteriaBuilder() {
    return jpaComplier.getConverter().cb;
  }

  UriResourceKind getLambdaType(final UriInfoResource member) {
    for (final UriResource r : member.getUriResourceParts()) {
      if (r.getKind() == UriResourceKind.lambdaAny
          || r.getKind() == UriResourceKind.lambdaAll)
        return r.getKind();
    }
    return null;
  }

  JPAServiceDocument getSd() {
    return jpaComplier.getSd();
  }

  boolean hasNavigation(final JPAOperator operand) {
    if (operand instanceof JPAMemberOperator) {
      final List<UriResource> uriResourceParts = ((JPAMemberOperator) operand).getMember().getResourcePath()
          .getUriResourceParts();
      for (int i = uriResourceParts.size() - 1; i >= 0; i--) {
        if (uriResourceParts.get(i) instanceof UriResourceNavigation
            || (uriResourceParts.get(i) instanceof UriResourceProperty
                && ((UriResourceProperty) uriResourceParts.get(i)).isCollection()))
          return true;
      }
    }
    return false;
  }

  private JPAOperator handleBinaryWithNavigation(final BinaryOperatorKind operator, final JPAOperator left,
      final JPAOperator right) throws ODataJPAFilterException {
    if (left instanceof JPANavigationOperation && right instanceof JPANavigationOperation)
      throw new ODataJPAFilterException(NOT_SUPPORTED_FILTER, NOT_IMPLEMENTED,
          "Binary operations comparing two navigation");

    if (left instanceof JPANavigationOperation) {
      return new JPANavigationOperation(operator, (JPANavigationOperation) left, (JPALiteralOperator) right,
          jpaComplier);
    }
    return new JPANavigationOperation(operator, (JPANavigationOperation) right, (JPALiteralOperator) left, jpaComplier);
  }

  private boolean isAggregation(final UriInfoResource resourcePath) {

    return (resourcePath.getUriResourceParts().size() == 1
        && resourcePath.getUriResourceParts().get(0).getKind() == UriResourceKind.count);
  }

  private boolean isCustomFunction(final UriInfoResource resourcePath) {

    return (!resourcePath.getUriResourceParts().isEmpty()
        && resourcePath.getUriResourceParts().get(0) instanceof UriResourceFunction);
  }

  private @Nullable JPAPath determineAttributePath(@Nullable final JPAEntityType jpaEntityType,
      @Nonnull final Member member, @Nullable final JPAAssociationPath jpaAssociationPath)
      throws ODataApplicationException {

    if (jpaEntityType == null)
      return null;
    final String attributePathName = Utility.determinePropertyNavigationPath(member.getResourcePath()
        .getUriResourceParts());
    JPAPath selectItemPath = null;
    try {
      selectItemPath = jpaEntityType.getPath(attributePathName);
      if (selectItemPath == null && jpaAssociationPath != null) {
        selectItemPath = jpaEntityType.getPath(attributePathName.isEmpty()
            ? jpaAssociationPath.getAlias()
            : (jpaAssociationPath.getAlias() + JPAPath.PATH_SEPARATOR + attributePathName));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAFilterException(e, INTERNAL_SERVER_ERROR);
    }
    return selectItemPath;
  }

  private void checkTransient(@Nullable final JPAPath attributePath) throws ODataApplicationException {

    if (attributePath != null) {
      final Optional<JPAAttribute> transientProperty = attributePath.getPath()
          .stream()
          .map(JPAAttribute.class::cast)
          .filter(JPAAttribute::isTransient)
          .findFirst();
      if (transientProperty.isPresent())
        throw new ODataJPAFilterException(NOT_SUPPORTED_TRANSIENT, NOT_IMPLEMENTED, attributePath.toString());
    }
  }
}