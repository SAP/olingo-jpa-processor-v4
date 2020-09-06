package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ATTRIBUTE_NOT_FOUND;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_NOT_ALLOWED_MEMBER;
import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;
import static org.apache.olingo.commons.api.http.HttpStatusCode.FORBIDDEN;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;

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

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPANotImplementedException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

/**
 * Constructor of Order By clause.
 * @author Oliver Grande 
 * @since 1.0.0
 */
class JPAOrderByBuilder {

  private final JPAEntityType jpaEntity;
  private final From<?, ?> target;
  private final CriteriaBuilder cb;
  private final List<String> groups;

  JPAOrderByBuilder(final JPAEntityType jpaEntity, final From<?, ?> target, final CriteriaBuilder cb,
      final List<String> groups) {
    super();
    this.jpaEntity = jpaEntity;
    this.target = target;
    this.cb = cb;
    this.groups = groups;
  }

  /**
   * Create a list of oder by for the root (non $expand) query part. Beside the $orderby query option, it also take $top
   * and $skip into account. SO that for the later once a stable sorting is guaranteed.<p>
   * If asc or desc is not specified, the service MUST order by the specified property in ascending order.
   * See: <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398305"
   * >OData Version 4.0 Part 1 - 11.2.5.2 System Query Option $orderby</a> <p>
   *
   * Some OData example requests:<br>
   * .../Organizations?$orderby=Address/Country --> one item, two resourcePaths
   * [...ComplexProperty,...PrimitiveProperty]<br>
   * .../Organizations?$orderby=Roles/$count --> one item, two resourcePaths [...NavigationProperty,...Count]<br>
   * .../Organizations?$orderby=Roles/$count desc,Address/Country asc -->two items <p>
   * SQL example to order by number of entities<br>
   * <code>
   * SELECT t0."BusinessPartnerID" ,COUNT(t1."BusinessPartnerID")<br>
   * FROM "OLINGO"."org.apache.olingo.jpa::BusinessPartner" t0 <br>
   * LEFT OUTER JOIN "OLINGO"."org.apache.olingo.jpa::BusinessPartnerRole" t1 <br>
   * ON (t1."BusinessPartnerID" = t0."BusinessPartnerID")} v
   * WHERE (t0."Type" = ?)<br>
   * GROUP BY t0."BusinessPartnerID"<br>
   * ORDER BY COUNT(t1."BusinessPartnerID") DESC<br>
   * </code>
   * @since 1.0.0
   * @param joinTables
   * @param uriResource
   * @return A list of generated orderby clauses
   * @throws ODataApplicationException
   */
  @Nonnull
  List<Order> createOrderByList(@Nonnull final Map<String, From<?, ?>> joinTables,
      @Nonnull final UriInfoResource uriResource) throws ODataApplicationException {
    final List<Order> result = new ArrayList<>();
    try {
      if (uriResource.getOrderByOption() != null) {
        addOrderByFromUriResource(joinTables, result, uriResource.getOrderByOption());
      }
      if (uriResource.getTopOption() != null || uriResource.getSkipOption() != null) {
        addOrderByPrimaryKey(result);
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

  private void addOrderByExpression(final List<Order> orders, final OrderByItem orderByItem,
      final javax.persistence.criteria.Expression<?> expression) {

    if (orderByItem.isDescending())
      orders.add(cb.desc(expression));
    else
      orders.add(cb.asc(expression));
  }

  private void addOrderByFromUriResource(final Map<String, From<?, ?>> joinTables, final List<Order> orders,
      final OrderByOption orderByOption) throws ODataJPAProcessException,
      ODataJPAModelException {

    for (final OrderByItem orderByItem : orderByOption.getOrders()) {
      final Expression expression = orderByItem.getExpression();
      if (expression instanceof Member) {
        final UriInfoResource resourcePath = ((Member) expression).getResourcePath();
        JPAStructuredType type = jpaEntity;
        Path<?> p = target;
        final StringBuilder externalPath = new StringBuilder();
        for (final UriResource uriResourceItem : resourcePath.getUriResourceParts()) {
          if (isPrimitiveSimpleProperty(uriResourceItem)) {
            p = convertPropertyPath(type, uriResourceItem, p);
            addOrderByExpression(orders, orderByItem, p);
          } else if (isComplexSimpleProperty(uriResourceItem)) {
            final JPAAttribute attribute = getAttribute(type, uriResourceItem);
            addPathByAttribute(externalPath, attribute);
            p = p.get(attribute.getInternalName());
            type = attribute.getStructuredType();
          } else if (uriResourceItem instanceof UriResourceNavigation
              || (uriResourceItem instanceof UriResourceProperty
                  && ((UriResourceProperty) uriResourceItem).isCollection())) {
            // In case the orderby contains a navigation or collection a $count has to follow. This is ensured by Olingo
            appendPathByCollection(externalPath, uriResourceItem);
            final From<?, ?> join = joinTables.get(externalPath.toString());
            addOrderByExpression(orders, orderByItem, cb.count(join));
          } else if (!(uriResourceItem instanceof UriResourceCount)) {
            throw new ODataJPANotImplementedException("orderby using " + uriResourceItem.getKind().name());
          }
        }
      }
    }
  }

  private Path<?> convertPropertyPath(final JPAStructuredType type,
      final UriResource uriResourceItem, final Path<?> p)
      throws ODataJPAQueryException, ODataJPAProcessorException, ODataJPAModelException {

    final JPAPath attributePath = type.getPath(((UriResourceProperty) uriResourceItem).getProperty().getName());
    if (attributePath == null)
      throw new ODataJPAProcessorException(ATTRIBUTE_NOT_FOUND, INTERNAL_SERVER_ERROR,
          uriResourceItem.getSegmentValue());
    if (!attributePath.isPartOfGroups(groups))
      throw new ODataJPAQueryException(QUERY_PREPARATION_NOT_ALLOWED_MEMBER, FORBIDDEN, attributePath.getAlias());
    Path<?> path = p;
    for (final JPAElement pathElement : attributePath.getPath()) {
      path = path.get(pathElement.getInternalName());
    }
    return path;
  }

  private void addOrderByPrimaryKey(final List<Order> orders) throws ODataJPAModelException {

    for (final JPAPath keyPath : jpaEntity.getKeyPath()) {
      final Path<?> p = target;
      for (final JPAElement pathElement : keyPath.getPath()) {
        p.get(pathElement.getInternalName());
      }
      orders.add(cb.asc(p));
    }
  }

  private void addPathByAttribute(final StringBuilder externalPath, final JPAAttribute attribute) {
    externalPath.append(attribute.getExternalName());
    externalPath.append(JPAPath.PATH_SEPERATOR);
  }

  private void appendPathByCollection(final StringBuilder externalPath, final UriResource uriResourceItem) {
    if (uriResourceItem instanceof UriResourceNavigation)
      externalPath.append(((UriResourceNavigation) uriResourceItem).getProperty().getName());
    else
      externalPath.append(((UriResourceProperty) uriResourceItem).getProperty().getName());
  }

  private JPAAttribute getAttribute(final JPAStructuredType type, final UriResource uriResourceItem)
      throws ODataJPAProcessorException, ODataJPAModelException, ODataJPAQueryException {

    final JPAAttribute attribute = type.getAttribute((UriResourceProperty) uriResourceItem);
    if (attribute == null)
      throw new ODataJPAProcessorException(ATTRIBUTE_NOT_FOUND, INTERNAL_SERVER_ERROR,
          uriResourceItem.getSegmentValue());
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
