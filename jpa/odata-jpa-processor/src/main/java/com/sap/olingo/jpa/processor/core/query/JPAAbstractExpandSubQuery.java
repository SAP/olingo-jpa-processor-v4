package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_JOIN_TABLE_TYPE_MISSING;
import static java.util.Collections.singletonList;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.properties.JPAProcessorAttribute;

abstract class JPAAbstractExpandSubQuery extends JPAAbstractExpandQuery {

  JPAAbstractExpandSubQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPAInlineItemInfo item) throws ODataException {
    super(odata, requestContext, item);
    ((ProcessorCriteriaBuilder) cb).resetParameterBuffer();
  }

  JPAAbstractExpandSubQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPAEntityType et, final JPAAssociationPath association, final List<JPANavigationPropertyInfo> hops)
      throws ODataException {
    super(odata, requestContext, et, association, hops);
  }

  @Override
  protected Map<String, From<?, ?>> createFromClause(final List<JPAProcessorAttribute> orderByTarget,
      final Collection<JPAPath> selectionPath, final CriteriaQuery<?> query, final JPANavigationPropertyInfo lastInfo)
      throws ODataApplicationException, JPANoSelectionException {

    final Map<String, From<?, ?>> joinTables = new HashMap<>();
    debugger.trace(this, "Create FROM clause for %s", query.toString());
    createFromClauseRoot(query, joinTables, lastInfo);
    target = root;
    createFromClauseJoinTable(joinTables, query);
    lastInfo.setFromClause(target);
    createFromClauseDescriptionFields(selectionPath, joinTables, target, singletonList(lastInfo));
    return joinTables;
  }

  LinkedList<JPAAbstractQuery> buildSubQueries(final JPAAbstractQuery query) throws ODataException {
    final LinkedList<JPAAbstractQuery> hops = new LinkedList<>();
    hops.push(query);
    for (int i = navigationInfo.size() - 2; i >= 0; i--) {
      final JPANavigationPropertyInfo hop = navigationInfo.get(i);
      if (hop.getUriInfo() != null) {
        final var associationIndex = determineAssociationPathIndex(navigationInfo, i);
        final JPAAbstractQuery parent = hops.getLast();
        final JPAAssociationPath childAssociation = associationIndex >= 0
            ? navigationInfo.get(associationIndex).getAssociationPath()
            : null;
        hops.push(new JPAExpandFilterQuery(odata, requestContext, hop, parent, childAssociation));
        debugger.trace(this, "Sub query created: %s for %s", hops.getFirst().getQuery(), hops.getFirst().jpaEntity);
      }
    }
    return hops;
  }

  Subquery<Object> linkSubQueries(final LinkedList<JPAAbstractQuery> hops) throws ODataApplicationException {
    Subquery<Object> subQuery = null;
    while (!hops.isEmpty() && hops.getFirst() instanceof JPAAbstractSubQuery) {
      final JPAAbstractSubQuery hop = (JPAAbstractSubQuery) hops.pop();
      subQuery = hop.getSubQuery(subQuery, null, Collections.emptyList());
    }
    return subQuery;
  }

  void createFromClauseJoinTable(final Map<String, From<?, ?>> joinTables, final CriteriaQuery<?> query)
      throws ODataJPAQueryException {
    if (association.hasJoinTable()) {
      final JPAJoinTable joinTable = association.getJoinTable();
      final JPAEntityType joinTableEt = Optional.ofNullable(joinTable.getEntityType())
          .orElseThrow(() -> new ODataJPAQueryException(QUERY_PREPARATION_JOIN_TABLE_TYPE_MISSING,
              INTERNAL_SERVER_ERROR, joinTable.getTableName()));
      debugger.trace(this, "Join table found: %s, join will be created", joinTableEt.toString());
      root = query.from(joinTableEt.getTypeClass());
      root.alias(association.getAlias());
      joinTables.put(association.getAlias(), root);
    }
  }

  List<Selection<?>> addSelectJoinTable(final List<Selection<?>> selections) throws ODataJPAQueryException {
    if (association.hasJoinTable()) {
      try {
        final List<Selection<?>> additionalSelections = new ArrayList<>(selections);
        final JPAJoinTable joinTable = association.getJoinTable();
        debugger.trace(this, "Creating SELECT snipped for join table %s with join conditions %s", joinTable.toString(),
            joinTable.getJoinColumns());
        for (final JPAOnConditionItem jc : association.getJoinTable().getJoinColumns()) {
          final Path<?> path = root.get(jc.getRightPath().getLeaf().getInternalName());
          path.alias(association.getAlias() + ALIAS_SEPARATOR + jc.getLeftPath().getAlias());
          additionalSelections.add(path);
        }
        return additionalSelections;
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
      }
    }
    return selections;
  }

  Expression<Boolean> createExpandWhere(final JPANavigationPropertyInfo navigationInfo)
      throws ODataApplicationException {

    try {
      return navigationInfo.getFilterCompiler().compile();
    } catch (final ExpressionVisitException e) {
      throw new ODataJPAQueryException(QUERY_PREPARATION_FILTER_ERROR, HttpStatusCode.BAD_REQUEST, e);
    }
  }

  Expression<Boolean> createWhere(final Subquery<?> subQuery, final JPANavigationPropertyInfo navigationInfo)
      throws ODataApplicationException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "createWhere")) {
      jakarta.persistence.criteria.Expression<Boolean> whereCondition = null;
      // Given keys: Organizations('1')/Roles(...)
      whereCondition = createWhereByKey(navigationInfo);
      whereCondition = addWhereClause(whereCondition, createWhereTableJoin(root, target, association, true));
      whereCondition = addWhereClause(whereCondition, createWhereKeyIn(this.association, root, subQuery));
      whereCondition = addWhereClause(whereCondition, createExpandWhere(navigationInfo));
      whereCondition = addWhereClause(whereCondition, createWhereEnhancement());
      whereCondition = addWhereClause(whereCondition, createProtectionWhereForEntityType(claimsProvider, jpaEntity,
          target));
      return whereCondition;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  private void createFromClauseRoot(final CriteriaQuery<?> query, final Map<String, From<?, ?>> joinTables,
      final JPANavigationPropertyInfo lastInfo) throws ODataJPAQueryException {
    try {
      final JPAEntityType sourceEt = lastInfo.getEntityType();
      this.root = query.from(sourceEt.getTypeClass());
      joinTables.put(sourceEt.getExternalFQN().getFullQualifiedNameAsString(), root);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
    }
  }

  private int determineAssociationPathIndex(final List<JPANavigationPropertyInfo> navigationInfo, final int index) {
    // look ahead
    var readIndex = index - 1;
    while (readIndex >= 0
        && navigationInfo.get(readIndex).getUriInfo() == null)
      readIndex--;
    return readIndex;
  }

}