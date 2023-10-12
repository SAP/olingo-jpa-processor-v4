package com.sap.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.From;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.expression.Binary;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.filter.JPACountExpression;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterExpression;
import com.sap.olingo.jpa.processor.core.filter.JPAMemberOperator;
import com.sap.olingo.jpa.processor.core.filter.JPANullExpression;
import com.sap.olingo.jpa.processor.core.filter.JPAVisitableExpression;

/**
 * Three types of navigation filter queries have to be supported:
 * <ol>
 * <li>Filter on a given value: AdministrativeDivisions?$filter=Parent/Parent/CodeID eq 'NUTS1' and DivisionCode eq
 * 'BE212'</li>
 * <li>Filter on the number of items of the relation target:
 * CollectionDeeps?$filter=FirstLevel/SecondLevel/Comment/$count eq 2&</li>
 * <li>Filter on the existence (to one) of an item: AssociationOneToOneSources?$filter=ColumnTarget eq null</li>
 * </ol>
 * The builder creates the corresponding query implementation.
 * @author Oliver Grande
 * @since 1.1.1
 * 28.07.2023
 */
public class JPANavigationFilterQueryBuilder {

  private static final String NULL = "null";

  private OData odata;
  private JPAServiceDocument sd;
  private JPAAbstractQuery parent;
  private EntityManager em;
  private JPAAssociationPath association;
  private VisitableExpression expression;
  private From<?, ?> from;
  private Optional<JPAODataClaimProvider> claimsProvider;
  private List<String> groups;
  private List<UriParameter> keyPredicates;
  private EdmEntityType type;

  public JPANavigationSubQuery build() throws ODataApplicationException {
    final JPANavigationSubQuery query;
    if (expression != null
        && getAggregationType(expression) != null)
      query = new JPANavigationCountQuery(odata, sd,
          type, em, parent, from, association, claimsProvider, keyPredicates);
    else if (expression != null
        && isNullExpression(expression))
      query = new JPANavigationNullQuery(odata, sd,
          type, em, parent, from, association, claimsProvider, keyPredicates);
    else
      query = new JPANavigationFilterQuery(odata, sd,
          type, em, parent, from, association, claimsProvider, keyPredicates);
    query.buildExpression(expression, groups);
    return query;
  }

  public JPANavigationFilterQueryBuilder setOdata(final OData odata) {
    this.odata = odata;
    return this;
  }

  public JPANavigationFilterQueryBuilder setServiceDocument(final JPAServiceDocument sd) {
    this.sd = sd;
    return this;
  }

  public JPANavigationFilterQueryBuilder setUriResourceItem(final UriResource uriResourceItem)
      throws ODataApplicationException {
    keyPredicates = Utility.determineKeyPredicates(uriResourceItem);
    type = (EdmEntityType) ((UriResourcePartTyped) uriResourceItem).getType();
    return this;
  }

  public JPANavigationFilterQueryBuilder setParent(final JPAAbstractQuery parent) {
    this.parent = parent;
    return this;
  }

  public JPANavigationFilterQueryBuilder setEntityManager(final EntityManager em) {
    this.em = em;
    return this;
  }

  public JPANavigationFilterQueryBuilder setAssociation(final JPAAssociationPath association) {
    this.association = association;
    return this;
  }

  public JPANavigationFilterQueryBuilder setExpression(final VisitableExpression expression) {
    this.expression = expression;
    return this;
  }

  public JPANavigationFilterQueryBuilder setFrom(final From<?, ?> from) {
    this.from = from;
    return this;
  }

  public JPANavigationFilterQueryBuilder setClaimsProvider(final JPAODataClaimProvider claimsProvider) {
    this.claimsProvider = Optional.ofNullable(claimsProvider);
    return this;
  }

  public JPANavigationFilterQueryBuilder setClaimsProvider(final Optional<JPAODataClaimProvider> claimsProvider) {
    this.claimsProvider = claimsProvider;
    return this;
  }

  public JPANavigationFilterQueryBuilder setGroups(final List<String> groups) {
    this.groups = groups;
    return this;
  }

  private UriResourceKind getAggregationType(final VisitableExpression expression) {
    UriInfoResource member = null;
    if (expression instanceof final Binary binary) {
      if (binary.getLeftOperand() instanceof final JPAMemberOperator leftMember)
        member = leftMember.getMember().getResourcePath();
      else if (binary.getRightOperand() instanceof final JPAMemberOperator rightMember)
        member = rightMember.getMember().getResourcePath();
    } else if (expression instanceof JPAFilterExpression
        || expression instanceof JPACountExpression) {
      member = ((JPAVisitableExpression) expression).getMember();
    }
    if (member != null) {
      for (final UriResource r : member.getUriResourceParts()) {
        if (r.getKind() == UriResourceKind.count)
          return r.getKind();
      }
    }
    return null;
  }

  private boolean isNullExpression(final VisitableExpression expression) {

    return expression instanceof final JPANullExpression nullExpression
        && NULL.equals(nullExpression.getLiteral().getText());
  }

}
