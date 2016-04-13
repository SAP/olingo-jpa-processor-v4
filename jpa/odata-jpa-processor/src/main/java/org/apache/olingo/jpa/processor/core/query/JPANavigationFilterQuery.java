package org.apache.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.filter.JPAFilterElementComplier;
import org.apache.olingo.jpa.processor.core.filter.JPAFilterExpression;
import org.apache.olingo.jpa.processor.core.filter.JPAMemberOperator;
import org.apache.olingo.jpa.processor.core.filter.JPAOperationConverter;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Binary;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

public class JPANavigationFilterQuery extends JPANavigationQuery {

  private final JPAFilterElementComplier filterComplier;

  public JPANavigationFilterQuery(final OData odata, final ServicDocument sd, final UriResource uriResourceItem,
      final JPAAbstractQuery parent, final EntityManager em, final JPAAssociationPath association)
      throws ODataApplicationException {
    super(sd, uriResourceItem, parent, em, association);
    this.filterComplier = null;
  }

  public JPANavigationFilterQuery(final OData odata, final ServicDocument sd, final UriResource uriResourceItem,
      final JPAAbstractQuery parent, final EntityManager em, final JPAAssociationPath association,
      final VisitableExpression expression) throws ODataApplicationException {
    super(sd, uriResourceItem, parent, em, association);
    this.filterComplier = new JPAFilterElementComplier(odata, sd, em, jpaEntity, new JPAOperationConverter(cb), null,
        this, expression);
  }

  @Override
  protected Expression<Boolean> createWhereByAssociation(final From<?, ?> parentFrom, final Root<?> subRoot,
      final List<JPAOnConditionItem> conditionItems) throws ODataApplicationException {

    Expression<Boolean> whereCondition = null;
    for (final JPAOnConditionItem onItem : conditionItems) {
      Path<?> paretPath = parentFrom;
      Path<?> subPath = subRoot;
      for (final JPAElement jpaPathElement : onItem.getRightPath().getPath())
        subPath = subPath.get(jpaPathElement.getInternalName());
      for (final JPAElement jpaPathElement : onItem.getLeftPath().getPath())
        paretPath = paretPath.get(jpaPathElement.getInternalName());
      final Expression<Boolean> equalCondition = cb.equal(paretPath, subPath);
//          parentFrom.get(onItem.getRightPath().getInternalName()),
//          subRoot.get(onItem.getLeftPath().getInternalName()));
      if (whereCondition == null)
        whereCondition = equalCondition;
      else
        whereCondition = cb.and(whereCondition, equalCondition);
    }
    if (filterComplier != null && getAggregationType(this.filterComplier.getExpressionMember()) == null)
      try {
      if (filterComplier.getExpressionMember() != null)
        whereCondition = cb.and(whereCondition, filterComplier.compile());
      } catch (ExpressionVisitException e) {
      throw new ODataApplicationException("Expression error", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          Locale.ENGLISH, e);
      }
    return whereCondition;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T> void createSelectClause(final Subquery<T> subQuery, List<JPAOnConditionItem> conditionItems) {
    Path<?> p = getRoot();
    for (final JPAElement jpaPathElement : conditionItems.get(0).getRightPath().getPath())
      p = p.get(jpaPathElement.getInternalName());
    subQuery.select((Expression<T>) p);
  }

  @Override
  protected void handleAggregation(final Subquery<?> subQuery, final Root<?> subRoot,
      final List<JPAOnConditionItem> conditionItems) throws ODataApplicationException {

    List<Expression<?>> groupByLIst = new ArrayList<Expression<?>>();
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
        throw new ODataApplicationException("Expression error", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
            Locale.ENGLISH, e);
      }
    }
  }

  private UriResourceKind getAggregationType(VisitableExpression expression) {
    UriInfoResource member = null;
    if (expression != null && expression instanceof Binary) {
      if (((Binary) expression).getLeftOperand() instanceof JPAMemberOperator)
        member = ((JPAMemberOperator) ((Binary) expression).getLeftOperand()).getMember().getResourcePath();
      else if (((Binary) expression).getRightOperand() instanceof JPAMemberOperator)
        member = ((JPAMemberOperator) ((Binary) expression).getRightOperand()).getMember().getResourcePath();
    } else if (expression != null && expression instanceof JPAFilterExpression)
      member = ((JPAFilterExpression) expression).getMember();

    if (member != null) {
      for (UriResource r : member.getUriResourceParts()) {
        if (r.getKind() == UriResourceKind.count)
          return r.getKind();
      }
    }
    return null;
  }
}
