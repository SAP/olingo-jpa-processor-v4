package com.sap.olingo.jpa.processor.core.query;

import static java.util.Collections.emptyList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaQuery;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

/**
 * Requires Processor Query
 *
 * @author Oliver Grande
 * @since 1.0.1
 * 25.11.2020
 */
public final class JPAExpandSubCountQuery extends JPAAbstractExpandSubQuery implements JPAExpandCountQuery {

  public JPAExpandSubCountQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPAEntityType et, final JPAAssociationPath association, final List<JPANavigationPropertyInfo> hops)
      throws ODataException {

    super(odata, requestContext, et, association, copyHops(hops));
  }

  public JPAExpandSubCountQuery(final OData odata, final JPAExpandItemInfo item,
      final JPAODataRequestContextAccess requestContext) throws ODataException {

    super(odata, requestContext, item);
  }

  @Override
  public final Map<String, Long> count() throws ODataApplicationException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "count")) {
      final ProcessorCriteriaQuery<Tuple> tupleQuery = (ProcessorCriteriaQuery<Tuple>) cb.createTupleQuery();
      addFilterCompiler(lastInfo);
      final LinkedList<JPAAbstractQuery> hops = buildSubQueries(this);
      final Subquery<Object> subQuery = linkSubQueries(hops);

      createFromClause(emptyList(), emptyList(), tupleQuery, lastInfo);
      final List<Selection<?>> selectionPath = createSelectClause();
      tupleQuery.multiselect(addCount(selectionPath));
      tupleQuery.where(createWhere(subQuery, lastInfo));
      tupleQuery.groupBy(buildExpandCountGroupBy(root));
      final TypedQuery<Tuple> query = em.createQuery(tupleQuery);
      final List<Tuple> intermediateResult = query.getResultList();
      return convertCountResult(intermediateResult);

    } catch (final ODataException | JPANoSelectionException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private List<Selection<?>> createSelectClause() throws ODataApplicationException {
    if (association.hasJoinTable()) {
      return addSelectJoinTable(List.of());
    } else
      return buildExpandJoinPath(root);
  }

}
