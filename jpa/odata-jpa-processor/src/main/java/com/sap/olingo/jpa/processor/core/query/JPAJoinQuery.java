package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.converter.JPAExpandResult.ROOT_RESULT_KEY;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.properties.JPAProcessorAttribute;

public class JPAJoinQuery extends JPAAbstractRootJoinQuery implements JPAQuery {

  public JPAJoinQuery(final OData odata, final JPAODataRequestContextAccess requestContext)
      throws ODataException {
    super(odata, requestContext);
  }

  @Override
  public JPAConvertibleResult execute() throws ODataApplicationException {
    // Pre-process URI parameter, so they can be used at different places
    final var selectionPath = buildSelectionPathList(this.uriResource);
    try (var measurement = debugger.newMeasurement(this, "execute")) {
      final var orderByAttributes = getOrderByAttributes(uriResource.getOrderByOption());

      final var joinTables = createFromClause(orderByAttributes, selectionPath.joinedPersistent(), cq,
          lastInfo);

      cq.multiselect(createSelectClause(joinTables, selectionPath.joinedPersistent(), target, groups))
          .distinct(determineDistinct());

      final var whereClause = createWhere();
      if (whereClause != null) {
        cq.where(whereClause);
      }

      cq.orderBy(createOrderByBuilder().createOrderByList(joinTables, orderByAttributes, lastInfo.getUriInfo()));

      if (orderByAttributes.stream().anyMatch(JPAProcessorAttribute::requiresJoin)) {
        cq.groupBy(createGroupBy(joinTables, root, selectionPath.joinedPersistent(), orderByAttributes));
      }

      final TypedQuery<Tuple> typedQuery = em.createQuery(cq);
      addTopSkip(typedQuery);

      final var result = new HashMap<String, List<Tuple>>(1);
      List<Tuple> intermediateResult;
      try (var resultMeasurement = debugger.newMeasurement(this, "getResultList")) {
        intermediateResult = typedQuery.getResultList();
      }
      result.put(ROOT_RESULT_KEY, intermediateResult);
      return returnResult(selectionPath.joinedRequested(), result);
    } catch (final JPANoSelectionException e) {
      return returnEmptyResult(selectionPath.joinedRequested());
    }
  }

  private JPAOrderByBuilder createOrderByBuilder() throws ODataJPAQueryException {
    if (entitySet.isPresent()) {
      return new JPAOrderByBuilder(entitySet.get(), jpaEntity, target, cb, groups);
    }
    return new JPAOrderByBuilder(jpaEntity, target, cb, groups);
  }

  public List<JPANavigationPropertyInfo> getNavigationInfo() {
    return navigationInfo;
  }

  /**
   * Desired if SELECT DISTINCT shall be generated. This is required e.g. if multiple values for the same claims are
   * present. As a DISTINCT is usually slower the decision algorithm my need to be enhanced in the future
   * @return
   */
  private boolean determineDistinct() {
    return claimsProvider.isPresent();
  }

  private JPAConvertibleResult returnEmptyResult(final Collection<JPAPath> selectionPath) {
    if (lastInfo.getAssociationPath() != null
        && (lastInfo.getAssociationPath().getLeaf() instanceof JPACollectionAttribute)) {
      return new JPACollectionQueryResult(jpaEntity, lastInfo.getAssociationPath(), selectionPath);
    }
    return new JPAExpandQueryResult(jpaEntity, selectionPath);
  }

  private JPAConvertibleResult returnResult(@Nonnull final Collection<JPAPath> selectionPath,
      final HashMap<String, List<Tuple>> result) throws ODataApplicationException {
    final var odataEntityType = determineODataTargetEntityType(requestContext);
    if (lastInfo.getAssociationPath() != null
        && (lastInfo.getAssociationPath().getLeaf() instanceof JPACollectionAttribute)) {
      return new JPACollectionQueryResult(result, null, odataEntityType, lastInfo.getAssociationPath(), selectionPath);
    }
    return new JPAExpandQueryResult(result, Collections.emptyMap(), odataEntityType, selectionPath, Optional.empty());
  }
}
