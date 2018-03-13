package com.sap.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterElementComplier;

public abstract class JPANavigationQuery extends JPAAbstractQuery {

  protected From<?, ?> queryJoinTable = null;
  protected Subquery<?> subQuery;
  protected final JPAAbstractQuery parentQuery;
  protected UriResourceKind aggregationType;
  protected From<?, ?> queryRoot;
  protected final From<?, ?> from;
  protected final JPAAssociationPath association;
  protected JPAFilterElementComplier filterComplier;

  public JPANavigationQuery(final OData odata, final JPAServiceDocument sd, final EdmEntityType edmEntityType,
      final EntityManager em, final JPAAbstractQuery parent, From<?, ?> from, final JPAAssociationPath association)
      throws ODataApplicationException {

    super(odata, sd, edmEntityType, em);
    this.parentQuery = parent;
    this.from = from;
    this.association = association;
  }

  public JPANavigationQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntity,
      final EntityManager em, final JPAAbstractQuery parent, final From<?, ?> from,
      final JPAAssociationPath association) {

    super(odata, sd, jpaEntity, em);
    this.parentQuery = parent;
    this.from = from;
    this.association = association;
  }

  public abstract <T extends Object> Subquery<T> getSubQueryExists(final Subquery<?> childQuery)
      throws ODataApplicationException;

  @Override
  public AbstractQuery<?> getQuery() {
    return subQuery;
  }

  @Override
  protected Locale getLocale() {
    return locale;
  }

  @Override
  JPAODataSessionContextAccess getContext() {
    return parentQuery.getContext();
  }

  protected void createRoots(final JPAAssociationPath association) throws ODataJPAQueryException {

    if (association.getJoinTable() != null) {
      if (association.getJoinTable().getEntityType() != null) {
        if (aggregationType != null) {
          this.queryJoinTable = subQuery.from(from.getJavaType());
          this.queryRoot = queryJoinTable.join(association.getLeaf().getInternalName(), JoinType.LEFT);
        } else {
          this.queryRoot = subQuery.from(this.jpaEntity.getTypeClass());
          this.queryJoinTable = subQuery.from(association.getJoinTable().getEntityType().getTypeClass());
        }
      } else {
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_NOT_IMPLEMENTED,
            HttpStatusCode.NOT_IMPLEMENTED, association.getAlias());
      }
    } else {
      this.queryRoot = subQuery.from(this.jpaEntity.getTypeClass());
    }
  }

  @SuppressWarnings("unchecked")
  protected <T> void createSelectClause(final Subquery<T> subQuery, final From<?, ?> from,
      final List<JPAOnConditionItem> conditionItems) {
    Path<?> p = from;

    for (final JPAElement jpaPathElement : conditionItems.get(0).getRightPath().getPath())
      p = p.get(jpaPathElement.getInternalName());
    subQuery.select((Expression<T>) p);
  }

  protected Expression<Boolean> createWhereByAssociation(final From<?, ?> subRoot, final From<?, ?> parentFrom,
      final List<JPAOnConditionItem> conditionItems) {

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

  protected Expression<Boolean> applyAdditionalFilter(final Expression<Boolean> where)
      throws ODataApplicationException {

    Expression<Boolean> whereCondition = where;
    if (filterComplier != null && aggregationType == null)
      try {
      if (filterComplier.getExpressionMember() != null)
        whereCondition = addWhereClause(whereCondition, filterComplier.compile());
      } catch (ExpressionVisitException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    return whereCondition;
  }

  protected void createSubQueryJoinTable() throws ODataApplicationException {
    /*
     * SELECT t0."TeamKey"
     * FROM "OLINGO"."Team" t0
     * WHERE (EXISTS (SELECT t2."TeamID"
     * FROM "OLINGO"."BusinessPartner" t1, "OLINGO"."Membership" t2
     * WHERE t2."TeamID" = t0."TeamKey"
     * AND t1."ID" = t2."PersonID"
     * AND t1."Type" = '1'
     * AND t1."NameLine2" = 'Mustermann'))
     */
    try {
      List<JPAOnConditionItem> left = association.getJoinTable().getJoinColumns(); // Team -->
      List<JPAOnConditionItem> right = association.getJoinTable().getInversJoinColumns(); // Person -->
      createSelectClause(subQuery, queryRoot, right);
      Expression<Boolean> whereCondition = createWhereByAssociation(from, queryJoinTable, left);
      whereCondition = cb.and(whereCondition, createWhereByAssociation(queryJoinTable, queryRoot, right));
      subQuery.where(applyAdditionalFilter(whereCondition));
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

  }

  /**
   * Self Join
   * @param subRoot
   * @param parentFrom
   * @param jpaEntity
   * @return
   * @throws ODataJPAModelException
   */
  protected Expression<Boolean> createWhereByAssociation(final From<?, ?> subRoot, final From<?, ?> parentFrom,
      JPAEntityType jpaEntity) throws ODataJPAModelException {
    Expression<Boolean> whereCondition = null;

    for (final JPAPath onItem : jpaEntity.getKeyPath()) {
      Path<?> paretPath = parentFrom;
      Path<?> subPath = subRoot;
      for (final JPAElement jpaPathElement : onItem.getPath()) {
        paretPath = paretPath.get(jpaPathElement.getInternalName());
        subPath = subPath.get(jpaPathElement.getInternalName());
      }
      final Expression<Boolean> equalCondition = cb.equal(paretPath, subPath);
      whereCondition = addWhereClause(whereCondition, equalCondition);
    }
    return whereCondition;
  }
}
