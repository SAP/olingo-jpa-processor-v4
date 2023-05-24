package com.sap.olingo.jpa.processor.core.query;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

/**
 * A query to retrieve the expand entities.
 * <p>
 * According to
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398162"
 * >OData Version 4.0 Part 2 - 5.1.2 System Query Option $expand</a> the following query options are allowed:
 * <ul>
 * <li>expandCountOption = <b>filter</b>/ search
 * <p>
 * <li>expandRefOption = expandCountOption/ <b>orderby</b> / <b>skip</b> / <b>top</b> / inlinecount
 * <li>expandOption = expandRefOption/ <b>select</b>/ <b>expand</b> / <b>levels</b>
 * <p>
 * </ul>
 * As of now only the bold once are supported
 * <p>
 * @author Oliver Grande
 *
 */
public final class JPAExpandJoinQuery extends JPAAbstractExpandQuery {

  private final Optional<JPAKeyBoundary> keyBoundary;
  private JPAQueryCreationResult tupleQuery;

  public JPAExpandJoinQuery(final OData odata, final JPAInlineItemInfo item,
      final JPAODataRequestContextAccess requestContext, final Optional<JPAKeyBoundary> keyBoundary)
      throws ODataException {

    super(odata, requestContext, item);
    this.keyBoundary = keyBoundary;
  }

  public JPAExpandJoinQuery(final OData odata, final JPAAssociationPath association, final JPAEntityType entityType,
      final JPAODataRequestContextAccess requestContext) throws ODataException {

    super(odata, entityType, requestContext, association);
    this.keyBoundary = Optional.empty();
  }

  /**
   * Process a expand query, which may contains a $skip and/or a $top option.
   * <p>
   * This is a tricky problem, as it can not be done easily with SQL. It could be that a database offers special
   * solutions. There is an worth reading blog regards this topic:
   * <a href="http://www.xaprb.com/blog/2006/12/07/how-to-select-the-firstleastmax-row-per-group-in-sql/">How to select
   * the first/least/max row per group in SQL</a>. Often databases offer the option to use <code>ROW_NUMBER</code>
   * together with <code>OVER ... ORDER BY</code> see e.g. <a
   * href="http://www.sqltutorial.org/sql-window-functions/sql-row_number/">SQL ROW_NUMBER</a>.
   * Unfortunately this is not supported by JPA.
   * @return query result
   * @throws ODataApplicationException
   */
  @Override
  public JPAExpandQueryResult execute() throws ODataApplicationException {

    try (JPARuntimeMeasurement meassument = debugger.newMeasurement(this, "execute")) {
      tupleQuery = createTupleQuery();
      List<Tuple> intermediateResult;
      try (JPARuntimeMeasurement resultMeassument = debugger.newMeasurement(tupleQuery, "getResultList")) {
        intermediateResult = tupleQuery.getQuery().getResultList();
      }
      // Simplest solution for the top/skip problem. Read all and throw away, what is not requested
      final Map<String, List<Tuple>> result = convertResult(intermediateResult, association, determineSkip(),
          determineTop());
      return new JPAExpandQueryResult(result, count(), jpaEntity, tupleQuery.getSelection().joinedRequested());
    } catch (final JPANoSelectionException e) {
      return new JPAExpandQueryResult(emptyMap(), emptyMap(), this.jpaEntity, emptyList());
    }
  }

  private long determineTop() {
    if (uriResource.getTopOption() != null)
      return uriResource.getTopOption().getValue();
    return Long.MAX_VALUE;
  }

  private long determineSkip() {
    if (uriResource.getSkipOption() != null)
      return uriResource.getSkipOption().getValue();
    return 0;
  }

  /**
   * Returns the generated SQL string after the query has been executed, otherwise an empty string.<br>
   * As of now this is only supported for EclipseLink
   * @return
   * @throws ODataJPAQueryException
   */
  String getSQLString() throws ODataJPAQueryException {
    if (tupleQuery != null && tupleQuery.getQuery().getClass().getCanonicalName().equals(
        "org.eclipse.persistence.internal.jpa.EJBQueryImpl")) {

      try {
        final Object dbQuery = tupleQuery.getQuery().getClass().getMethod("getDatabaseQuery")
            .invoke(tupleQuery.getQuery());
        return (String) dbQuery.getClass().getMethod("getSQLString").invoke(dbQuery);
      } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    } else {
      return "";
    }
  }

