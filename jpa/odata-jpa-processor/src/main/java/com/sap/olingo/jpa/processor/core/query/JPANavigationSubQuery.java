package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterElementComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAOperationConverter;

public abstract class JPANavigationSubQuery extends JPAAbstractSubQuery {

  protected final List<UriParameter> keyPredicates;

  JPANavigationSubQuery(final OData odata, final JPAServiceDocument sd, final EdmEntityType edmEntityType,
      final EntityManager em, final JPAAbstractQuery parent, final From<?, ?> from,
      final JPAAssociationPath association, final Optional<JPAODataClaimProvider> claimsProvider,
      final List<UriParameter> keyPredicates) throws ODataApplicationException {
    super(odata, sd, edmEntityType, em, parent, from, association, claimsProvider);
    this.keyPredicates = keyPredicates;
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());
    this.locale = parent.getLocale();
    createRoots(association);
  }

  final void buildExpression(final VisitableExpression expression, final List<String> groups)
      throws ODataApplicationException {
    this.filterComplier = new JPAFilterElementComplier(odata, sd, em, jpaEntity, new JPAOperationConverter(cb,
        getContext().getOperationConverter()), null, this, expression, null, groups);
    createDescriptionJoin();
  }

  void createDescriptionJoin() throws ODataApplicationException {
    final HashMap<String, From<?, ?>> joinTables = new HashMap<>();
    generateDescriptionJoin(joinTables, determineAllDescriptionPath(), getRoot());
  }

  private Set<JPAPath> determineAllDescriptionPath() throws ODataApplicationException {
    final Set<JPAPath> allPath = new HashSet<>();
    if (filterComplier != null) {
      for (final JPAPath path : filterComplier.getMember()) {
        if (path.getLeaf() instanceof JPADescriptionAttribute)
          allPath.add(path);
      }
    }
    return allPath;
  }

  protected void createGroupBy(final Subquery<?> subQuery, final From<?, ?> from,
      final List<JPAOnConditionItem> conditionItems) {

    final List<Expression<?>> groupByList = new ArrayList<>();
    for (final JPAOnConditionItem onCondition : conditionItems) {
      Path<?> subPath = from;
      for (final JPAElement jpaPathElement : onCondition.getRightPath().getPath())
        subPath = subPath.get(jpaPathElement.getInternalName());
      groupByList.add(subPath);
    }
    subQuery.groupBy(groupByList);
  }

  protected List<JPAOnConditionItem> determineJoinColumns() throws ODataJPAQueryException {

    try {
      final List<JPAOnConditionItem> conditionItems = association.getJoinColumnsList();
      if (conditionItems.isEmpty())
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_JOIN_NOT_DEFINED,
            HttpStatusCode.INTERNAL_SERVER_ERROR, association.getTargetType().getExternalName(), association
                .getSourceType().getExternalName());
      return conditionItems;

    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_NAVI_PROPERTY_UNKNOWN,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e, association.getAlias());
    }
  }

  @Override
  public From<?, ?> getRoot() {
    assert queryRoot != null;
    return queryRoot;
  }

}