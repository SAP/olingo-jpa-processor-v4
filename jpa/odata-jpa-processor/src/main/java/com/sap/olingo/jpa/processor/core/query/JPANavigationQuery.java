package com.sap.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.expression.Binary;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterElementComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterExpression;
import com.sap.olingo.jpa.processor.core.filter.JPAMemberOperator;

/**
 * Creates a sub query for a navigation.
 * @author Oliver Grande
 *
 */
public class JPANavigationQuery extends JPAAbstractQuery {
  private final List<UriParameter> keyPredicates;
  private final JPAAssociationPath association;
  private From<?, ?> queryRoot;
  private Subquery<?> subQuery;
  private JPAAbstractQuery parentQuery;
  private final From<?, ?> from;

  public <T extends Object> JPANavigationQuery(final OData odata, final JPAServiceDocument sd,
      final UriResource uriResourceItem, final JPAAbstractQuery parent, final EntityManager em,
      final JPAAssociationPath association, final From<?, ?> from) throws ODataApplicationException {

    super(odata, sd, (EdmEntityType) ((UriResourcePartTyped) uriResourceItem).getType(), em);
    this.keyPredicates = Util.determineKeyPredicates(uriResourceItem);
    this.association = association;
    this.parentQuery = parent;
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());
    this.queryRoot = subQuery.from(this.jpaEntity.getTypeClass());
    this.locale = parent.getLocale();
    this.from = from;
  }

  @Override
  public AbstractQuery<?> getQuery() {
    return subQuery;
  }

  /**
   * @return
   */
  @Override
  public From<?, ?> getRoot() {
    assert queryRoot != null;
    return queryRoot;
  }

  @SuppressWarnings("unchecked")
  public <T extends Object> Subquery<T> getSubQueryExists(final Subquery<?> childQuery)
      throws ODataApplicationException {
    final Subquery<T> query = (Subquery<T>) this.subQuery;

    List<JPAOnConditionItem> conditionItems = determineJoinColumns();
    createSelectClause(query, conditionItems);
    Expression<Boolean> whereCondition = null;
    if (this.keyPredicates == null || this.keyPredicates.isEmpty())
      whereCondition = createWhereByAssociation(from, queryRoot, conditionItems);
    else
      whereCondition = cb.and(
          createWhereByKey(queryRoot, null, this.keyPredicates, jpaEntity),
          createWhereByAssociation(from, queryRoot, conditionItems));
    if (childQuery != null)
      whereCondition = cb.and(whereCondition, cb.exists(childQuery));
    query.where(whereCondition);
    handleAggregation(query, queryRoot, conditionItems);

    return query;
  }

  @SuppressWarnings("unchecked")
  protected <T> void createSelectClause(final Subquery<T> subQuery, final List<JPAOnConditionItem> conditionItems) {

    Path<?> p = queryRoot;
    for (final JPAElement jpaPathElement : conditionItems.get(0).getLeftPath().getPath())
      p = p.get(jpaPathElement.getInternalName());
    subQuery.select((Expression<T>) p);
  }

  protected Expression<Boolean> createWhereByAssociation(final From<?, ?> parentFrom, final From<?, ?> subRoot,
      final List<JPAOnConditionItem> conditionItems) throws ODataApplicationException { // NOSONAR needed for sub class

    Expression<Boolean> whereCondition = null;
    for (final JPAOnConditionItem onItem : conditionItems) {
      Path<?> paretPath = parentFrom;
      Path<?> subPath = subRoot;
      for (final JPAElement jpaPathElement : onItem.getRightPath().getPath())
        paretPath = paretPath.get(jpaPathElement.getInternalName());
      for (final JPAElement jpaPathElement : onItem.getLeftPath().getPath())
        subPath = subPath.get(jpaPathElement.getInternalName());
      final Expression<Boolean> equalCondition = cb.equal(paretPath, subPath);
      if (whereCondition == null)
        whereCondition = equalCondition;
      else
        whereCondition = cb.and(whereCondition, equalCondition);
    }
    return whereCondition;
  }

  @Override
  protected Locale getLocale() {
    return locale;
  }

  protected void handleAggregation(final Subquery<?> subQuery, final From<?, ?> subRoot,
      final List<JPAOnConditionItem> conditionItems) throws ODataApplicationException {}

  Expression<Boolean> applyAdditionalFilter(JPAFilterElementComplier filterComplier,
      Expression<Boolean> whereCondition) throws ODataApplicationException {

    if (filterComplier != null && getAggregationType(filterComplier.getExpressionMember()) == null)
      try {
      if (filterComplier.getExpressionMember() != null)
        whereCondition = cb.and(whereCondition, filterComplier.compile());
      } catch (ExpressionVisitException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    return whereCondition;
  }

  UriResourceKind getAggregationType(final VisitableExpression expression) {
    UriInfoResource member = null;
    if (expression != null && expression instanceof Binary) {
      if (((Binary) expression).getLeftOperand() instanceof JPAMemberOperator)
        member = ((JPAMemberOperator) ((Binary) expression).getLeftOperand()).getMember().getResourcePath();
      else if (((Binary) expression).getRightOperand() instanceof JPAMemberOperator)
        member = ((JPAMemberOperator) ((Binary) expression).getRightOperand()).getMember().getResourcePath();
    } else if (expression != null && expression instanceof JPAFilterExpression)
      member = ((JPAFilterExpression) expression).getMember();

    if (member != null) {
      for (final UriResource r : member.getUriResourceParts()) {
        if (r.getKind() == UriResourceKind.count)
          return r.getKind();
      }
    }
    return null;
  }

  @Override
  JPAODataSessionContextAccess getContext() {
    return parentQuery.getContext();
  }

  private List<JPAOnConditionItem> determineJoinColumns() throws ODataJPAQueryException {

    try {
      if (association.getJoinTable() != null) {
        // The requested service e.g. 'any' or 'all' is not implemented for '%1$s'
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_NOT_IMPLEMENTED,
            HttpStatusCode.NOT_IMPLEMENTED, association.getAlias());
      } else {
        List<JPAOnConditionItem> conditionItems = association.getJoinColumnsList();
        if (conditionItems.isEmpty())
          throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_JOIN_NOT_DEFINED,
              HttpStatusCode.INTERNAL_SERVER_ERROR, association.getTargetType().getExternalName(), association
                  .getSourceType().getExternalName());
        return conditionItems;
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_NAVI_PROPERTY_UNKNOWN,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e, association.getAlias());
    }
  }
}
