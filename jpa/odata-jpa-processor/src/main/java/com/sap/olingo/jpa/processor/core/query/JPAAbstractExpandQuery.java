package com.sap.olingo.jpa.processor.core.query;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.CountOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSkipTokenProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public abstract class JPAAbstractExpandQuery extends JPAAbstractJoinQuery {

  protected final JPAAssociationPath association;
  protected final Optional<JPAODataSkipTokenProvider> skipTokenProvider;

  static List<JPANavigationPropertyInfo> copyHops(final List<JPANavigationPropertyInfo> hops) {
    return hops.stream()
        .map(JPANavigationPropertyInfo::new)
        .toList();
  }

  JPAAbstractExpandQuery(final OData odata,
      final JPAEntityType jpaEntityType, final JPAODataRequestContextAccess requestContext,
      final JPAAssociationPath association) throws ODataException {

    super(odata, jpaEntityType, requestContext, emptyList());
    this.association = association;
    this.skipTokenProvider = Optional.empty();
  }

  JPAAbstractExpandQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPAInlineItemInfo item) throws ODataException {

    super(odata, item.getEntityType(), item.getUriInfo(), requestContext, item.getHops());
    this.association = item.getExpandAssociation();
    this.skipTokenProvider = item.getSkipTokenProvider();
  }

  JPAAbstractExpandQuery(final OData odata, final JPAODataRequestContextAccess requestContext, final JPAEntityType et,
      final JPAAssociationPath association, final List<JPANavigationPropertyInfo> hops) throws ODataException {

    super(odata, et, requestContext, hops);
    this.association = association;
    this.skipTokenProvider = Optional.empty();
  }

  protected String buildConcatenatedKey(final Tuple row, final JPAAssociationPath association)
      throws ODataJPAModelException {

    if (!association.hasJoinTable()) {
      final List<JPAPath> joinColumns = association.getRightColumnsList();
      return joinColumns.stream()
          .map(column -> (row.get(column
              .getAlias()))
                  .toString())
          .collect(joining(JPAPath.PATH_SEPARATOR));
    } else {
      final List<JPAPath> joinColumns = association.getLeftColumnsList();
      return joinColumns.stream()
          .map(column -> (row.get(association.getAlias() + ALIAS_SEPARATOR + column.getAlias())).toString())
          .collect(joining(JPAPath.PATH_SEPARATOR));
    }
  }

  protected List<Order> createOrderByJoinCondition(final JPAAssociationPath associationPath)
      throws ODataApplicationException {
    final List<Order> orders = new ArrayList<>();

    try {
      final List<JPAPath> joinColumns = associationPath.hasJoinTable()
          ? associationPath.getLeftColumnsList() : associationPath.getRightColumnsList();
      final From<?, ?> from = associationPath.hasJoinTable() ? determineParentFrom() : target;

      for (final JPAPath j : joinColumns) {
        Path<?> jpaProperty = from;
        for (final JPAElement pathElement : j.getPath()) {
          jpaProperty = jpaProperty.get(pathElement.getInternalName());
        }
        orders.add(cb.asc(jpaProperty));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return orders;
  }

  @SuppressWarnings("unchecked")
  protected <S, T> From<S, T> determineParentFrom() throws ODataJPAQueryException {
    for (final JPANavigationPropertyInfo item : this.navigationInfo) {
      if (item.getAssociationPath() == association)
        return (From<S, T>) item.getFromClause();
    }
    throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
        HttpStatusCode.BAD_REQUEST);
  }

  protected List<Expression<?>> buildExpandCountGroupBy(final From<?, ?> root) throws ODataJPAQueryException {
    if (association.hasJoinTable()) {
      return buildExpandCountGroupByJoinTable(root);
    } else
      return buildExpandCountGroupByJoin(root);
  }

  private List<Expression<?>> buildExpandCountGroupByJoin(final From<?, ?> root)
      throws ODataJPAQueryException {

    final List<Expression<?>> groupBy = new ArrayList<>();
    try {
      final List<JPAOnConditionItem> associationPathList = association.getJoinColumnsList();
      for (final JPAOnConditionItem onCondition : associationPathList) {
        groupBy.add(ExpressionUtility.convertToCriteriaPath(root, onCondition.getRightPath().getPath()));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return groupBy;
  }

  private List<Expression<?>> buildExpandCountGroupByJoinTable(final From<?, ?> root)
      throws ODataJPAQueryException {

    final List<Expression<?>> groupBy = new ArrayList<>();
    final JPAJoinTable joinTable = association.getJoinTable();
    try {
      for (final JPAOnConditionItem onCondition : joinTable.getJoinColumns()) {
        groupBy.add(ExpressionUtility.convertToCriteriaPath(root, onCondition.getRightPath().getPath()));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return groupBy;
  }

  abstract Map<String, Long> count() throws ODataApplicationException;

  protected boolean countRequested(final JPANavigationPropertyInfo lastInfo) {
    if (lastInfo.getUriInfo() == null)
      return false;
    final CountOption count = lastInfo.getUriInfo().getCountOption();
    final List<UriResource> parts = lastInfo.getUriInfo().getUriResourceParts();
    return count != null && count.getValue()
        || parts.size() > 1
            && parts.get(parts.size() - 1).getKind() == UriResourceKind.count;
  }

  protected List<Selection<?>> buildExpandJoinPath(final From<?, ?> root) throws ODataApplicationException {
    final List<Selection<?>> selections = new ArrayList<>();
    try {
      final List<JPAOnConditionItem> associationPathList = association.getJoinColumnsList();
      for (final JPAOnConditionItem onCondition : associationPathList) {
        final Path<?> p = ExpressionUtility.convertToCriteriaPath(root, onCondition.getRightPath().getPath());
        p.alias(onCondition.getRightPath().getAlias());
        selections.add(p);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return selections;
  }

  protected Map<String, Long> convertCountResult(final List<Tuple> intermediateResult) throws ODataJPAQueryException {
    final Map<String, Long> result = new HashMap<>();
    for (final Tuple row : intermediateResult) {
      try {
        final String actualKey = buildConcatenatedKey(row, association);
        final Number count = (Number) row.get(COUNT_COLUMN_NAME);
        result.put(actualKey, count.longValue());
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
      }
    }
    return result;
  }

  protected final List<Selection<?>> addCount(final List<Selection<?>> selectionPath) {
    final Expression<Long> count = cb.count(target);
    count.alias(COUNT_COLUMN_NAME);
    selectionPath.add(count);
    return selectionPath;
  }
}