  @Override
  protected List<Selection<?>> createSelectClause(final Map<String, From<?, ?>> joinTables,
      final Collection<JPAPath> jpaPathList,
      final From<?, ?> target, final List<String> groups) throws ODataApplicationException {

    final List<Selection<?>> selections = new ArrayList<>(super.createSelectClause(joinTables, jpaPathList, target,
        groups));
    if (association.hasJoinTable()) {
      // For associations with JoinTable the join columns, linking columns to the parent, need to be added
      createAdditionSelectionForJoinTable(selections);
    }
    return selections;
  }

  private void createAdditionSelectionForJoinTable(final List<Selection<?>> selections) throws ODataJPAQueryException {
    final From<?, ?> parent = determineParentFrom(); // e.g. JoinSource
    try {
      for (final JPAPath p : association.getLeftColumnsList()) {
        final Path<?> selection = ExpressionUtil.convertToCriteriaPath(parent, p.getPath());
        // If source and target of an association use the same name for their key we get conflicts with the alias.
        // Therefore it is necessary to unify them.
        selection.alias(association.getAlias() + ALIAS_SEPARATOR + p.getAlias());
        selections.add(selection);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Splits up a expand results, so it is returned as a map that uses a concatenation of the field values know by the
   * parent.
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
        throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
      }

      if (!actualKey.equals(joinKey)) {
        subResult = new ArrayList<>();
        convertedResult.put(actualKey, subResult);
        joinKey = actualKey;
        skipped = taken = 0;
      }
      if (subResult != null && skipped >= skip && taken < top) {
        taken += 1;
        subResult.add(row);
      } else {
        skipped += 1;
      }
    }
    return convertedResult;
  }

  @Override
  final Map<String, Long> count() throws ODataApplicationException {

    try (JPARuntimeMeasurement meassument = debugger.newMeasurement(this, "count")) {
      final JPAExpandJoinCountQuery countQuery = new JPAExpandJoinCountQuery(odata, requestContext, jpaEntity,
          association, navigationInfo, keyBoundary);
      return countQuery.count();
    } catch (final ODataException e) {
      throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
    }
  }

  private JPAQueryCreationResult createTupleQuery() throws ODataApplicationException, JPANoSelectionException {

    try (JPARuntimeMeasurement meassument = debugger.newMeasurement(this, "createTupleQuery")) {
      final List<JPAAssociationPath> orderByAttributes = extractOrderByNaviAttributes(uriResource.getOrderByOption());
      final SelectionPathInfo<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
      final Map<String, From<?, ?>> joinTables = createFromClause(orderByAttributes, selectionPath.joinedPersistent(),
          cq, lastInfo);
      // TODO handle Join Column is ignored
      cq.multiselect(createSelectClause(joinTables, selectionPath.joinedPersistent(), target, groups));
      if (orderByAttributes.isEmpty())
        cq.distinct(true);
      final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere();
      if (whereClause != null)
        cq.where(whereClause);

      final List<Order> orderBy = createOrderByJoinCondition(association);
      orderBy.addAll(new JPAOrderByBuilder(jpaEntity, target, cb, groups).createOrderByList(joinTables, uriResource,
          page));

      cq.orderBy(orderBy);
      if (!orderByAttributes.isEmpty())
        cq.groupBy(createGroupBy(joinTables, target, selectionPath.joinedPersistent()));

      final TypedQuery<Tuple> query = em.createQuery(cq);

      return new JPAQueryCreationResult(query, selectionPath);
    }
  }

  private Expression<Boolean> createWhere() throws ODataApplicationException {

    try (JPARuntimeMeasurement meassument = debugger.newMeasurement(this, "createWhere")) {
      javax.persistence.criteria.Expression<Boolean> whereCondition = null;
      // Given keys: Organizations('1')/Roles(...)
      whereCondition = createKeyWhere(navigationInfo);
      whereCondition = addWhereClause(whereCondition, createBoundary(navigationInfo, keyBoundary));
      whereCondition = addWhereClause(whereCondition, createExpandWhere());
      whereCondition = addWhereClause(whereCondition, createProtectionWhere(claimsProvider));
      return whereCondition;
    }
  }

  private javax.persistence.criteria.Expression<Boolean> createExpandWhere() throws ODataApplicationException {

    javax.persistence.criteria.Expression<Boolean> whereCondition = null;
    for (final JPANavigationPropertyInfo info : this.navigationInfo) {
      if (info.getFilterCompiler() != null) {
        try {
          whereCondition = addWhereClause(whereCondition, info.getFilterCompiler().compile());
        } catch (final ExpressionVisitException e) {
          throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
              HttpStatusCode.BAD_REQUEST, e);
        }
      }
    }
    return whereCondition;
  }

  @Override
  protected JPAAssociationPath getAssociation(final JPAInlineItemInfo item) {
    return item.getExpandAssociation();
  }
}
