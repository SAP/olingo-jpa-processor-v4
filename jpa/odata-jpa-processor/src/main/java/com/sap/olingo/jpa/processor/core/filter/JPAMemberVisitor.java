package com.sap.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.query.Util;

final class JPAMemberVisitor implements ExpressionVisitor<JPAPath> {
  private final ArrayList<JPAPath> pathList = new ArrayList<>();
  private final JPAEntityType jpaEntityType;

  public JPAMemberVisitor(final JPAEntityType jpaEntityType) {
    super();
    this.jpaEntityType = jpaEntityType;
  }

  public List<JPAPath> get() {
    return pathList;
  }

  @Override
  public JPAPath visitBinaryOperator(final BinaryOperatorKind operator, final JPAPath left, final JPAPath right)
      throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitBinaryOperator(final BinaryOperatorKind operator, final JPAPath left, final List<JPAPath> right)
      throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitUnaryOperator(final UnaryOperatorKind operator, final JPAPath operand)
      throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitMethodCall(final MethodKind methodCall, final List<JPAPath> parameters)
      throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitLambdaExpression(final String lambdaFunction, final String lambdaVariable,
      final org.apache.olingo.server.api.uri.queryoption.expression.Expression expression)
      throws ExpressionVisitException,
      ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitLiteral(final Literal literal) throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitMember(final Member member) throws ExpressionVisitException, ODataApplicationException {
    final UriResourceKind uriResourceKind = member.getResourcePath().getUriResourceParts().get(0).getKind();

    if ((uriResourceKind == UriResourceKind.primitiveProperty || uriResourceKind == UriResourceKind.complexProperty)
        && !Util.hasNavigation(member.getResourcePath().getUriResourceParts())) {
      final String path = Util.determinePropertyNavigationPath(member.getResourcePath().getUriResourceParts());
      JPAPath selectItemPath = null;
      try {
        selectItemPath = jpaEntityType.getPath(path);
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
      if (selectItemPath != null) {
        pathList.add(selectItemPath);
        return selectItemPath;
      }
    }
    return null;
  }

  @Override
  public JPAPath visitAlias(final String aliasName) throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitTypeLiteral(final EdmType type) throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitLambdaReference(final String variableName) throws ExpressionVisitException,
      ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitEnum(final EdmEnumType type, final List<String> enumValues) throws ExpressionVisitException,
      ODataApplicationException {
    return null;
  }
}
