package org.apache.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import org.apache.olingo.jpa.processor.core.query.JPAAbstractQuery;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

/**
 * Cross compiles Olingo generated AST of an OData filter into JPA criteria builder where condition.
 * 
 * Details can be found:
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398301"
 * >OData Version 4.0 Part 1 - 11.2.5.1 System Query Option $filter </a>
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398094"
 * >OData Version 4.0 Part 2 - 5.1.1 System Query Option $filter</a>
 * <a href=
 * "https://tools.oasis-open.org/version-control/browse/wsvn/odata/trunk/spec/ABNF/odata-abnf-construction-rules.txt">
 * odata-abnf-construction-rules</a>
 * @author Oliver Grande
 *
 */
//TODO handle $it ...
public class JPAFilterCrossComplier implements JPAFilterComplier, JPAFilterComplierAccess {
  final JPAOperationConverter converter;
  private final FilterOption filterTree;
  // TODO Check if it is allowed to select via navigation
  // ...Organizations?$select=Roles/RoleCategory eq 'C'
  // see also https://issues.apache.org/jira/browse/OLINGO-414
  final JPAEntityType jpaEntityType;
  final EntityManager em;
  final OData odata;
  final ServicDocument sd;
  final List<UriResource> uriResourceParts;
  final JPAAbstractQuery parent;

  public JPAFilterCrossComplier(final OData odata, final ServicDocument sd, final EntityManager em,
      final JPAEntityType jpaEntityType, final JPAOperationConverter converter,
      final UriInfoResource uriResource, final JPAAbstractQuery parent) {
    super();
    this.converter = converter;
    if (uriResource != null) {
      this.filterTree = uriResource.getFilterOption();
      this.uriResourceParts = uriResource.getUriResourceParts();
    } else {
      this.filterTree = null;
      this.uriResourceParts = null;
    }
    this.jpaEntityType = jpaEntityType;
    this.em = em;
    this.odata = odata;
    this.sd = sd;
    this.parent = parent;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.jpa.processor.core.filter.JPAFilterComplier#compile()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Expression<Boolean> compile() throws ExpressionVisitException, ODataApplicationException {

    if (filterTree == null)
      return null;

    final ExpressionVisitor<JPAOperator> visitor = new JPAVisitor(this);
    final org.apache.olingo.server.api.uri.queryoption.expression.Expression e = filterTree.getExpression();
    return (Expression<Boolean>) e.accept(visitor).get();
  }

  @Override
  public JPAOperationConverter getConverter() {
    return converter;
  }

  @Override
  public JPAEntityType getJpaEntityType() {
    return jpaEntityType;
  }

  @Override
  public EntityManager getEntityManager() {
    return em;
  }

  @Override
  public OData getOdata() {
    return odata;
  }

  @Override
  public ServicDocument getSd() {
    return sd;
  }

  @Override
  public List<UriResource> getUriResourceParts() {
    return uriResourceParts;
  }

  @Override
  public JPAAbstractQuery getParent() {
    return parent;
  }

  @Override
  public List<JPAPath> getMemeber() {
    MemberVisitor visitor = new MemberVisitor();
    if (filterTree != null) {
      try {
        filterTree.getExpression().accept(visitor);
      } catch (ExpressionVisitException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ODataApplicationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return Collections.unmodifiableList(visitor.get());
    } else
      return null;
  }

  private class MemberVisitor implements ExpressionVisitor<JPAPath> {
    private final ArrayList<JPAPath> pathList = new ArrayList<JPAPath>();

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
}
