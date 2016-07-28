package org.apache.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;

/**
 * Creates a sub query for a navigation.
 * @author Oliver Grande
 *
 */
public class JPANavigationQuery extends JPAAbstractQuery {
  private final List<UriParameter> keyPredicates;
  private final JPAAssociationPath association;
  private Root<?> queryRoot;
  private Subquery<?> subQuery;
  private JPAAbstractQuery parentQuery;

//  public JPANavigationQuery(final ServicDocument sd, final UriResource uriResourceItem,
//      final EntityManager em, final JPAAssociationPath association) throws ODataApplicationException {
//
//    super(sd, (EdmEntityType) ((UriResourcePartTyped) uriResourceItem).getType(), em);
//    this.keyPredicates = determineKeyPredicates(uriResourceItem);
//    this.association = association;
//  }

  public <T extends Object> JPANavigationQuery(final ServicDocument sd, final UriResource uriResourceItem,
      final JPAAbstractQuery parent, final EntityManager em, final JPAAssociationPath association)
      throws ODataApplicationException {

    super(sd, (EdmEntityType) ((UriResourcePartTyped) uriResourceItem).getType(), em);
    this.keyPredicates = determineKeyPredicates(uriResourceItem);
    this.association = association;
    this.parentQuery = parent;
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());
    this.queryRoot = subQuery.from(this.jpaEntity.getTypeClass());
  }

  /**
   * @return
   */
  @Override
  public Root<?> getRoot() {
    assert queryRoot != null;
    return queryRoot;
  }

  @Override
  public AbstractQuery<?> getQuery() {
    return subQuery;
  }

  /**
   * Creates a sub query that can be used within an EXISTS condition.
   * @return
   * @throws ODataApplicationException
   */
//  public <T extends Object> Subquery<T> getSubQueryExists() throws ODataApplicationException {
//    return getSubQueryExists(null);
//  }

  @SuppressWarnings("unchecked")
  public <T extends Object> Subquery<T> getSubQueryExists(final Subquery<?> childQuery)
      throws ODataApplicationException {
    final Subquery<T> subQuery = (Subquery<T>) this.subQuery;

    List<JPAOnConditionItem> conditionItems;
    try {
      conditionItems = association.getJoinColumnsList();
      createSelectClause(subQuery, conditionItems);
    } catch (ODataJPAModelException e) {

      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_NAVI_PROPERTY_UNKNOWN,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e, association.getAlias());
    }

    Expression<Boolean> whereCondition = null;
    if (this.keyPredicates == null || this.keyPredicates.isEmpty())
      whereCondition = createWhereByAssociation(parentQuery.getRoot(), queryRoot, conditionItems);
    else
      whereCondition = cb.and(
          createWhereByKey(queryRoot, null, this.keyPredicates),
          createWhereByAssociation(parentQuery.getRoot(), queryRoot, conditionItems));
    if (childQuery != null)
      whereCondition = cb.and(whereCondition, cb.exists(childQuery));
    subQuery.where(whereCondition);
    handleAggregation(subQuery, queryRoot, conditionItems);
    return subQuery;
  }

  protected void handleAggregation(final Subquery<?> subQuery, final Root<?> subRoot,
      final List<JPAOnConditionItem> conditionItems) throws ODataApplicationException {}

  @SuppressWarnings("unchecked")
  protected <T> void createSelectClause(final Subquery<T> subQuery, final List<JPAOnConditionItem> conditionItems) {
    Path<?> p = queryRoot;
    for (final JPAElement jpaPathElement : conditionItems.get(0).getLeftPath().getPath())
      p = p.get(jpaPathElement.getInternalName());
    subQuery.select((Expression<T>) p);
  }

  protected Expression<Boolean> createWhereByAssociation(final From<?, ?> parentFrom, final Root<?> subRoot,
      final List<JPAOnConditionItem> conditionItems) throws ODataApplicationException {
    Expression<Boolean> whereCondition = null;
    for (final JPAOnConditionItem onItem : conditionItems) {
      Path<?> paretPath = parentFrom;
      for (final JPAElement jpaPathElement : onItem.getRightPath().getPath())
        paretPath = paretPath.get(jpaPathElement.getInternalName());
      Path<?> subPath = subRoot;
      for (final JPAElement jpaPathElement : onItem.getLeftPath().getPath())
        subPath = subPath.get(jpaPathElement.getInternalName());

      final Expression<Boolean> equalCondition = cb.equal(paretPath, subPath);
//          parentFrom.get(onItem.getRightPath().getInternalName()),
//          subRoot.get(onItem.getLeftPath().getInternalName()));
      if (whereCondition == null)
        whereCondition = equalCondition;
      else
        whereCondition = cb.and(whereCondition, equalCondition);
    }
    return whereCondition;
  }

  @Override
  protected Locale getLocale() {
    // TODO Auto-generated method stub
    return Locale.GERMANY;
  }
}
