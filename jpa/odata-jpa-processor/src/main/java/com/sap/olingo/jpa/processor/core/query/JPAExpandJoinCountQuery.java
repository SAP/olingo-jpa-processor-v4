package com.sap.olingo.jpa.processor.core.query;

import static java.util.Collections.emptyMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;

/**
 * Requires Processor Query
 *
 * @author Oliver Grande
 * @since 1.0.1
 * 25.11.2020
 */
public final class JPAExpandJoinCountQuery extends JPAAbstractExpandJoinQuery implements JPAExpandCountQuery {

  public JPAExpandJoinCountQuery(final OData odata,
      final JPAODataRequestContextAccess requestContext, final JPAEntityType et,
      final JPAAssociationPath association, final List<JPANavigationPropertyInfo> hops,
      final Optional<JPAKeyBoundary> keyBoundary)
      throws ODataException {

    super(odata, requestContext, et, association, copyHops(hops), keyBoundary);

  }

  public JPAExpandJoinCountQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPAExpandItemInfo item, final Optional<JPAKeyBoundary> keyBoundary) throws ODataException {

    super(odata, requestContext, item, keyBoundary);
  }

  @Override
  public final Map<String, Long> count() throws ODataApplicationException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "count")) {
      if (countRequested(lastInfo)) {
        final CriteriaQuery<Tuple> countQuery = cb.createTupleQuery();
        createCountFrom(countQuery);
        final List<Selection<?>> selectionPath = createSelectClause(target);
        countQuery.multiselect(addCount(selectionPath));
        final jakarta.persistence.criteria.Expression<Boolean> whereClause = createWhere();
        if (whereClause != null)
          countQuery.where(whereClause);
        countQuery.groupBy(extractPath(selectionPath));
        final TypedQuery<Tuple> query = em.createQuery(countQuery);
        final List<Tuple> intermediateResult = query.getResultList();
        return convertCountResult(intermediateResult);
      }
      return emptyMap();
    }
  }

  private List<Expression<?>> extractPath(final List<Selection<?>> selectionPath) {

    final List<Expression<?>> result = new ArrayList<>(selectionPath.size());
    for (final var selection : selectionPath) {
      if (selection instanceof final Path<?> path) {
        result.add(path);
      }
    }
    return result;
  }

  private void createCountFrom(final CriteriaQuery<Tuple> countQuery) throws ODataApplicationException {
    final HashMap<String, From<?, ?>> joinTables = new HashMap<>();
    // 1. Create navigation joins
    createFromClauseRoot(countQuery, joinTables);
    target = root;
    createFromClauseNavigationJoins(joinTables);
    lastInfo.setFromClause(target);
  }

  private List<Selection<?>> createSelectClause(final From<?, ?> from) throws ODataApplicationException {
    if (association.hasJoinTable()) {
      return createAdditionSelectionForJoinTable(association);
    } else {
      return buildExpandJoinPath(from);
    }
  }
}
