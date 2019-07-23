package com.sap.olingo.jpa.processor.core.filter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractQuery;

/**
 * Cross compiles Olingo generated AST of an OData filter into JPA criteria builder where condition.
 * 
 * Details can be found:
 * <ul>
 * <li><a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398301"
 * >OData Version 4.0 Part 1 - 11.2.5.1 System Query Option $filter </a>
 * <li><a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398094"
 * >OData Version 4.0 Part 2 - 5.1.1 System Query Option $filter</a>
 * <li><a href=
 * "https://tools.oasis-open.org/version-control/browse/wsvn/odata/trunk/spec/ABNF/odata-abnf-construction-rules.txt">
 * odata-abnf-construction-rules</a>
 * </ul>
 * @author Oliver Grande
 *
 */
//TODO handle $it ...
public final class JPAFilterCrossComplier extends JPAAbstractFilter {
  final JPAOperationConverter converter;
  // TODO Check if it is allowed to select via navigation
  // ...Organizations?$select=Roles/RoleCategory eq 'C'
  // see also https://issues.apache.org/jira/browse/OLINGO-414
  final EntityManager em;
  final OData odata;
  final JPAServiceDocument sd;
  final List<UriResource> uriResourceParts;
  final JPAAbstractQuery parent;
  final Optional<JPAODataClaimProvider> claimsProvider;
  final List<String> groups;
  private From<?, ?> root;

  public JPAFilterCrossComplier(final OData odata, final JPAServiceDocument sd,
      final JPAEntityType jpaEntityType, final JPAOperationConverter converter,
      final JPAAbstractQuery parent, From<?, ?> from, final JPAAssociationPath assization,
      final JPAODataRequestContextAccess requestContext) {

    this(odata, sd, jpaEntityType, converter, parent, assization, requestContext);
    this.root = from;
  }

  public JPAFilterCrossComplier(final OData odata, final JPAServiceDocument sd,
      final JPAEntityType jpaEntityType, final JPAOperationConverter converter, final JPAAbstractQuery parent,
      final JPAAssociationPath assization, final JPAODataRequestContextAccess requestContext) {

    super(jpaEntityType, requestContext.getUriInfo(), assization);
    final Optional<JPAODataGroupProvider> groupsProvider = requestContext.getGroupsProvider();
    this.uriResourceParts = requestContext.getUriInfo().getUriResourceParts();
    this.converter = converter;
    this.em = requestContext.getEntityManager();
    this.odata = odata;
    this.sd = sd;
    this.parent = parent;
    this.claimsProvider = requestContext.getClaimsProvider();
    this.groups = groupsProvider.isPresent() ? groupsProvider.get().getGroups() : Collections.emptyList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAFilterComplier#compile()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Expression<Boolean> compile() throws ExpressionVisitException, ODataApplicationException {
    final int handle = parent.getDebugger().startRuntimeMeasurement("JPAFilterCrossComplier", "compile");

    if (expression == null) {
      parent.getDebugger().stopRuntimeMeasurement(handle);
      return null;
    }
    final ExpressionVisitor<JPAOperator> visitor = new JPAVisitor(this);
    final Expression<Boolean> finalExpression = (Expression<Boolean>) expression.accept(visitor).get();

    parent.getDebugger().stopRuntimeMeasurement(handle);
    return finalExpression;
  }

  @Override
  public Optional<JPAODataClaimProvider> getClaimsProvider() {
    return claimsProvider;
  }

  @Override
  public JPAOperationConverter getConverter() {
    return converter;
  }

  @Override
  public JPAServiceDebugger getDebugger() {
    return parent.getDebugger();
  }

  @Override
  public EntityManager getEntityManager() {
    return em;
  }

  @Override
  public JPAEntityType getJpaEntityType() {
    return jpaEntityType;
  }

  @Override
  public OData getOdata() {
    return odata;
  }

  @Override
  public JPAAbstractQuery getParent() {
    return parent;
  }

  @Override
  public From<?, ?> getRoot() {
    if (root == null)
      return parent.getRoot();
    return root;
  }

  @Override
  public JPAServiceDocument getSd() {
    return sd;
  }

  @Override
  public List<UriResource> getUriResourceParts() {
    return uriResourceParts;
  }

  @Override
  public List<String> getGroups() {
    return groups;
  }

}
