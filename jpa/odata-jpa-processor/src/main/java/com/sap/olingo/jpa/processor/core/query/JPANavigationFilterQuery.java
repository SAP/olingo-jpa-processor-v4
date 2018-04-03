package com.sap.olingo.jpa.processor.core.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterElementComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAOperationConverter;

public final class JPANavigationFilterQuery extends JPANavigationQuery {
  private final List<UriParameter> keyPredicates;

  public JPANavigationFilterQuery(final OData odata, final JPAServiceDocument sd, final UriResource uriResourceItem,
      final JPAAbstractQuery parent, final EntityManager em, final JPAAssociationPath association,
      final From<?, ?> from) throws ODataApplicationException {

    super(odata, sd, (EdmEntityType) ((UriResourcePartTyped) uriResourceItem).getType(), em, parent, from, association);
    this.keyPredicates = Util.determineKeyPredicates(uriResourceItem);
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());

    this.locale = parent.getLocale();
    this.filterComplier = null;
    this.aggregationType = null;
    createRoots(association);
  }

  public JPANavigationFilterQuery(final OData odata, final JPAServiceDocument sd, final UriResource uriResourceItem,
      final JPAAbstractQuery parent, final EntityManager em, final JPAAssociationPath association,
      final VisitableExpression expression, final From<?, ?> from) throws ODataApplicationException {

    super(odata, sd, (EdmEntityType) ((UriResourcePartTyped) uriResourceItem).getType(), em, parent, from,
        association);
    this.keyPredicates = Util.determineKeyPredicates(uriResourceItem);
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());

    this.locale = parent.getLocale();

    this.filterComplier = new JPAFilterElementComplier(odata, sd, em, jpaEntity, new JPAOperationConverter(cb,
        getContext().getOperationConverter()), null, this, expression, null);
    this.aggregationType = getAggregationType(this.filterComplier.getExpressionMember());
    createRoots(association);
    createDescriptionJoin();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.query.JPANavigationQuery#getRoot()
   */
  @Override
  public From<?, ?> getRoot() {
    assert queryRoot != null;
    return queryRoot;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.sap.olingo.jpa.processor.core.query.JPANavigationQuery#getSubQueryExists(javax.persistence.criteria.Subquery)
   */

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> Subquery<T> getSubQueryExists(final Subquery<?> childQuery)
      throws ODataApplicationException {
    final Subquery<T> query = (Subquery<T>) this.subQuery;

    if (this.queryJoinTable != null) {
      if (this.aggregationType != null)
        createSubQueryJoinTableAggregation();
      else
        createSubQueryJoinTable();
    } else {
      createSubQueryAggregation(childQuery, query);
    }
    return query;
  }

  private void createDescriptionJoin() throws ODataApplicationException {
    final HashMap<String, From<?, ?>> joinTables = new HashMap<>();
    generateDesciptionJoin(joinTables, determineAllDescriptionPath(), getRoot());
  }

  private <T> void createSubQueryAggregation(final Subquery<?> childQuery, final Subquery<T> query)
      throws ODataApplicationException {

    List<JPAOnConditionItem> conditionItems = determineJoinColumns();
    createSelectClause(query, queryRoot, conditionItems);
    Expression<Boolean> whereCondition = null;
    if (this.keyPredicates == null || this.keyPredicates.isEmpty())
      whereCondition = createWhereByAssociation(from, queryRoot, conditionItems);
    else
      whereCondition = cb.and(
          createWhereByKey(queryRoot, null, this.keyPredicates, jpaEntity),
          createWhereByAssociation(from, queryRoot, conditionItems));
    if (childQuery != null)
      whereCondition = cb.and(whereCondition, cb.exists(childQuery));
    query.where(applyAdditionalFilter(whereCondition));
    handleAggregation(query, queryRoot, conditionItems);
  }

  private Set<JPAPath> determineAllDescriptionPath() throws ODataApplicationException {
    Set<JPAPath> allPath = new HashSet<>();
    if (filterComplier != null) {
      for (JPAPath path : filterComplier.getMember()) {
        if (path.getLeaf() instanceof JPADescriptionAttribute)
          allPath.add(path);
      }
    }
    return allPath;
  }

  private List<JPAOnConditionItem> determineJoinColumns() throws ODataJPAQueryException {

    try {
      List<JPAOnConditionItem> conditionItems = association.getJoinColumnsList();
      if (conditionItems.isEmpty())
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_JOIN_NOT_DEFINED,
            HttpStatusCode.INTERNAL_SERVER_ERROR, association.getTargetType().getExternalName(), association
                .getSourceType().getExternalName());
      return conditionItems;

    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_NAVI_PROPERTY_UNKNOWN,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e, association.getAlias());
    }
  }
}
