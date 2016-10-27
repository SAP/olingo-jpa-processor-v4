package org.apache.olingo.jpa.processor.core.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServiceDocument;
import org.apache.olingo.jpa.processor.core.filter.JPAFilterElementComplier;
import org.apache.olingo.jpa.processor.core.filter.JPAOperationConverter;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;

public class JPANavigationInheritFilterQuery extends JPANavigationQuery {

  private final JPANavigationProptertyInfo item;
  private final JPAFilterElementComplier filterComplier;

  public JPANavigationInheritFilterQuery(OData odata, ServiceDocument sd, JPAAbstractQuery parent, EntityManager em,
      JPANavigationProptertyInfo naviInfo) throws ODataApplicationException {

    super(sd, naviInfo.getUriResiource(), parent, em, naviInfo.getAssociationPath());
    this.item = naviInfo;
    this.filterComplier = new JPAFilterElementComplier(odata, sd, em, jpaEntity, new JPAOperationConverter(cb,
        getContext().getOperationConverter()), null, this, naviInfo.getExpression());
  }

  @Override
  protected Expression<Boolean> createWhereByAssociation(From<?, ?> parentFrom, From<?, ?> subRoot,
      List<JPAOnConditionItem> conditionItems) throws ODataApplicationException {

    Expression<Boolean> whereCondition = super.createWhereByAssociation(parentFrom, subRoot, conditionItems);
    whereCondition = createWhereByKey(getRoot(), whereCondition, item.getKeyPredicates());
    return applyAdditionalFilter(filterComplier, whereCondition);
  }

}
