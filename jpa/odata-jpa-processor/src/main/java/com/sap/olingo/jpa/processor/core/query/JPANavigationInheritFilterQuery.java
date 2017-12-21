package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;

public final class JPANavigationInheritFilterQuery extends JPANavigationQuery {

  private final JPANavigationProptertyInfo item;

  public JPANavigationInheritFilterQuery(OData odata, JPAServiceDocument sd, JPAAbstractQuery parent, EntityManager em,
      JPANavigationProptertyInfo naviInfo) throws ODataApplicationException {

    super(odata, sd, naviInfo.getUriResiource(), parent, em, naviInfo.getAssociationPath());
    this.item = naviInfo;
  }

  @Override
  protected Expression<Boolean> createWhereByAssociation(From<?, ?> parentFrom, From<?, ?> subRoot,
      List<JPAOnConditionItem> conditionItems) throws ODataApplicationException {

    Expression<Boolean> whereCondition = super.createWhereByAssociation(parentFrom, subRoot, conditionItems);
    whereCondition = createWhereByKey(getRoot(), whereCondition, item.getKeyPredicates());
    return item.getExpression() == null ? whereCondition : cb.and(item.getExpression(), whereCondition);
  }

}
