package org.apache.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServiceDocument;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import org.apache.olingo.jpa.processor.core.filter.JPAFilterElementComplier;
import org.apache.olingo.jpa.processor.core.filter.JPAOperationConverter;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

public class JPANavigationFilterQuery extends JPANavigationQuery {

  final private JPAFilterElementComplier filterComplier;

  public JPANavigationFilterQuery(final OData odata, final ServiceDocument sd, final UriResource uriResourceItem,
      final JPAAbstractQuery parent, final EntityManager em, final JPAAssociationPath association)
      throws ODataApplicationException {

    super(odata, sd, uriResourceItem, parent, em, association);
    this.filterComplier = null;
  }

  public JPANavigationFilterQuery(final OData odata, final ServiceDocument sd, final UriResource uriResourceItem,
      final JPAAbstractQuery parent, final EntityManager em, final JPAAssociationPath association,
      final VisitableExpression expression) throws ODataApplicationException {

    super(odata, sd, uriResourceItem, parent, em, association);
    this.filterComplier = new JPAFilterElementComplier(odata, sd, em, jpaEntity, new JPAOperationConverter(cb,
        getContext().getOperationConverter()), null, this, expression);
    createDescriptionJoin(filterComplier);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T> void createSelectClause(final Subquery<T> subQuery, final List<JPAOnConditionItem> conditionItems) {
    Path<?> p = getRoot();
    for (final JPAElement jpaPathElement : conditionItems.get(0).getRightPath().getPath())
      p = p.get(jpaPathElement.getInternalName());
    subQuery.select((Expression<T>) p);
  }

  @Override
  protected Expression<Boolean> createWhereByAssociation(final From<?, ?> parentFrom, final From<?, ?> subRoot,
      final List<JPAOnConditionItem> conditionItems) throws ODataApplicationException {

    Expression<Boolean> whereCondition = super.createWhereByAssociation(subRoot, parentFrom, conditionItems);
    return applyAdditionalFilter(filterComplier, whereCondition);
  }

  @Override
  protected void handleAggregation(final Subquery<?> subQuery, final Root<?> subRoot,
      final List<JPAOnConditionItem> conditionItems) throws ODataApplicationException {

    final List<Expression<?>> groupByLIst = new ArrayList<Expression<?>>();
    if (filterComplier != null && getAggregationType(this.filterComplier.getExpressionMember()) != null) {
      for (final JPAOnConditionItem onItem : conditionItems) {
        Path<?> subPath = subRoot;
        for (final JPAElement jpaPathElement : onItem.getRightPath().getPath())
          subPath = subPath.get(jpaPathElement.getInternalName());
        groupByLIst.add(subPath);
      }
      subQuery.groupBy(groupByLIst);

      // subQuery.having(cb.greaterThan(cb.count(this.getRoot().get("roleCategory")), new Long(2)));
      try {
        subQuery.having(this.filterComplier.compile());
      } catch (ExpressionVisitException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    }
  }

  private void createDescriptionJoin(JPAFilterElementComplier filterComplier) throws ODataApplicationException {
    final HashMap<String, From<?, ?>> joinTables = new HashMap<String, From<?, ?>>();
    generateDesciptionJoin(joinTables, determineAllDescriptionPath());
  }

  private Set<JPAPath> determineAllDescriptionPath() {
    Set<JPAPath> allPath = new HashSet<JPAPath>();
    if (filterComplier != null) {
      for (JPAPath path : filterComplier.getMember()) {
        if (path.getLeaf() instanceof JPADescriptionAttribute)
          allPath.add(path);
      }
    }
    return allPath;
  }
}
