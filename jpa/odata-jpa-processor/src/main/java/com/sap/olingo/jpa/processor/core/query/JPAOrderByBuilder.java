package com.sap.olingo.jpa.processor.core.query;

import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.properties.JPAOrderByPropertyFactory;
import com.sap.olingo.jpa.processor.core.properties.JPAProcessorAttribute;

/**
 * Constructor of Order By clause.
 * @author Oliver Grande
 * @since 1.0.0
 */
final class JPAOrderByBuilder {
  private static final String ORDER_BY_LO_ENTRY = "Determined relationship and add corresponding to OrderBy";
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
   * @param uriInfoResource
   * @param orderByPaths: A collection of paths to the properties within the ORDER BY clause
   * @return A list of generated orderby clauses
   * @throws ODataApplicationException
   */
  @Nonnull
  List<Order> createOrderByList(@Nonnull final Map<String, From<?, ?>> joinTables,
      final List<JPAProcessorAttribute> orderByAttributes, final UriInfoResource uriInfoResource)
      throws ODataJPAQueryException {

    final List<Order> result = new ArrayList<>();
    try {
      if (uriInfoResource != null
          && (uriInfoResource.getTopOption() != null
              || uriInfoResource.getSkipOption() != null)) {
        LOGGER.trace("Determined $top/$skip or page: add primary key to Order By");
        final var factory = new JPAOrderByPropertyFactory();
        for (final var key : jpaEntity.getKey()) {
          if (!containsAttribute(orderByAttributes, key))
            orderByAttributes.add(factory.createProperty(target, jpaEntity.getPath(key.getExternalName()), cb));
        }
      }

      if (!orderByAttributes.isEmpty()) {
        LOGGER.trace(LOG_ORDER_BY);
        addOrderByProperties(orderByAttributes, result);
        watchDog.watch(result);
      }

    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, BAD_REQUEST);
    }
    return result;
  }

  private boolean containsAttribute(final List<JPAProcessorAttribute> orderByAttributes, final JPAAttribute key)
      throws ODataJPAModelException {

    var found = false;
    for (final var attribute : orderByAttributes) {
      found = attribute.getJPAPath().equals(jpaEntity.getPath(key.getExternalName()));
      if (found)
        break;
    }
    return found;
  }

  @Nonnull
  List<Order> createOrderByList(final Map<String, From<?, ?>> joinTables) {
    return Collections.emptyList();
  }

  /**
   * For row number query with join table
   * @param association
   * @return
   * @throws ODataApplicationException
   */
  List<Order> createOrderByList(final JPAAssociationPath association)
      throws ODataApplicationException {
    final List<Order> result = new ArrayList<>();
    addOrderByInvertJoinCondition(association, result);
    return result;
  }

  /**
   * Create a list of order by for $expand query part. It does not take top and skip into account, but the
   * association.
   * <p>
   * @throws ODataApplicationException
   */
  @Nonnull
  List<Order> createOrderByList(final Map<String, From<?, ?>> joinTables,
      final List<JPAProcessorAttribute> orderByAttributes, @Nonnull final JPAAssociationPath association)
      throws ODataApplicationException {
    final List<Order> result = new ArrayList<>();
    addOrderByJoinCondition(orderByAttributes, association);
    addOrderByProperties(orderByAttributes, result);
    return result;
  }

  @Nonnull
  List<Order> createOrderByListAlias(@Nonnull final Map<String, From<?, ?>> joinTables,
      final List<JPAProcessorAttribute> orderByAttributes, @Nonnull final JPAAssociationPath association)
      throws ODataApplicationException {

    final List<Order> result = new ArrayList<>();

    addOrderByJoinConditionAlias(association, result);
    addOrderByProperties(orderByAttributes, result);

    return result;
  }

  @Nonnull
  List<Order> createOrderByList(@Nonnull final Map<String, From<?, ?>> joinTables,
      final List<JPAProcessorAttribute> orderByAttributes) throws ODataApplicationException {

    final List<Order> result = new ArrayList<>();
    addOrderByProperties(orderByAttributes, result);
    return result;
  }

  void addOrderByProperties(final List<JPAProcessorAttribute> orderByAttributes, final List<Order> result)
      throws ODataJPAQueryException {
    for (final var attribute : orderByAttributes) {
      result.add(attribute.createOrderBy(cb, groups));
    }
  }

  void addOrderByJoinConditionAlias(final JPAAssociationPath association, final List<Order> orders)
      throws ODataApplicationException {

    LOGGER.trace(ORDER_BY_LO_ENTRY);
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

  void addOrderByJoinCondition(final List<JPAProcessorAttribute> orderByAttributes,
      final JPAAssociationPath association) throws ODataApplicationException {

    LOGGER.trace(ORDER_BY_LO_ENTRY);
    try {
      final var factory = new JPAOrderByPropertyFactory();
      final List<JPAPath> joinColumns = association.hasJoinTable()
          ? asPathList(association) : association.getRightColumnsList();

      for (var i = joinColumns.size() - 1; i >= 0; i--) {
        final JPAPath joinPath = joinColumns.get(i);
        orderByAttributes.add(0, factory.createProperty(target, joinPath, cb));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  void addOrderByInvertJoinCondition(final JPAAssociationPath association, final List<Order> orders)
      throws ODataApplicationException {

    LOGGER.trace(ORDER_BY_LO_ENTRY);
    try {
      final List<JPAPath> joinColumns = association.hasJoinTable()
          ? asInvertPathList(association) : association.getLeftColumnsList();

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

  private List<JPAPath> asInvertPathList(final JPAAssociationPath association) throws ODataJPAModelException {
    final List<JPAOnConditionItem> joinColumns = association.getJoinTable().getJoinColumns();
    return joinColumns.stream()
        .map(JPAOnConditionItem::getLeftPath)
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

}
