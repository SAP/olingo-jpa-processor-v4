package org.apache.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.api.JPAODataContextAccess;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

public class JPAQuery extends JPAExecutableQuery {
  // private final EdmEntitySet edmEntitySet;

  public JPAQuery(final OData odata, final EdmEntitySet entitySet, final JPAODataContextAccess context,
      final UriInfo uriInfo, final EntityManager em, final Map<String, List<String>> requestHeaders)
          throws ODataApplicationException {
    super(odata, context, entitySet.getEntityType(), em, requestHeaders, uriInfo);
  }

  /**
   * Counts the number of results to be expected by a query. The method shall fulfill the requirements of the $count
   * query option. This is defined as follows:<p>
   * <i>The $count system query option ignores any $top, $skip, or $expand query options, and returns the total count
   * of results across all pages including only those results matching any specified $filter and $search.</i><p>
   * For details see: <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398308"
   * >OData Version 4.0 Part 1 - 11.2.5.5 System Query Option $count</a>
   * 
   * @return Number of results
   * @throws ODataApplicationException
   * @throws ExpressionVisitException
   */
  public Long countResults() throws ODataApplicationException {
    /*
     * URL example:
     * .../Organizations?$count=true
     * .../Organizations/count
     * .../Organizations('3')/Roles/$count
     */

    final HashMap<String, From<?, ?>> joinTables = new HashMap<String, From<?, ?>>();

    final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    // root = cq.from(jpaEntity.getTypeClass());

    joinTables.put(jpaEntity.getTypeClass().getCanonicalName(), root);

    final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere(joinTables);
    if (whereClause != null)
      cq.where(whereClause);
    cq.select(cb.count(root));
    return em.createQuery(cq).getSingleResult();
  }

  public JPAExpandResult execute() throws ODataApplicationException {
    // Pre-process URI parameter, so they can be used at different places
    // TODO check if Path is also required for OrderBy Attributes, as it is for descriptions
    final List<JPAAssociationAttribute> orderByNaviAttributes = extractOrderByNaviAttributes();
    final List<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
    final List<JPAPath> descriptionAttributes = extractDescriptionAttributes(selectionPath);
    final Map<String, From<?, ?>> joinTables = createFromClause(orderByNaviAttributes, descriptionAttributes);

    cq.multiselect(createSelectClause(joinTables, selectionPath));

    final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere(joinTables);
    if (whereClause != null)
      cq.where(whereClause);

    cq.orderBy(createOrderList(joinTables, uriResource.getOrderByOption()));

    if (!orderByNaviAttributes.isEmpty())
      cq.groupBy(createGroupBy(joinTables, selectionPath));

    final TypedQuery<Tuple> tq = em.createQuery(cq);
    addTopSkip(tq);
    final HashMap<String, List<Tuple>> result = new HashMap<String, List<Tuple>>(1);
    result.put("root", tq.getResultList());
    return new JPAExpandResult(result, Long.parseLong("0"), edmType);// count()););
  }

  public JPAStructuredType getEntityType() {
    return jpaEntity;
  }

  public SelectOption getSelectOption() {
    return uriResource.getSelectOption();
  }

  private List<javax.persistence.criteria.Expression<?>> createGroupBy(final Map<String, From<?, ?>> joinTables,
      final List<JPAPath> selectionPathList) throws ODataApplicationException {
    final List<javax.persistence.criteria.Expression<?>> groupBy =
        new ArrayList<javax.persistence.criteria.Expression<?>>();

    for (final JPAPath jpaPath : selectionPathList) {
      groupBy.add(convertToCriteriaPath(joinTables, jpaPath));
    }

    return groupBy;
  }

  private List<JPAAssociationAttribute> extractOrderByNaviAttributes() throws ODataApplicationException {
    final List<JPAAssociationAttribute> naviAttributes = new ArrayList<JPAAssociationAttribute>();

    final OrderByOption orderBy = uriResource.getOrderByOption();
    if (orderBy != null) {
      for (final OrderByItem orderByItem : orderBy.getOrders()) {
        final Expression expression = orderByItem.getExpression();
        if (expression instanceof Member) {
          final UriInfoResource resourcePath = ((Member) expression).getResourcePath();
          for (final UriResource uriResource : resourcePath.getUriResourceParts()) {
            if (uriResource instanceof UriResourceNavigation) {
              final EdmNavigationProperty edmNaviProperty = ((UriResourceNavigation) uriResource).getProperty();
              try {
                naviAttributes.add(jpaEntity.getAssociationPath(edmNaviProperty.getName())
                    .getLeaf());
              } catch (ODataJPAModelException e) {
                throw new ODataApplicationException("Property not found", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                    Locale.ENGLISH, e);
              }
            }
          }
        }
      }
    }
    return naviAttributes;
  }

}
