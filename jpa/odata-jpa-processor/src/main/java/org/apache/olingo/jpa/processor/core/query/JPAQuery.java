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
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
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
   */
  public Long countResults() {
    /*
     * URL example: .../Organizations?$count=true
     */
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<?> root = cq.from(jpaEntity.getTypeClass());
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

  /**
   * Create a criteria builder sub query based on options provided in the constructor. The method is intend to be used
   * by $expand implementation.
   * @param superordinateQuery Query that uses the created sub query within an EXIST where clause
   * @param assoziation Navigation target
   * @return
   * @throws ODataApplicationException
   */
  @Override
  protected <T extends Object> Subquery<T> asSubQuery(JPAAbstractQuery superordinateQuery,
      JPAAssociationPath assoziation)
          throws ODataApplicationException {

    JPANavigationQuery subQuery = new JPANavigationQuery(sd, (UriResourcePartTyped) uriResource.getUriResourceParts()
        .get(0), superordinateQuery, em, assoziation);

    return subQuery.getSubQueryExists(null);
  }

  @Override
  protected List<JPANavigationQuery> asSubQueries(JPAAbstractQuery superordinateQuery,
      JPAAssociationPath assoziation) throws ODataApplicationException {
    List<JPANavigationQuery> subQueries = new ArrayList<JPANavigationQuery>();

    JPANavigationQuery subQuery = new JPANavigationQuery(sd, (UriResourcePartTyped) uriResource.getUriResourceParts()
        .get(0), superordinateQuery, em, assoziation);

    subQueries.add(subQuery);
    return subQueries;
  }

  /**
   * Applies the $skip and $top options of the OData request to the query. The values are defined as follows:
   * <ul>
   * <li> The $top system query option specifies a non-negative integer n that limits the number of items returned from
   * a collection.
   * <li> The $skip system query option specifies a non-negative integer n that excludes the first n items of the
   * queried collection from the result.
   * </ul>
   * For details see:
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398306"
   * >OData Version 4.0 Part 1 - 11.2.5.3 System Query Option $top</a>
   * 
   * @throws ODataApplicationException
   */
  private void addTopSkip(TypedQuery<Tuple> tq) throws ODataApplicationException {
    /*
     * Where $top and $skip are used together, $skip MUST be applied before $top, regardless of the order in which they
     * appear in the request.
     * If no unique ordering is imposed through an $orderby query option, the service MUST impose a stable ordering
     * across requests that include $skip.
     * 
     * URL example: http://localhost:8080/BuPa/BuPa.svc/Organizations?$count=true&$skip=5
     */

    TopOption topOption = uriResource.getTopOption();
    if (topOption != null) {
      int topNumber = topOption.getValue();
      if (topNumber >= 0)
        tq.setMaxResults(topNumber);
      else
        throw new ODataApplicationException("Invalid value for $top", HttpStatusCode.BAD_REQUEST.getStatusCode(),
            Locale.ROOT);
    }

    SkipOption skipOption = uriResource.getSkipOption();
    if (skipOption != null) {
      int skipNumber = skipOption.getValue();
      if (skipNumber >= 0)
        tq.setFirstResult(skipNumber);
      else
        throw new ODataApplicationException("Invalid value for $skip", HttpStatusCode.BAD_REQUEST.getStatusCode(),
            Locale.ROOT);
    }
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
