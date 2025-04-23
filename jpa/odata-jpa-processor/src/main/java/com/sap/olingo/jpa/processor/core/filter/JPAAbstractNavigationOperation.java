package com.sap.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.query.ExpressionUtility;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractQuery;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractSubQuery;
import com.sap.olingo.jpa.processor.core.query.JPAExpandItem;
import com.sap.olingo.jpa.processor.core.query.JPANavigationFilterQueryBuilder;
import com.sap.olingo.jpa.processor.core.query.JPANavigationPropertyInfo;
import com.sap.olingo.jpa.processor.core.query.JPANavigationPropertyInfoAccess;

/**
 * In case the query result shall be filtered on an attribute of navigation target a sub-select will be generated.
 *
 * @author Oliver Grande
 *
 */
abstract class JPAAbstractNavigationOperation extends JPAExistsOperation {

  final BinaryOperatorKind operator;
  final JPAMemberOperator jpaMember;
  final MethodKind methodCall;
  private VisitableExpression expression;

  JPAAbstractNavigationOperation(final JPAFilterComplierAccess jpaComplier, final MethodKind methodCall,
      final BinaryOperatorKind operator, final JPAMemberOperator jpaMember) {
    super(jpaComplier);
    this.operator = operator;
    this.methodCall = methodCall;
    this.jpaMember = jpaMember;
  }

  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    try {
      final SubQueryItem existQuery = getExistsQuery();
      return ExpressionUtility.createSubQueryBasedExpression(existQuery.query(), existQuery.jpaPath(), converter.cb,
          expression);

    } catch (final ODataJPAIllegalAccessException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public String getName() {
    return operator != null ? operator.name() : methodCall.name();
  }

  @Override
  SubQueryItem getExistsQuery() throws ODataApplicationException, ODataJPAIllegalAccessException {
    final List<UriResource> allUriResourceParts = new ArrayList<>(uriResourceParts);
    allUriResourceParts.addAll(jpaMember.getMember().getResourcePath().getUriResourceParts());

    // 1. Determine all relevant associations
    final List<JPANavigationPropertyInfo> navigationPathList = determineAssociations(sd, allUriResourceParts);
    JPAAbstractQuery parent = root;
    final List<JPAAbstractSubQuery> queryList = new ArrayList<>();

    // 2. Create the queries and roots
    for (int i = navigationPathList.size() - 1; i >= 0; i--) {
      final JPANavigationPropertyInfoAccess navigationInfo = navigationPathList.get(i);
      if (i == 0) {
        expression = createExpression();
        queryList.add(new JPANavigationFilterQueryBuilder(converter.cb)
            .setOdata(odata)
            .setServiceDocument(sd)
            .setNavigationInfo(navigationInfo)
            .setParent(parent)
            .setEntityManager(em)
            .setExpression(expression)
            .setFrom(determineFrom(i, navigationPathList.size(), parent))
            .setParent(parent)
            .setClaimsProvider(claimsProvider)
            .setGroups(groups)
            .build());
      } else {
        queryList.add(new JPANavigationFilterQueryBuilder(converter.cb)
            .setOdata(odata)
            .setServiceDocument(sd)
            .setNavigationInfo(navigationInfo)
            .setParent(parent)
            .setEntityManager(em)
            .setFrom(determineFrom(i, navigationPathList.size(), parent))
            .setParent(parent)
            .setClaimsProvider(claimsProvider)
            .build());
      }
      parent = queryList.get(queryList.size() - 1);
    }
    // 3. Create select statements
    Subquery<List<Comparable<?>>> childQuery = null;
    List<Path<Comparable<?>>> inPath = Collections.emptyList();
    for (int i = queryList.size() - 1; i >= 0; i--) {
      childQuery = queryList.get(i).getSubQuery(childQuery, expression, inPath);
      inPath = queryList.get(i).getLeftPaths();
    }
    return new SubQueryItem(inPath, childQuery);
  }

  Member getMember() {
    return new SubMember(jpaMember);
  }

  abstract VisitableExpression createExpression() throws ODataJPAFilterException;

  private From<?, ?> determineFrom(final int i, final int size, final JPAAbstractQuery parent) {
    return i == size - 1 ? from : parent.getRoot();
  }

  private static class SubMember implements Member {
    private final JPAMemberOperator parentMember;

    SubMember(final JPAMemberOperator parentMember) {
      super();
      this.parentMember = parentMember;
    }

    @Override
    public <T> T accept(final ExpressionVisitor<T> visitor) throws ODataApplicationException {
      return null;
    }

    @Override
    public UriInfoResource getResourcePath() {
      return new SubResource(parentMember);
    }

    @Override
    public EdmType getStartTypeFilter() {
      return null;
    }

    @Override
    public EdmType getType() {
      return null;
    }

    @Override
    public boolean isCollection() {
      return false;
    }

  }

  private static class SubResource implements JPAExpandItem {
    private final JPAMemberOperator parentMember;

    public SubResource(final JPAMemberOperator member) {
      super();
      this.parentMember = member;
    }

    @Override
    public List<UriResource> getUriResourceParts() {
      final List<UriResource> result = new ArrayList<>();
      final List<UriResource> source = parentMember.getMember().getResourcePath().getUriResourceParts();
      for (int i = source.size() - 1; i > 0; i--) {
        if (source.get(i).getKind() == UriResourceKind.navigationProperty
            || source.get(i).getKind() == UriResourceKind.entitySet
            || (source.get(i) instanceof final UriResourceProperty resourceProperty
                && resourceProperty.isCollection())) {
          break;
        }
        result.add(0, source.get(i));
      }
      return result;
    }

    @Override
    public String getValueForAlias(final String alias) {
      return null;
    }
  }

}
