package com.sap.olingo.jpa.processor.core.query;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.CountOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public abstract class JPAAbstractExpandQuery extends JPAAbstractJoinQuery {

  protected final JPAAssociationPath association;

  JPAAbstractExpandQuery(final OData odata,
      final JPAEntityType jpaEntityType, final JPAODataRequestContextAccess requestContext,
      final JPAAssociationPath association) throws ODataException {

    super(odata, jpaEntityType, requestContext, emptyList());
    this.association = association;
  }

  JPAAbstractExpandQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPAInlineItemInfo item) throws ODataException {

    super(odata, item.getEntityType(), item.getUriInfo(), requestContext, item.getHops());
    this.association = getAssociation(item);
  }

  JPAAbstractExpandQuery(final OData odata, final JPAODataRequestContextAccess requestContext, final JPAEntityType et,
      final JPAAssociationPath association, final List<JPANavigationPropertyInfo> hops) throws ODataException {

    super(odata, et, requestContext, hops);
    this.association = association;
  }

  protected abstract JPAAssociationPath getAssociation(JPAInlineItemInfo item);

  @Override
  public abstract JPAExpandQueryResult execute() throws ODataApplicationException;

  @Override
  protected SelectionPathInfo<JPAPath> buildSelectionPathList(final UriInfoResource uriResource)
      throws ODataApplicationException {
    try {
      final SelectionPathInfo<JPAPath> jpaPathList = super.buildSelectionPathList(uriResource);
      debugger.trace(this, "The following selection path elements were found: %s", jpaPathList.toString());
      return new SelectionPathInfo<>(association.getRightColumnsList(), jpaPathList);
    } catch (final ODataJPAModelException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR
          .getStatusCode(), ODataJPAException.getLocales().nextElement(), e);
    }
  }

  protected String buildConcatenatedKey(final Tuple row, final JPAAssociationPath association)
      throws ODataJPAModelException {

    if (!association.hasJoinTable()) {
      final List<JPAPath> joinColumns = association.getRightColumnsList();
      return joinColumns.stream()
          .map(c -> (row.get(c
              .getAlias()))
                  .toString())
          .collect(joining(JPAPath.PATH_SEPARATOR));
    } else {
      final List<JPAPath> joinColumns = association.getLeftColumnsList();
      return joinColumns.stream()
          .map(c -> (row.get(association.getAlias() + ALIAS_SEPARATOR + c.getAlias())).toString())
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

  protected From<?, ?> determineParentFrom() throws ODataJPAQueryException {
    for (final JPANavigationPropertyInfo item : this.navigationInfo) {
      if (item.getAssociationPath() == association)
        return item.getFromClause();
    }
    throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
        HttpStatusCode.BAD_REQUEST);
  }

  protected List<Expression<?>> buildExpandCountGroupBy(final From<?, ?> root)
      throws ODataJPAQueryException {

    final List<Expression<?>> groupBy = new ArrayList<>();
    try {
      final List<JPAOnConditionItem> associationPathList = association.getJoinColumnsList();
      for (final JPAOnConditionItem onCondition : associationPathList) {
        groupBy.add(ExpressionUtil.convertToCriteriaPath(root, onCondition.getRightPath().getPath()));
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
        final Path<?> p = ExpressionUtil.convertToCriteriaPath(root, onCondition.getRightPath().getPath());
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
