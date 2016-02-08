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
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

public class JPAQuery extends JPAExecutableQuery {
  // private final EdmEntitySet edmEntitySet;

  public JPAQuery(EdmEntitySet entitySet, final ServicDocument sd, final UriInfo uriInfo,
      final EntityManager em, Map<String, List<String>> requestHeaders) throws ODataApplicationException {
    super(sd, entitySet.getEntityType(), em, requestHeaders, uriInfo);

    // this.edmEntitySet = entitySet;
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
   */
  public Long countResults() throws ODataApplicationException {
    /*
     * URL example:
     * .../Organizations?$count=true
     * .../Organizations/count
     * .../Organizations('3')/Roles/$count
     */

    final HashMap<String, From<?, ?>> joinTables = new HashMap<String, From<?, ?>>();

    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    // root = cq.from(jpaEntity.getTypeClass());

    joinTables.put(jpaEntity.getTypeClass().getCanonicalName(), root);

    javax.persistence.criteria.Expression<Boolean> whereClause = createWhere(joinTables);
    if (whereClause != null)
      cq.where(whereClause);
    cq.select(cb.count(root));
    return em.createQuery(cq).getSingleResult();
  }

  public List<Tuple> execute() throws ODataApplicationException {
    // Pre-process URI parameter, so they can be used at different places
    // TODO check if Path is also required for OrderBy Attributes, as it is for descriptions
    final List<JPAAssociationAttribute> orderByNaviAttributes = extractOrderByNaviAttributes();
    final List<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
    final List<JPAPath> descriptionAttributes = extractDescriptionAttributes(selectionPath);
    final HashMap<String, From<?, ?>> joinTables = createFromClause(orderByNaviAttributes, descriptionAttributes);

    cq.multiselect(createSelectClause(joinTables, selectionPath));

    javax.persistence.criteria.Expression<Boolean> whereClause = createWhere(joinTables);
    if (whereClause != null)
      cq.where(whereClause);

    cq.orderBy(createOrderList(joinTables, uriResource.getOrderByOption()));

    if (orderByNaviAttributes.size() > 0)
      cq.groupBy(createGroupBy(joinTables));

    TypedQuery<Tuple> tq = em.createQuery(cq);
    addTopSkip(tq);

    return tq.getResultList();
  }

  public JPAStructuredType getEntityType() {
    return jpaEntity;
  }

  public SelectOption getSelectOption() {
    return uriResource.getSelectOption();
  }

  private List<javax.persistence.criteria.Expression<?>> createGroupBy(HashMap<String, From<?, ?>> joinTables)
      throws ODataApplicationException {
    List<javax.persistence.criteria.Expression<?>> groupBy = new ArrayList<javax.persistence.criteria.Expression<?>>();
    Root<?> root = (Root<?>) joinTables.get(jpaEntity.getInternalName());

    try {
      for (JPAAttribute key : jpaEntity.getKey()) {
        groupBy.add(root.get(key.getInternalName()));
      }
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Property not found", HttpStatusCode.BAD_REQUEST.ordinal(),
          Locale.ENGLISH, e);
    }
    return groupBy; // joinTables.get(jpaEntity.getInternalName());
  }

  private List<JPAAssociationAttribute> extractOrderByNaviAttributes() throws ODataApplicationException {
    List<JPAAssociationAttribute> naviAttributes = new ArrayList<JPAAssociationAttribute>();

    OrderByOption orderBy = uriResource.getOrderByOption();
    if (orderBy != null) {
      for (OrderByItem orderByItem : orderBy.getOrders()) {
        Expression expression = orderByItem.getExpression();
        if (expression instanceof Member) {
          UriInfoResource resourcePath = ((Member) expression).getResourcePath();
          for (UriResource uriResource : resourcePath.getUriResourceParts()) {
            if (uriResource instanceof UriResourceNavigation) {
              EdmNavigationProperty edmNaviProperty = ((UriResourceNavigation) uriResource).getProperty();
              try {
                naviAttributes.add((JPAAssociationAttribute) jpaEntity.getAssociationPath(edmNaviProperty.getName())
                    .getLeaf());
              } catch (ODataJPAModelException e) {
                throw new ODataApplicationException("Property not found", HttpStatusCode.BAD_REQUEST.ordinal(),
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
