package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ATTRIBUTE_NOT_FOUND;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_NOT_ALLOWED_MEMBER;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ORDER_BY_TRANSIENT;
import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;
import static org.apache.olingo.commons.api.http.HttpStatusCode.FORBIDDEN;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;
import static org.apache.olingo.commons.api.http.HttpStatusCode.NOT_IMPLEMENTED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceCount;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.exception.ODataJPANotImplementedException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;

/**
 * Constructor of Order By clause.
 * @author Oliver Grande
 * @since 1.0.0
 */
final class JPAOrderByBuilder {
  private static final Log LOGGER = LogFactory.getLog(JPAOrderByBuilder.class);
  private static final String LOG_ORDER_BY = "Determined $orderby: convert to Order By";
  private final JPAEntityType jpaEntity;
  private final From<?, ?> target;
  private final CriteriaBuilder cb;
  private final List<String> groups;
  private final JPAOrderByBuilderWatchDog watchDog;

  JPAOrderByBuilder(final JPAEntityType jpaEntity, final From<?, ?> target, final CriteriaBuilder cb,
      final List<String> groups) {
    super();
    this.jpaEntity = jpaEntity;
    this.target = target;
    this.cb = cb;
    this.groups = groups;
    this.watchDog = new JPAOrderByBuilderWatchDog();
  }

  JPAOrderByBuilder(final JPAAnnotatable annotatable, final JPAEntityType jpaEntity, final From<?, ?> target,
      final CriteriaBuilder cb, final List<String> groups) throws ODataJPAQueryException {
    super();
    this.jpaEntity = jpaEntity;
    this.target = target;
    this.cb = cb;
    this.groups = groups;
    this.watchDog = new JPAOrderByBuilderWatchDog(annotatable);
  }

  /**
   * Create a list of order by for the root (non $expand) query part. Beside the $orderby query option, it also take
   * $top, $skip and an optional server driven paging into account, so that for the later once a stable sorting is
   * guaranteed.
   * <p>
   * If asc or desc is not specified, the service MUST order by the specified property in ascending order.
   * See: <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398305"
   * >OData Version 4.0 Part 1 - 11.2.5.2 System Query Option $orderby</a>
   * <p>
   *
   * Some OData example requests:<br>
   * .../Organizations?$orderby=Address/Country --> one item, two resourcePaths
   * [...ComplexProperty,...PrimitiveProperty]<br>
   * .../Organizations?$orderby=Roles/$count --> one item, two resourcePaths [...NavigationProperty,...Count]<br>
   * .../Organizations?$orderby=Roles/$count desc,Address/Country asc -->two items
   * .../AdminustrativeDivision?$orderby=Parent/DivisionCode
   * <p>
   * SQL example to order by number of entities
   * <p>
   * <code>
   * SELECT t0."BusinessPartnerID" ,COUNT(t1."BusinessPartnerID")
   * <pre>FROM "OLINGO"."org.apache.olingo.jpa::BusinessPartner" t0 <br>
   * LEFT OUTER JOIN "OLINGO"."org.apache.olingo.jpa::BusinessPartnerRole" t1 <br>
   * ON (t1."BusinessPartnerID" = t0."BusinessPartnerID")}
   * WHERE (t0."Type" = ?)<br>
   * GROUP BY t0."BusinessPartnerID"<br>
   * ORDER BY COUNT(t1."BusinessPartnerID") DESC<br></pre>
   * </code>
   * @since 1.0.0
   * @param joinTables
   * @param uriResource
   * @param page
   * @param orderByPaths: A collection of paths to the properties within the ORDER BY clause
   * @return A list of generated orderby clauses
   * @throws ODataApplicationException
   */
  @Nonnull
  List<Order> createOrderByList(@Nonnull final Map<String, From<?, ?>> joinTables,
      @Nonnull final UriInfoResource uriResource, @Nullable final JPAODataPage page, Set<Path<?>> orderByPaths)
      throws ODataApplicationException {

    final List<Order> result = new ArrayList<>();
    try {
      if (uriResource.getOrderByOption() != null) {
        LOGGER.trace(LOG_ORDER_BY);
        addOrderByFromUriResource(joinTables, result, orderByPaths, uriResource.getOrderByOption());
        watchDog.watch(result);
      }
      if (uriResource.getTopOption() != null || uriResource.getSkipOption() != null
          || (page != null && page.top() != Integer.MAX_VALUE)) {
        LOGGER.trace("Determined $top/$skip or page: add primary key to Order By");
        addOrderByPrimaryKey(result, orderByPaths);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, BAD_REQUEST);
    }
    return result;
  }

  @Nonnull
  List<Order> createOrderByList(final Map<String, From<?, ?>> joinTables) {
    return Collections.emptyList();
  }

