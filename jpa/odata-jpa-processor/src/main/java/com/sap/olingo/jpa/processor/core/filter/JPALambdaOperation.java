package com.sap.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceLambdaAll;
import org.apache.olingo.server.api.uri.UriResourceLambdaAny;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.processor.core.query.JPAAbstractQuery;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractSubQuery;
import com.sap.olingo.jpa.processor.core.query.JPANavigationFilterQueryBuilder;
import com.sap.olingo.jpa.processor.core.query.JPANavigationPropertyInfo;
import com.sap.olingo.jpa.processor.core.query.JPANavigationPropertyInfoAccess;

abstract class JPALambdaOperation extends JPAExistsOperation {

  protected final UriInfoResource member;

  JPALambdaOperation(final JPAFilterComplierAccess jpaCompiler, final Member member) {
    super(jpaCompiler);
    this.member = member.getResourcePath();
  }

  @Override
  protected SubQueryItem getExistsQuery() throws ODataApplicationException {
    return new SubQueryItem(Collections.emptyList(), getSubQuery(determineExpression()));
  }

  @SuppressWarnings("unchecked")
  protected final <S> Subquery<S> getSubQuery(final Expression expression)
      throws ODataApplicationException {
    // Add association root, which is only available for the first lambda expression
    final List<UriResource> allUriResourceParts = new ArrayList<>();
    if (uriResourceParts != null)
      allUriResourceParts.addAll(uriResourceParts);
    // Add association path
    allUriResourceParts.addAll(member.getUriResourceParts());

    // 1. Determine all relevant associations
    final List<JPANavigationPropertyInfo> navigationPathList = determineAssociations(sd, allUriResourceParts);
    JPAAbstractQuery parent = root;
    final List<JPAAbstractSubQuery> queryList = new ArrayList<>();

    // 2. Create the queries and roots
    for (int i = navigationPathList.size() - 1; i >= 0; i--) {
      final JPANavigationPropertyInfoAccess navigationInfo = navigationPathList.get(i);
      if (i == 0) {
        queryList.add(new JPANavigationFilterQueryBuilder(converter.cb)
            .setOdata(odata)
            .setServiceDocument(sd)
            .setNavigationInfo(navigationInfo)
            .setParent(parent)
            .setEntityManager(em)
            .setExpression(expression)
            .setFrom(from)
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
            .setFrom(from)
            .setParent(parent)
            .setClaimsProvider(claimsProvider)
            .build());
      }
      parent = queryList.get(queryList.size() - 1);
    }
    // 3. Create select statements
    Subquery<?> childQuery = null;
    for (int i = queryList.size() - 1; i >= 0; i--) {
      childQuery = queryList.get(i).getSubQuery(childQuery, null, Collections.emptyList());
    }
    return (Subquery<S>) childQuery;
  }

  Expression determineExpression() {
    for (final UriResource uriResource : member.getUriResourceParts()) {
      if (uriResource.getKind() == UriResourceKind.lambdaAny)
        return ((UriResourceLambdaAny) uriResource).getExpression();
      else if (uriResource.getKind() == UriResourceKind.lambdaAll)
        return ((UriResourceLambdaAll) uriResource).getExpression();
    }
    return null;
  }
}