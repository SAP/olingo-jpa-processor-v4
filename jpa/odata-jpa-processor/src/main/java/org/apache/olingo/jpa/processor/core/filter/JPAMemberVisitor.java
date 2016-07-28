package org.apache.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

class JPAMemberVisitor implements ExpressionVisitor<JPAPath> {
  private final ArrayList<JPAPath> pathList = new ArrayList<JPAPath>();
  private final JPAEntityType jpaEntityType;

  public JPAMemberVisitor(JPAEntityType jpaEntityType) {
    super();
    this.jpaEntityType = jpaEntityType;
  }

  public List<JPAPath> get() {
    return pathList;
  }

  @Override
  public JPAPath visitBinaryOperator(BinaryOperatorKind operator, JPAPath left, JPAPath right)
      throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitUnaryOperator(UnaryOperatorKind operator, JPAPath operand) throws ExpressionVisitException,
      ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitMethodCall(MethodKind methodCall, List<JPAPath> parameters) throws ExpressionVisitException,
      ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitLambdaExpression(String lambdaFunction, String lambdaVariable,
      org.apache.olingo.server.api.uri.queryoption.expression.Expression expression) throws ExpressionVisitException,
      ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
    UriResourceKind uriResourceKind = member.getResourcePath().getUriResourceParts().get(0).getKind();

    if (uriResourceKind == UriResourceKind.primitiveProperty || uriResourceKind == UriResourceKind.complexProperty) {
      if (!Util.hasNavigation(member.getResourcePath().getUriResourceParts())) {
        final String path = Util.determineProptertyNavigationPath(member.getResourcePath().getUriResourceParts());
        JPAPath selectItemPath = null;
        try {
          selectItemPath = jpaEntityType.getPath(path);
        } catch (ODataJPAModelException e) {
          throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
        if (selectItemPath != null) {
          pathList.add(selectItemPath);
          return selectItemPath;
        }
      }
    }
    return null;
  }

  @Override
  public JPAPath visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitLambdaReference(String variableName) throws ExpressionVisitException,
      ODataApplicationException {
    return null;
  }

  @Override
  public JPAPath visitEnum(EdmEnumType type, List<String> enumValues) throws ExpressionVisitException,
      ODataApplicationException {
    return null;
  }

}
