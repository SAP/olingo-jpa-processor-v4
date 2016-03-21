package org.apache.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.filter.JPAFilterElementComplier;
import org.apache.olingo.jpa.processor.core.filter.JPAOperationConverter;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

public class JPANavigationFilterQuery extends JPANavigationQuery {

  private final JPAFilterElementComplier filterComplier;

  public JPANavigationFilterQuery(OData odata, ServicDocument sd, UriResource uriResourceItem, JPAAbstractQuery parent,
      EntityManager em, JPAAssociationPath association) throws ODataApplicationException {
    super(sd, uriResourceItem, parent, em, association);
    this.filterComplier = null;
  }

  public JPANavigationFilterQuery(OData odata, ServicDocument sd, UriResource uriResourceItem, JPAAbstractQuery parent,
      EntityManager em, JPAAssociationPath association, VisitableExpression expression)
      throws ODataApplicationException {
    super(sd, uriResourceItem, parent, em, association);
    this.filterComplier = new JPAFilterElementComplier(odata, sd, em, jpaEntity, new JPAOperationConverter(cb), null,
        this, expression);
  }

  @Override
  protected Expression<Boolean> createWhereByAssociation(From<?, ?> parentFrom, Root<?> subRoot,
      List<JPAOnConditionItem> conditionItems) throws ODataApplicationException {
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
    if (filterComplier != null) try {
      whereCondition = cb.and(whereCondition, filterComplier.compile());
    } catch (ExpressionVisitException e) {
      throw new ODataApplicationException("Expression error", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          Locale.ENGLISH, e);
    }
    return whereCondition;
  }

}
