package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceCount;
import org.apache.olingo.server.api.uri.UriResourceLambdaAll;
import org.apache.olingo.server.api.uri.UriResourceLambdaAny;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterElementComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAOperationConverter;

/**
 * Create a sub query to filter on collection properties e.g.
 * <code>CollectionDeeps?$select=ID&$filter=FirstLevel/SecondLevel/Address/any(s:s/TaskID eq 'DEV')</code> or
 * <code>CollectionDeeps?$filter=FirstLevel/SecondLevel/Comment/$count eq 2</code>.
 * This is done as sub-query instead of a join to have more straightforward way to implement OR or AND conditions
 * 
 * @author Oliver Grande
 *
 */
public final class JPACollectionFilterQuery extends JPANavigationQuery {

  public JPACollectionFilterQuery(final OData odata, final JPAServiceDocument sd, final EntityManager em,
      final JPAAbstractQuery parent, final List<UriResource> uriResourceParts, final VisitableExpression expression,
      final From<?, ?> from, final List<String> groups) throws ODataApplicationException {

    this(odata, sd, em, parent, determineAssoziation(parent.jpaEntity, uriResourceParts), expression, from, groups);
  }

  public JPACollectionFilterQuery(final OData odata, final JPAServiceDocument sd, final EntityManager em,
      final JPAAbstractQuery parent, final JPAAssociationPath associationPath, final VisitableExpression expression,
      final From<?, ?> from, final List<String> groups) throws ODataApplicationException {

    super(odata, sd, determineEntityType(parent, associationPath), em, parent, from, associationPath);
    // Create a sub-query having the key of the parent as result type
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());
    this.filterComplier = new JPAFilterElementComplier(odata, sd, em, jpaEntity,
        new JPAOperationConverter(cb, getContext().getOperationConverter()), null, this, expression, association,
        groups);
    this.aggregationType = getAggregationType(this.filterComplier.getExpressionMember());
    createRoots(this.association);
  }

  private static JPAEntityType determineEntityType(final JPAAbstractQuery parent,
      final JPAAssociationPath associationPath) {
    if (associationPath.getLeaf().isComplex())
      return associationPath.getJoinTable().getEntityType();
    else
      return parent.jpaEntity;
  }

  private static JPAAssociationPath determineAssoziation(final JPAEntityType jpaEntity,
      final List<UriResource> uriResourceParts) throws ODataJPAQueryException {
    final StringBuilder pathName = new StringBuilder();
    int i = 0;
    while (uriResourceParts.get(i) != null
        && !(uriResourceParts.get(i) instanceof UriResourceLambdaAny
            || uriResourceParts.get(i) instanceof UriResourceLambdaAll
            || uriResourceParts.get(i) instanceof UriResourceCount)) {
      pathName.append(uriResourceParts.get(i).toString());
      pathName.append(JPAPath.PATH_SEPERATOR);
      i++;
    }
    pathName.deleteCharAt(pathName.lastIndexOf(JPAPath.PATH_SEPERATOR));
    try {
      return jpaEntity.getCollectionAttribute(pathName.toString()).asAssociation();
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Subquery<T> getSubQueryExists(Subquery<?> childQuery) throws ODataApplicationException {

    if (this.queryJoinTable != null) {
      if (this.aggregationType != null) {

        try {
          final List<JPAOnConditionItem> right = association.getJoinTable().getInversJoinColumns();
          createSelectClause(subQuery, queryJoinTable, right);
          Expression<Boolean> whereCondition = createWhereByAssociation(from, queryJoinTable, jpaEntity);
          subQuery.where(applyAdditionalFilter(whereCondition));
          handleAggregation(subQuery, queryJoinTable, right);
        } catch (ODataJPAModelException e) {
          throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
      } else {
        createSubQueryJoinTable();
      }
    }
    return (Subquery<T>) this.subQuery;
  }

  @Override
  public From<?, ?> getRoot() {
    assert queryRoot != null;
    return queryRoot;
  }

  @Override
  protected void createRoots(JPAAssociationPath association) throws ODataJPAQueryException {
    if (association.hasJoinTable()) {
      if (association.getJoinTable().getEntityType() != null) {
        if (aggregationType != null) {
          this.queryJoinTable = subQuery.from(from.getJavaType());
          From<?, ?> p = queryJoinTable;
          for (int i = 0; i < association.getPath().size() - 1; i++)
            p = p.join(association.getPath().get(i).getInternalName());
          this.queryRoot = p.join(association.getLeaf().getInternalName(), JoinType.LEFT);
        } else {
          this.queryRoot = this.queryJoinTable = subQuery.from(association.getJoinTable().getEntityType()
              .getTypeClass());
        }
      } else {
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_NOT_IMPLEMENTED,
            HttpStatusCode.NOT_IMPLEMENTED, association.getAlias());
      }
    } else {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_NOT_IMPLEMENTED,
          HttpStatusCode.NOT_IMPLEMENTED, association.getAlias());
    }
  }

  @Override
  protected void createSubQueryJoinTable() throws ODataApplicationException {
    /*
     * SELECT * FROM "BusinessPartner" AS B
     * WHERE "Type" = '2'
     * AND EXISTS (SELECT "BusinessPartnerID" FROM "Comment" AS C
     * WHERE B."ID" = C."BusinessPartnerID"
     * AND C."Text" LIKE '%just%')
     */
    try {
      final List<JPAOnConditionItem> left = association.getJoinTable().getJoinColumns();
      createSelectClause(subQuery, queryRoot, left);
      Expression<Boolean> whereCondition = createWhereByAssociation(from, queryJoinTable, left);
      subQuery.where(applyAdditionalFilter(whereCondition));
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }
}
