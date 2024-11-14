package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_INVALID_SELECTION_PATH;
import static java.util.stream.Collectors.joining;
import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public class JPACollectionJoinQuery extends JPAAbstractJoinQuery implements JPAQuery {
  private final JPAAssociationPath association;
  private final Optional<JPAKeyBoundary> keyBoundary;

  public JPACollectionJoinQuery(final OData odata, final JPACollectionItemInfo item,
      final JPAODataRequestContextAccess requestContext, final Optional<JPAKeyBoundary> keyBoundary)
      throws ODataException {

    super(odata, item.getEntityType(), requestContext, new ArrayList<>(item.getHops().subList(0,
        item.getHops().size() - 1)));
    this.association = item.getExpandAssociation();
    this.keyBoundary = keyBoundary;
    if (this.cb instanceof final ProcessorCriteriaBuilder processorCb)
      processorCb.resetParameterBuffer();
  }

  @Override
  public JPACollectionQueryResult execute() throws ODataApplicationException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "executeStandardQuery")) {
      final SelectionPathInfo<JPAPath> requestedSelection = buildSelectionPathList(this.uriResource);
      final TypedQuery<Tuple> tupleQuery = createTupleQuery(requestedSelection);
      List<Tuple> intermediateResult;
      try (JPARuntimeMeasurement resultMeasurement = debugger.newMeasurement(this, "getResultList")) {
        intermediateResult = tupleQuery.getResultList();
      }
      final Map<String, List<Tuple>> result = convertResult(intermediateResult, association, 0, Long.MAX_VALUE);
      return new JPACollectionQueryResult(result, new HashMap<>(1), jpaEntity, this.association,
          requestedSelection.joinedRequested());
    } catch (final JPANoSelectionException e) {
      return new JPACollectionQueryResult(this.jpaEntity, association, Collections.emptyList());
    }
  }

  @Override
  protected SelectionPathInfo<JPAPath> buildSelectionPathList(final UriInfoResource uriResource)
      throws ODataApplicationException {
    final SelectionPathInfo<JPAPath> jpaPathList = new SelectionPathInfo<>();
    final String pathPrefix = Utility.determinePropertyNavigationPrefix(uriResource.getUriResourceParts());
    final SelectOption select = uriResource.getSelectOption();
    // Following situations have to be handled:
    // - .../Organizations --> Select all collection attributes
    // - .../Organizations('1')/Comment --> Select navigation target
    // - .../Et/St/St --> Select navigation target --> Select navigation target via complex properties
    // - .../Organizations?$select=ID,Comment --> Select collection attributes given by select clause
    // - .../Persons('99')/InhouseAddress?$select=Building --> Select attributes of complex collection given by select
    // clause
    try {

      if (SelectOptionUtil.selectAll(select))
        // If the collection is part of a navigation take all the attributes
        expandPath(jpaEntity, jpaPathList, this.association.getAlias(), true);
      else {
        final var st = jpaEntity;
        for (final SelectItem sItem : select.getSelectItems()) {
          final JPAPath selectItemPath = selectItemAsPath(st, pathPrefix, sItem);
          if (pathContainsCollection(selectItemPath)) {
            if (selectItemPath.getLeaf().isComplex()) {
              expandPath(st, jpaPathList, selectItemPath.getAlias(), true);
            } else {
              jpaPathList.getODataSelections().add(selectItemPath);
            }
          } else if (selectItemPath.getLeaf().isComplex()) {
            expandPath(st, jpaPathList, pathPrefix.isEmpty() ? this.association.getAlias() : pathPrefix
                + JPAPath.PATH_SEPARATOR + this.association.getAlias(), true);
          }
        }
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(QUERY_PREPARATION_INVALID_SELECTION_PATH, BAD_REQUEST);
    }
    return jpaPathList;
  }

  private JPAPath selectItemAsPath(final JPAStructuredType st, final String pathPrefix, final SelectItem selectionItem)
      throws ODataJPAModelException, ODataJPAQueryException {

    String pathItem = selectionItem.getResourcePath().getUriResourceParts().stream()
        .map(path -> (path.getSegmentValue()))
        .collect(Collectors.joining(JPAPath.PATH_SEPARATOR));
    pathItem = pathPrefix == null || pathPrefix.isEmpty() ? pathItem : pathPrefix + JPAPath.PATH_SEPARATOR
        + pathItem;
    final JPAPath selectItemPath = st.getPath(pathItem);
    if (selectItemPath == null)
      throw new ODataJPAQueryException(QUERY_PREPARATION_INVALID_SELECTION_PATH, BAD_REQUEST);
    return selectItemPath;
  }

  @Override
  protected List<Selection<?>> createSelectClause(final Map<String, From<?, ?>> joinTables, // NOSONAR
      final Collection<JPAPath> jpaPathList, final From<?, ?> target, final List<String> groups)
      throws ODataApplicationException { // NOSONAR Allow
    // subclasses to throw an exception
    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "createSelectClause")) {
      final List<Selection<?>> selections = new ArrayList<>();
      // Based on an error in Eclipse Link first the join columns have to be selected. Otherwise the alias is assigned
      // to
      // the wrong column. E.g. if Organization Comment shall be read Eclipse Link automatically selects also the Order
      // column and if the join column is added later the select clause would look as follows: SELECT t0."Text,
      // t0."Order", t1,"ID". Eclipse Link will then return the value of the Order column for the alias of the ID
      // column.
      createAdditionSelectionForJoinTable(selections);

      // Build select clause
      for (final JPAPath jpaPath : jpaPathList) {
        if (jpaPath.isPartOfGroups(groups)) {
          final Path<?> path = ExpressionUtility.convertToCriteriaPath(joinTables, target, jpaPath);
          path.alias(jpaPath.getAlias());
          selections.add(path);
        }
      }
      return selections;
    }
  }

  /**
   * Splits up a expand results, so it is returned as a map that uses a concatenation of the field values know by the
   * parent.
   *
   * @param intermediateResult
   * @param associationPath
   * @param skip
   * @param top
   * @return
   * @throws ODataApplicationException
   */
  Map<String, List<Tuple>> convertResult(final List<Tuple> intermediateResult, final JPAAssociationPath associationPath,
      final long skip, final long top) throws ODataApplicationException {
    String joinKey = "";
    long skipped = 0;
    long taken = 0;

    List<Tuple> subResult = null;
    final Map<String, List<Tuple>> convertedResult = new HashMap<>();
    for (final Tuple row : intermediateResult) {
      String actualKey;
      try {
        actualKey = buildConcatenatedKey(row, associationPath);
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, BAD_REQUEST);
      }

      if (!actualKey.equals(joinKey)) {
        subResult = new ArrayList<>();
        convertedResult.put(actualKey, subResult);
        joinKey = actualKey;
        skipped = taken = 0;
      }
      if (skipped >= skip && taken < top && subResult != null) {
        taken += 1;
        subResult.add(row);
      } else {
        skipped += 1;
      }
    }
    return convertedResult;
  }

  private String buildConcatenatedKey(final Tuple row, final JPAAssociationPath associationPath)
      throws ODataJPAModelException {

    if (!associationPath.hasJoinTable()) {
      final List<JPAPath> joinColumns = associationPath.getRightColumnsList();
      return joinColumns.stream()
          .map(column -> (row.get(column.getAlias())).toString())
          .collect(joining(JPAPath.PATH_SEPARATOR));
    } else {
      final List<JPAPath> joinColumns = associationPath.getLeftColumnsList();
      return joinColumns.stream()
          .map(column -> (row.get(association.getAlias() + ALIAS_SEPARATOR + column.getAlias())).toString())
          .collect(joining(JPAPath.PATH_SEPARATOR));
    }
  }

  private List<Order> createOrderByJoinCondition(final JPAAssociationPath associationPath)
      throws ODataApplicationException {
    final List<Order> orders = new ArrayList<>();

    try {
      final List<JPAPath> joinColumns = associationPath.hasJoinTable()
          ? associationPath.getLeftColumnsList() : associationPath.getRightColumnsList();
      final From<?, ?> from = associationPath.hasJoinTable()
          ? determineParentFrom() : target;

      for (final JPAPath j : joinColumns) {
        Path<?> jpaProperty = from;
        for (final JPAElement pathElement : j.getPath()) {
          jpaProperty = jpaProperty.get(pathElement.getInternalName());
        }
        orders.add(cb.asc(jpaProperty));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, BAD_REQUEST);
    }
    return orders;
  }

  private TypedQuery<Tuple> createTupleQuery(final SelectionPathInfo<JPAPath> selectionPath)
      throws ODataApplicationException,
      JPANoSelectionException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "createTupleQuery")) {
      final Map<String, From<?, ?>> joinTables = createFromClause(new ArrayList<>(1),
          selectionPath.joinedPersistent(), cq, lastInfo);
      // TODO handle Join Column is ignored
      cq.multiselect(createSelectClause(joinTables, selectionPath.joinedPersistent(), target, groups));
      cq.distinct(true);
      final jakarta.persistence.criteria.Expression<Boolean> whereClause = createWhere();
      if (whereClause != null)
        cq.where(whereClause);

      final List<Order> orderBy = createOrderByJoinCondition(association);
      orderBy.addAll(new JPAOrderByBuilder(jpaEntity, target, cb, groups).createOrderByList(joinTables));
      cq.orderBy(orderBy);

      return em.createQuery(cq);
    }
  }

  Expression<Boolean> createWhere() throws ODataApplicationException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "createWhere")) {
      jakarta.persistence.criteria.Expression<Boolean> whereCondition = null;
      // Given keys: Organizations('1')/Roles(...)
      whereCondition = createKeyWhere(navigationInfo);
      whereCondition = addWhereClause(whereCondition, createBoundary(navigationInfo, keyBoundary));
      whereCondition = addWhereClause(whereCondition, createCollectionWhere());
      whereCondition = addWhereClause(whereCondition, createProtectionWhere(claimsProvider));
      return whereCondition;
    }
  }

  private jakarta.persistence.criteria.Expression<Boolean> createCollectionWhere()
      throws ODataApplicationException {

    jakarta.persistence.criteria.Expression<Boolean> whereCondition = null;
    for (final JPANavigationPropertyInfo info : this.navigationInfo) {
      if (info.getFilterCompiler() != null) {
        try {
          whereCondition = addWhereClause(whereCondition, info.getFilterCompiler().compile());
        } catch (final ExpressionVisitException e) {
          throw new ODataJPAQueryException(QUERY_PREPARATION_FILTER_ERROR, BAD_REQUEST, e);
        }
      }
    }
    return whereCondition;
  }

  private From<?, ?> determineParentFrom() throws ODataJPAQueryException {
    for (final JPANavigationPropertyInfo item : this.navigationInfo) {
      if (item.getAssociationPath() == association)
        return item.getFromClause();
    }
    throw new ODataJPAQueryException(QUERY_PREPARATION_FILTER_ERROR, BAD_REQUEST);
  }

  private void createAdditionSelectionForJoinTable(final List<Selection<?>> selections) throws ODataJPAQueryException {
    final From<?, ?> parent = determineParentFrom(); // e.g. JoinSource
    try {
      for (final JPAPath p : association.getLeftColumnsList()) {
        final Path<?> selection = ExpressionUtility.convertToCriteriaPath(parent, p.getPath());
        // If source and target of an association use the same name for their key we get conflicts with the alias.
        // Therefore it is necessary to unify them.
        selection.alias(association.getAlias() + ALIAS_SEPARATOR + p.getAlias());
        selections.add(selection);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
    }
  }

  private boolean pathContainsCollection(final JPAPath p) {
    for (final JPAElement pathElement : p.getPath()) {
      if (pathElement instanceof final JPAAttribute attribute && attribute.isCollection()) {
        return true;
      }
    }
    return false;
  }
}
