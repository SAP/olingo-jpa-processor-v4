package org.apache.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Subquery;

import org.apache.olingo.jpa.processor.core.query.JPAAbstractQuery;
import org.apache.olingo.jpa.processor.core.query.JPANavigationFilterQuery;
import org.apache.olingo.jpa.processor.core.query.JPANavigationProptertyInfo;
import org.apache.olingo.jpa.processor.core.query.JPANavigationQuery;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceLambdaAll;
import org.apache.olingo.server.api.uri.UriResourceLambdaAny;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

abstract class JPALambdaOperation extends JPAExistsOperation {

  protected final UriInfoResource member;

  JPALambdaOperation(final JPAFilterComplierAccess jpaComplier, final UriInfoResource member) {
    super(jpaComplier);
    this.member = member;
  }

  public JPALambdaOperation(final JPAFilterComplierAccess jpaComplier, final Member member) {
    super(jpaComplier);
    this.member = member.getResourcePath();
  }

  @Override
  protected Subquery<?> getExistsQuery() throws ODataApplicationException {
    return getSubQuery(determineExpression());
  }

  protected final Subquery<?> getSubQuery(final Expression expression) throws ODataApplicationException {
    final List<UriResource> allUriResourceParts = new ArrayList<UriResource>(uriResourceParts);
    allUriResourceParts.addAll(member.getUriResourceParts());

    // 1. Determine all relevant associations
    final List<JPANavigationProptertyInfo> naviPathList = determineAssoziations(sd, allUriResourceParts);
    JPAAbstractQuery parent = root;
    final List<JPANavigationQuery> queryList = new ArrayList<JPANavigationQuery>();

    // 2. Create the queries and roots

    // for (int i = 0; i < naviPathList.size(); i++) {
    for (int i = naviPathList.size() - 1; i >= 0; i--) {
      final JPANavigationProptertyInfo naviInfo = naviPathList.get(i);
      if (i == 0) // naviPathList.size() - 1)
        queryList.add(new JPANavigationFilterQuery(odata, sd, naviInfo.getUriResiource(), parent, em, naviInfo
            .getAssociationPath(), expression));
      else
        queryList.add(new JPANavigationFilterQuery(odata, sd, naviInfo.getUriResiource(), parent, em, naviInfo
            .getAssociationPath()));
      parent = queryList.get(queryList.size() - 1);
    }
    // 3. Create select statements
    Subquery<?> childQuery = null;
    for (int i = queryList.size() - 1; i >= 0; i--) {
      childQuery = queryList.get(i).getSubQueryExists(childQuery);
    }
    return childQuery;
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