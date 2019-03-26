package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
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
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Binary;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

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
import com.sap.olingo.jpa.processor.core.filter.JPAFilterExpression;
import com.sap.olingo.jpa.processor.core.filter.JPAMemberOperator;

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
          From<?, ?> p = queryJoinTable;
          for (int i = 0; i < association.getPath().size() - 1; i++)
            p = p.join(association.getPath().get(i).getInternalName());
          this.queryRoot = p.join(association.getLeaf().getInternalName(), JoinType.LEFT);
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
      final List<JPAOnConditionItem> left = association.getJoinTable().getJoinColumns(); // Team -->
      final List<JPAOnConditionItem> right = association.getJoinTable().getInversJoinColumns(); // Person -->
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

  @SuppressWarnings("unchecked")
  protected <T> void createSelectClauseAggregation(final Subquery<T> subQuery, final From<?, ?> from,
      final List<JPAOnConditionItem> conditionItems) {
    Path<?> p = from;

    for (final JPAElement jpaPathElement : conditionItems.get(0).getLeftPath().getPath())
      p = p.get(jpaPathElement.getInternalName());
    subQuery.select((Expression<T>) p);
  }

  protected void handleAggregation(final Subquery<?> subQuery, final From<?, ?> subRoot,
      final List<JPAOnConditionItem> conditionItems) throws ODataApplicationException {

    final List<Expression<?>> groupByLIst = new ArrayList<>();
    if (filterComplier != null && this.aggregationType != null) {
      for (final JPAOnConditionItem onItem : conditionItems) {
        Path<?> subPath = subRoot;
        for (final JPAElement jpaPathElement : onItem.getRightPath().getPath())
          subPath = subPath.get(jpaPathElement.getInternalName());
        groupByLIst.add(subPath);
      }
      subQuery.groupBy(groupByLIst);

      try {
        subQuery.having(this.filterComplier.compile());
      } catch (ExpressionVisitException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    }
  }

  protected void createSubQueryJoinTableAggregation() throws ODataApplicationException {
    /*
     * SELECT t0."ID"
     * FROM "OLINGO"."BusinessPartner" t0
     * WHERE (EXISTS (SELECT t1."ID"
     * FROM "OLINGO"."BusinessPartner" t1
     * LEFT OUTER JOIN ("OLINGO"."Membership" t3 JOIN "OLINGO"."Team" t2
     * ON (t2."TeamKey" = t3."TeamID"))
     * ON (t3."PersonID" = t1."ID")
     * WHERE ((t1."ID" = t0."ID")
     * AND (t1."Type" = '1'))
     * GROUP BY t1."ID"
     * HAVING (COUNT(t2."TeamKey") > 0))
     * AND (t0."Type" = '1'))
     */
    try {
      List<JPAOnConditionItem> left = association.getJoinTable().getJoinColumns(); // Person -->
      List<JPAOnConditionItem> right = association.getJoinTable().getInversJoinColumns(); // Team -->
      createSelectClauseAggregation(subQuery, queryJoinTable, left);
      Expression<Boolean> whereCondition = createWhereByAssociation(from, queryJoinTable, parentQuery.jpaEntity);
      subQuery.where(applyAdditionalFilter(whereCondition));
      handleAggregation(subQuery, queryJoinTable, right);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  protected UriResourceKind getAggregationType(final VisitableExpression expression) {
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
}