  /**
   * Create a list of order by for $expand query part. It does not take top and skip into account, but the
   * association.
   * <p>
   */
  @Nonnull
  List<Order> createOrderByList(@Nonnull final Map<String, From<?, ?>> joinTables,
      @Nullable final OrderByOption orderBy, @Nonnull final JPAAssociationPath association)
      throws ODataApplicationException {

    final List<Order> result = new ArrayList<>();
    final Set<Path<?>> orderByPaths = new HashSet<>();
    try {
      LOGGER.trace("Determined relationship and add corresponding to OrderBy");
      addOrderByJoinCondition(association, result);
      if (orderBy != null) {
        LOGGER.trace(LOG_ORDER_BY);
        addOrderByFromUriResource(joinTables, result, orderByPaths, orderBy);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, BAD_REQUEST);
    }
    return result;
  }

  @Nonnull
  List<Order> createOrderByListAlias(@Nonnull final Map<String, From<?, ?>> joinTables,
      @Nullable final OrderByOption orderBy, @Nonnull final JPAAssociationPath association)
      throws ODataApplicationException {

    final List<Order> result = new ArrayList<>();
    final Set<Path<?>> orderByPaths = new HashSet<>();
    try {
      LOGGER.trace("Determined relationship and add corresponding to OrderBy");
      addOrderByJoinConditionAlias(association, result);
      if (orderBy != null) {
        LOGGER.trace(LOG_ORDER_BY);
        addOrderByFromUriResource(joinTables, result, orderByPaths, orderBy);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, BAD_REQUEST);
    }
    return result;
  }

  @Nonnull
  List<Order> createOrderByList(@Nonnull final Map<String, From<?, ?>> joinTables,
      @Nullable final OrderByOption orderBy) throws ODataApplicationException {

    final List<Order> result = new ArrayList<>();
    final Set<Path<?>> orderByPaths = new HashSet<>();
    try {
      if (orderBy != null) {
        LOGGER.trace(LOG_ORDER_BY);
        addOrderByFromUriResource(joinTables, result, orderByPaths, orderBy);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, BAD_REQUEST);
    }
    return result;
  }

  void addOrderByJoinConditionAlias(final JPAAssociationPath association, final List<Order> orders)
      throws ODataApplicationException {

    try {
      final List<JPAPath> joinColumns = association.hasJoinTable()
          ? asPathList(association) : association.getRightColumnsList();

      for (final JPAPath j : joinColumns) {
        final Path<Object> jpaProperty = target.get(j.getAlias());
        orders.add(cb.asc(jpaProperty));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  void addOrderByJoinCondition(final JPAAssociationPath association, final List<Order> orders)
      throws ODataApplicationException {

    try {
      final List<JPAPath> joinColumns = association.hasJoinTable()
          ? asPathList(association) : association.getRightColumnsList();

      for (final JPAPath j : joinColumns) {
        Path<?> jpaProperty = target;
        for (final JPAElement pathElement : j.getPath()) {
          jpaProperty = jpaProperty.get(pathElement.getInternalName());
        }
        orders.add(cb.asc(jpaProperty));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  private List<JPAPath> asPathList(final JPAAssociationPath association) throws ODataJPAModelException {
    final List<JPAOnConditionItem> joinColumns = association.getJoinTable().getJoinColumns();
    return joinColumns.stream()
        .map(JPAOnConditionItem::getRightPath)
        .toList();
  }

  @SuppressWarnings("unchecked")
  <X extends Object, Y extends Object> From<X, Y> determineParentFrom(final JPAAssociationPath association,
      final List<JPANavigationPropertyInfo> navigationInfo) throws ODataJPAQueryException {

    for (final JPANavigationPropertyInfo item : navigationInfo) {
      if (item.getAssociationPath() == association) {
        return (From<X, Y>) item.getFromClause();
      }
    }
    throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
        HttpStatusCode.BAD_REQUEST);
  }

  private void addOrderByExpression(final List<Order> orders, final OrderByItem orderByItem,
      final jakarta.persistence.criteria.Expression<?> expression) {

    if (orderByItem.isDescending()) {
      orders.add(cb.desc(expression));
    } else {
      orders.add(cb.asc(expression));
    }
  }

  private void addOrderByExpression(final List<Order> orders, final OrderByItem orderByItem,
      final Set<Path<?>> orderByPaths, final Path<?> path) {

    if (!orderByPaths.contains(path)) {
      orderByPaths.add(path);
      addOrderByExpression(orders, orderByItem, path);
    }
  }

  private void addOrderByFromUriResource(final Map<String, From<?, ?>> joinTables, final List<Order> orders,
      final Set<Path<?>> orderByPaths, final OrderByOption orderByOption) throws ODataJPAProcessException,
      ODataJPAModelException {

    for (final OrderByItem orderByItem : orderByOption.getOrders()) {
      final Expression expression = orderByItem.getExpression();
      if (expression instanceof final Member member) {
        final UriInfoResource resourcePath = member.getResourcePath();
        JPAStructuredType type = jpaEntity;
        Path<?> path = target;
        StringBuilder externalPath = new StringBuilder();
        for (final UriResource uriResourceItem : resourcePath.getUriResourceParts()) {
          if (isPrimitiveSimpleProperty(uriResourceItem)) {
            // addPathByAttribute(externalPath, getAttribute(type, uriResourceItem));
            path = convertPropertyPath(type, uriResourceItem, path);
            addOrderByExpression(orders, orderByItem, orderByPaths, path);
          } else if (isComplexSimpleProperty(uriResourceItem)) {
            final JPAAttribute attribute = getAttribute(type, uriResourceItem);
            addPathByAttribute(externalPath, attribute);
            path = path.get(attribute.getInternalName());
            type = attribute.getStructuredType();
          } else if ((uriResourceItem instanceof final UriResourceNavigation navigation
              && navigation.isCollection())
              || (uriResourceItem instanceof final UriResourceProperty property
                  && property.isCollection())) {
            // In case the orderby contains a navigation or collection a $count has to follow. This is ensured by Olingo
            appendPathByCollection(externalPath, uriResourceItem);
            final From<?, ?> join = joinTables.get(externalPath.toString());
            addOrderByExpression(orders, orderByItem, cb.count(join));
          } else if (uriResourceItem instanceof UriResourceNavigation) {
            appendPathByCollection(externalPath, uriResourceItem);
            type = type.getAssociationPath(externalPath.toString()).getTargetType();
            path = joinTables.get(externalPath.toString());
            externalPath = new StringBuilder();
          } else if (!(uriResourceItem instanceof UriResourceCount)) {
            throw new ODataJPANotImplementedException("orderby using " + uriResourceItem.getKind().name());
          }
        }
      }
    }
  }

  private Path<?> convertPropertyPath(final JPAStructuredType type,
      final UriResource uriResourceItem, final Path<?> startPath)
      throws ODataJPAQueryException, ODataJPAProcessorException, ODataJPAModelException {

    final JPAPath attributePath = type.getPath(((UriResourceProperty) uriResourceItem).getProperty().getName());
    if (attributePath == null) {
      throw new ODataJPAProcessorException(ATTRIBUTE_NOT_FOUND, INTERNAL_SERVER_ERROR,
          uriResourceItem.getSegmentValue());
    }
    if (!attributePath.isPartOfGroups(groups)) {
      throw new ODataJPAQueryException(QUERY_PREPARATION_NOT_ALLOWED_MEMBER, FORBIDDEN, attributePath.getAlias());
    }
    Path<?> path = startPath;
    for (final JPAElement pathElement : attributePath.getPath()) {
      if (pathElement instanceof final JPAAttribute attribute && attribute.isTransient()) {
        throw new ODataJPAQueryException(QUERY_PREPARATION_ORDER_BY_TRANSIENT, NOT_IMPLEMENTED,
            pathElement.getExternalName());
      }
      path = path.get(pathElement.getInternalName());
    }
    path.alias(attributePath.getAlias());
    return path;
  }

  private void addOrderByPrimaryKey(final List<Order> orders, final Set<Path<?>> existing)
      throws ODataJPAQueryException, ODataJPAModelException {

    final List<Path<Object>> paths = ExpressionUtility.convertToCriteriaPathList(target, jpaEntity, jpaEntity.getKey());
    for (final Path<Object> p : paths) {
      if (!existing.contains(p)) {
        orders.add(cb.asc(p));
        existing.add(p);
      }
    }
  }

  private void addPathByAttribute(final StringBuilder externalPath, final JPAAttribute attribute) {
    externalPath.append(attribute.getExternalName());
    externalPath.append(JPAPath.PATH_SEPARATOR);
  }

  private void appendPathByCollection(final StringBuilder externalPath, final UriResource uriResourceItem) {
    if (uriResourceItem instanceof final UriResourceNavigation navigation) {
      externalPath.append(navigation.getProperty().getName());
    } else {
      externalPath.append(((UriResourceProperty) uriResourceItem).getProperty().getName());
    }
  }

  private JPAAttribute getAttribute(final JPAStructuredType type, final UriResource uriResourceItem)
      throws ODataJPAProcessorException, ODataJPAModelException, ODataJPAQueryException {

    final JPAAttribute attribute = type.getAttribute((UriResourceProperty) uriResourceItem).orElseThrow(
        () -> new ODataJPAProcessorException(ATTRIBUTE_NOT_FOUND, INTERNAL_SERVER_ERROR,
            uriResourceItem.getSegmentValue()));
    if (attribute.isTransient()) {
      throw new ODataJPAQueryException(QUERY_PREPARATION_ORDER_BY_TRANSIENT, NOT_IMPLEMENTED,
          attribute.getExternalName());
    }
    return attribute;

  }

  private boolean isComplexSimpleProperty(final UriResource uriResourceItem) {
    return uriResourceItem instanceof UriResourceComplexProperty
        && !((UriResourceProperty) uriResourceItem).isCollection();
  }

  private boolean isPrimitiveSimpleProperty(final UriResource uriResourceItem) {
    return uriResourceItem instanceof UriResourcePrimitiveProperty
        && !((UriResourceProperty) uriResourceItem).isCollection();
  }
}
