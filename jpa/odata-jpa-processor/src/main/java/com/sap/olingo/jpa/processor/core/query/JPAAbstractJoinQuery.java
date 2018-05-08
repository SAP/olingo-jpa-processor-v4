package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterCrossComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAOperationConverter;

public abstract class JPAAbstractJoinQuery extends JPAAbstractQuery {
  protected static final String ALIAS_SEPERATOR = ".";
  protected final UriInfoResource uriResource;
  protected final CriteriaQuery<Tuple> cq;
  protected Root<?> root;
  protected From<?, ?> target;
  protected final JPAODataSessionContextAccess context;
  protected List<JPANavigationProptertyInfo> navigationInfo;
  protected final JPAODataPage page;

  public JPAAbstractJoinQuery(final OData odata, final JPAODataSessionContextAccess context,
      final JPAEntityType jpaEntityType, final EntityManager em, final Map<String, List<String>> requestHeaders,
      final UriInfoResource uriResource, final JPAODataPage page) throws ODataException {

    super(odata, context.getEdmProvider().getServiceDocument(), jpaEntityType, em, context.getDebugger());
    this.locale = ExpressionUtil.determineLocale(requestHeaders);
    this.uriResource = uriResource;
    this.cq = cb.createTupleQuery();
    this.context = context;
    this.page = page;
  }

  @Override
  public AbstractQuery<?> getQuery() {
    return cq;
  }

  @Override
  public From<?, ?> getRoot() {
    return target;
  }

  /**
   * Applies the $skip and $top options of the OData request to the query. The values are defined as follows:
   * <ul>
   * <li> The $top system query option specifies a non-negative integer n that limits the number of items returned from
   * a collection.
   * <li> The $skip system query option specifies a non-negative integer n that excludes the first n items of the
   * queried collection from the result.
   * </ul>
   * These values can be restricted by a page provided by server driven paging<p>
   * For details see:
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398306"
   * >OData Version 4.0 Part 1 - 11.2.5.3 System Query Option $top</a> and
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Server-Driven_Paging"
   * >OData Version 4.0 Part 1 - 11.2.5.7 Server-Driven Paging</a>
   * 
   * @throws ODataApplicationException
   */
  protected void addTopSkip(final TypedQuery<Tuple> tq) throws ODataApplicationException {
    /*
     * Where $top and $skip are used together, $skip MUST be applied before $top, regardless of the order in which they
     * appear in the request.
     * If no unique ordering is imposed through an $orderby query option, the service MUST impose a stable ordering
     * across requests that include $skip.
     * 
     * URL example: http://localhost:8080/BuPa/BuPa.svc/Organizations?$count=true&$skip=5
     */

    final TopOption topOption = uriResource.getTopOption();
    if (topOption != null || page != null) {
      int topNumber = topOption != null ? topOption.getValue() : page.getTop();
      topNumber = topOption != null && page != null ? Math.min(topOption.getValue(), page.getTop())
          : topNumber;
      if (topNumber >= 0)
        tq.setMaxResults(topNumber);
      else
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_INVALID_VALUE,
            HttpStatusCode.BAD_REQUEST, Integer.toString(topNumber), "$top");
    }

    final SkipOption skipOption = uriResource.getSkipOption();
    if (skipOption != null || page != null) {
      int skipNumber = skipOption != null ? skipOption.getValue() : page.getSkip();
      skipNumber = skipOption != null && page != null ? Math.max(skipOption.getValue(), page.getSkip()) : skipNumber;
      if (skipNumber >= 0)
        tq.setFirstResult(skipNumber);
      else
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_INVALID_VALUE,
            HttpStatusCode.BAD_REQUEST, Integer.toString(skipNumber), "$skip");
    }
  }

  protected List<JPAPath> buildEntityPathList(final JPAEntityType jpaEntity) throws ODataApplicationException {

    try {
      return jpaEntity.getPathList();
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  /**
   * Creates the path to all properties that need to be selected from the database. A Property can be included for the
   * following reasons:
   * <ul>
   * <li>It is a key in order to be able to build the links</li>
   * <li>It is part of the $select system query option</li>
   * <li>It is the result of a navigation, which my be restricted by a $select</li>
   * <li>If is required to link $expand with result with the parent result</li>
   * <li>A stream is requested and the property contains the mime type</>
   * </ul>
   * Not included are collection properties.
   * @param uriResource
   * @return
   * @throws ODataApplicationException
   */
  protected List<JPAPath> buildSelectionPathList(final UriInfoResource uriResource)
      throws ODataApplicationException {
    // TODO It is also possible to request all actions or functions available for each returned entity:
    // http://host/service/Products?$select=DemoService.*

    final List<JPAPath> jpaPathList = new ArrayList<>();
    final SelectOption select = uriResource.getSelectOption();
    try {
      buildSelectionAddNavigationAndSelect(uriResource, jpaPathList, select);
      buildSelectionAddMimeType(jpaEntity, jpaPathList);
      buildSelectionAddKeys(jpaEntity, jpaPathList);
      buildSelectionAddExpandSelection(uriResource, jpaPathList);
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR
          .getStatusCode(), ODataJPAModelException.getLocales().nextElement(), e);
    }
    return jpaPathList;
  }

  /**
   * 
   * @param orderByTarget
   * @param descriptionFields List of the requested fields that of type description
   * @param query
   * @param queryRoot
   * @return
   * @throws ODataApplicationException
   */
  protected Map<String, From<?, ?>> createFromClause(final List<JPAAssociationPath> orderByTarget,
      final List<JPAPath> descriptionFields, CriteriaQuery<?> query) throws ODataApplicationException {

    final HashMap<String, From<?, ?>> joinTables = new HashMap<>();
    // 1. Create navigation joins
    try {
      final JPAEntityType sourceEt = this.navigationInfo.get(0).getEntityType();
      this.root = query.from(sourceEt.getTypeClass());
      joinTables.put(sourceEt.getInternalName(), root);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

    target = root;
    for (int i = 0; i < this.navigationInfo.size() - 1; i++) {
      final JPANavigationProptertyInfo naviInfo = this.navigationInfo.get(i);

      EdmType castType = null;
      if (naviInfo.getUriResiource() instanceof UriResourceNavigation)
        castType = ((UriResourceNavigation) naviInfo.getUriResiource()).getTypeFilterOnEntry();
      else
        castType = ((UriResourceEntitySet) naviInfo.getUriResiource()).getTypeFilterOnEntry();
      if (castType != null)
        target = (From<?, ?>) target.as(sd.getEntity(castType.getFullQualifiedName()).getTypeClass());
      naviInfo.setFromClause(target);
      if (naviInfo.getUriInfo() != null && naviInfo.getUriInfo().getFilterOption() != null) {
        try {
          naviInfo.setFilterCompiler(new JPAFilterCrossComplier(odata, sd, em, naviInfo.getEntityType(),
              new JPAOperationConverter(cb, context.getOperationConverter()), naviInfo.getUriInfo(), this, naviInfo
                  .getFromClause()));
        } catch (ODataJPAModelException e) {
          throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
              HttpStatusCode.BAD_REQUEST, e);
        }
      }
      target = createJoinFromPath(naviInfo.getAssociationPath().getAlias(), naviInfo.getAssociationPath().getPath(),
          target, JoinType.INNER);
      joinTables.put(naviInfo.getAssociationPath().getAlias(), target);
    }
    final JPANavigationProptertyInfo lastInfo = this.navigationInfo.get(this.navigationInfo.size() - 1);
    try {
      if (lastInfo.getAssociationPath() != null
          && lastInfo.getAssociationPath().getLeaf() instanceof JPACollectionAttribute
          && !uriResource.getUriResourceParts().isEmpty()
          && uriResource.getUriResourceParts().get(uriResource.getUriResourceParts().size() - 1)
              .getKind() == UriResourceKind.complexProperty) {
        Path<?> p = target;
        JPAElement element = null;
        for (JPAElement pathElement : lastInfo.getAssociationPath().getPath()) {
          p = p.get(pathElement.getInternalName());
          element = pathElement;
        }
        joinTables.put(lastInfo.getAssociationPath().getAlias(), (From<?, ?>) p);
        lastInfo.setFilterCompiler(new JPAFilterCrossComplier(odata, sd, em,
            (JPAEntityType) ((JPAAssociationAttribute) element).getTargetEntity(), new JPAOperationConverter(cb,
                context.getOperationConverter()), uriResource, this, (From<?, ?>) p));
      } else
        lastInfo.setFilterCompiler(new JPAFilterCrossComplier(odata, sd, em, jpaEntity, new JPAOperationConverter(cb,
            context.getOperationConverter()), uriResource, this, lastInfo.getAssociationPath()));
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
          HttpStatusCode.BAD_REQUEST, e);
    }

    lastInfo.setFromClause(target);

    // 2. OrderBy navigation property
    for (final JPAAssociationPath orderBy : orderByTarget) {
      From<?, ?> join = target;
      for (JPAElement o : orderBy.getPath())
        join = join.join(o.getInternalName(), JoinType.LEFT);
      // Take on condition from JPA metadata; no explicit on
      joinTables.put(orderBy.getAlias(), join);
    }

    // 3. Description Join determine
    for (JPANavigationProptertyInfo info : this.navigationInfo) {
      if (info.getFilterCompiler() != null) {
        generateDesciptionJoin(joinTables,
            determineAllDescriptionPath(info.getFromClause() == target ? descriptionFields : new ArrayList<>(1),
                info.getFilterCompiler()), info.getFromClause());
      }
    }
    return joinTables;
  }

  protected final javax.persistence.criteria.Expression<Boolean> createKeyWhere(
      final List<JPANavigationProptertyInfo> info) throws ODataApplicationException {

    javax.persistence.criteria.Expression<Boolean> whereCondition = null;
    // Given key: Organizations('1')/Roles(...)
    for (JPANavigationProptertyInfo naviInfo : info) {
      if (naviInfo.getKeyPredicates() != null) {
        try {
          final JPAEntityType et = naviInfo.getEntityType();

          final From<?, ?> f = naviInfo.getFromClause();
          final List<UriParameter> keyPredicates = naviInfo.getKeyPredicates();
          whereCondition = createWhereByKey(f, whereCondition, keyPredicates, et);
        } catch (ODataJPAModelException e) {
          throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
      }
    }
    return whereCondition;
  }

  /**
   * If asc or desc is not specified, the service MUST order by the specified property in ascending order.
   * See: <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398305"
   * >OData Version 4.0 Part 1 - 11.2.5.2 System Query Option $orderby</a> <p>
   * 
   * @throws ODataJPAModelException
   * 
   */
  protected List<Order> createOrderByList(Map<String, From<?, ?>> joinTables, OrderByOption orderByOption)
      throws ODataApplicationException {
    // .../Organizations?$orderby=Address/Country --> one item, two resourcePaths
    // [...ComplexProperty,...PrimitiveProperty]
    // .../Organizations?$orderby=Roles/$count --> one item, two resourcePaths [...NavigationProperty,...Count]
    // .../Organizations?$orderby=Roles/$count desc,Address/Country asc -->two items
    //
    // SQL example to order by number of entities of the
    // SELECT t0."BusinessPartnerID" ,COUNT(t1."BusinessPartnerID")
    // FROM {oj "OLINGO"."org.apache.olingo.jpa::BusinessPartner" t0
    // LEFT OUTER JOIN "OLINGO"."org.apache.olingo.jpa::BusinessPartnerRole" t1
    // ON (t1."BusinessPartnerID" = t0."BusinessPartnerID")} //NOSONAR
    // WHERE (t0."Type" = ?)
    // GROUP BY t0."BusinessPartnerID"
    // ORDER BY COUNT(t1."BusinessPartnerID") DESC

    // TODO Functions and orderBy: Part 1 - 11.5.3.1 Invoking a Function

    final int handle = debugger.startRuntimeMeasurement(this, "createOrderByList");
    final List<Order> orders = new ArrayList<>();
    if (orderByOption != null) {
      try {
        for (final OrderByItem orderByItem : orderByOption.getOrders()) {
          final Expression expression = orderByItem.getExpression();
          if (expression instanceof Member) {
            final UriInfoResource resourcePath = ((Member) expression).getResourcePath();
            JPAStructuredType type = jpaEntity;
            Path<?> p = target;
            StringBuilder externalPath = new StringBuilder();
            for (final UriResource uriResourceItem : resourcePath.getUriResourceParts()) {
              if (uriResourceItem instanceof UriResourcePrimitiveProperty
                  && !((UriResourceProperty) uriResourceItem).isCollection()) {
                p = p.get(type.getAttribute((UriResourceProperty) uriResourceItem).getInternalName());
                addOrderByExpression(orders, orderByItem, p);
              } else if (uriResourceItem instanceof UriResourceComplexProperty
                  && !((UriResourceProperty) uriResourceItem).isCollection()) {
                final JPAAttribute attribute = type.getAttribute((UriResourceProperty) uriResourceItem);
                addPathElement(externalPath, attribute);
                p = p.get(attribute.getInternalName());
                type = attribute.getStructuredType();
              } else if (uriResourceItem instanceof UriResourceNavigation
                  || (uriResourceItem instanceof UriResourceProperty
                      && ((UriResourceProperty) uriResourceItem).isCollection())) {

                if (uriResourceItem instanceof UriResourceNavigation)
                  externalPath.append(((UriResourceNavigation) uriResourceItem).getProperty().getName());
                else
                  externalPath.append(((UriResourceProperty) uriResourceItem).getProperty().getName());
                From<?, ?> join = joinTables.get(externalPath.toString());
                addOrderByExpression(orders, orderByItem, cb.count(join));
              }
            }
          }
        }
      } catch (ODataJPAModelException e) {
        debugger.stopRuntimeMeasurement(handle);
        throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
      }
    }
    debugger.stopRuntimeMeasurement(handle);
    return orders;
  }

  /**
   * The value of the $select query option is a comma-separated list of <b>properties</b>, qualified action names,
   * qualified function names, the <b>star operator (*)</b>, or the star operator prefixed with the namespace or alias
   * of the schema in order to specify all operations defined in the schema. See:
   * <a
   * href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398297"
   * >OData Version 4.0 Part 1 - 11.2.4.1 System Query Option $select</a> <p>
   * See also:
   * <a
   * href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398163"
   * >OData Version 4.0 Part 2 - 5.1.3 System Query Option $select</a>
   * 
   * @param joinTables
   * @param jpaPathList
   * @return
   * @throws ODataApplicationException
   */
  protected List<Selection<?>> createSelectClause(final Map<String, From<?, ?>> joinTables, // NOSONAR
      final List<JPAPath> jpaPathList, final From<?, ?> target) throws ODataApplicationException { // NOSONAR Allow
    // subclasses to throw an exception

    final int handle = debugger.startRuntimeMeasurement(this, "createSelectClause");
    final List<Selection<?>> selections = new ArrayList<>();

    // Build select clause
    for (final JPAPath jpaPath : jpaPathList) {
      final Path<?> p = ExpressionUtil.convertToCriteriaPath(joinTables, target, jpaPath.getPath());
      p.alias(jpaPath.getAlias());
      selections.add(p);
    }
    debugger.stopRuntimeMeasurement(handle);
    return selections;
  }

  protected javax.persistence.criteria.Expression<Boolean> createWhere(final UriInfoResource uriInfo,
      final List<JPANavigationProptertyInfo> navigationInfo) throws ODataApplicationException {

    final int handle = debugger.startRuntimeMeasurement(this, "createWhere");
    javax.persistence.criteria.Expression<Boolean> whereCondition = null;
    // Given keys: Organizations('1')/Roles(...)
    try {
      whereCondition = createKeyWhere(navigationInfo);
    } catch (ODataApplicationException e) {
      debugger.stopRuntimeMeasurement(handle);
      throw e;
    }

    // http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398301
    // http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398094
    // https://tools.oasis-open.org/version-control/browse/wsvn/odata/trunk/spec/ABNF/odata-abnf-construction-rules.txt
    try {
      whereCondition = addWhereClause(whereCondition, navigationInfo.get(navigationInfo.size() - 1).getFilterCompiler()
          .compile());
    } catch (ExpressionVisitException e) {
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
          HttpStatusCode.BAD_REQUEST, e);
    }

    if (uriInfo.getSearchOption() != null && uriInfo.getSearchOption().getSearchExpression() != null)
      whereCondition = addWhereClause(whereCondition,
          context.getDatabaseProcessor().createSearchWhereClause(cb, this.cq, target, jpaEntity, uriInfo
              .getSearchOption()));

    debugger.stopRuntimeMeasurement(handle);
    return whereCondition;
  }

  protected JPAAssociationPath determineAssoziation(final UriResourcePartTyped naviStart,
      final StringBuilder associationName) throws ODataApplicationException {

    JPAEntityType naviStartType;
    try {
      if (naviStart instanceof UriResourceEntitySet)
        naviStartType = sd.getEntity(((UriResourceEntitySet) naviStart).getType());
      else
        naviStartType = sd.getEntity(((UriResourceNavigation) naviStart).getProperty().getType());
      return naviStartType.getAssociationPath(associationName.toString());
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  protected void expandPath(final JPAEntityType jpaEntity, final List<JPAPath> jpaPathList, final String selectItem,
      final boolean targetIsCollection)
      throws ODataJPAModelException, ODataJPAQueryException {

    final JPAPath selectItemPath = jpaEntity.getPath(selectItem);
    if (selectItemPath == null)
      throw new ODataJPAQueryException(MessageKeys.QUERY_PREPARATION_INVALID_SELECTION_PATH,
          HttpStatusCode.BAD_REQUEST);
    if (selectItemPath.getLeaf().isComplex()) {
      // Complex Type
      final List<JPAPath> c = jpaEntity.searchChildPath(selectItemPath);
      if (targetIsCollection)
        jpaPathList.addAll(c);
      else
        copyNonCollectionProperties(jpaPathList, c);
    } else // Primitive Type
    if (!selectItemPath.getLeaf().isCollection() || targetIsCollection)
      jpaPathList.add(selectItemPath);
  }

  protected List<JPAPath> extractDescriptionAttributes(final List<JPAPath> jpaPathList) {

    final List<JPAPath> result = new ArrayList<>();
    for (final JPAPath p : jpaPathList)
      if (p.getLeaf() instanceof JPADescriptionAttribute)
        result.add(p);
    return result;
  }

  @Override
  protected Locale getLocale() {
    return locale;
  }

  @Override
  JPAODataSessionContextAccess getContext() {
    return context;
  }

  boolean hasNavigation(final List<UriResource> uriResourceParts) {
    if (uriResourceParts != null) {
      for (int i = uriResourceParts.size() - 1; i >= 0; i--) {
        if (uriResourceParts.get(i) instanceof UriResourceNavigation)
          return true;
      }
    }
    return false;
  }

  private void addOrderByExpression(final List<Order> orders, final OrderByItem orderByItem,
      javax.persistence.criteria.Expression<?> expression) {

    if (orderByItem.isDescending())
      orders.add(cb.desc(expression));
    else
      orders.add(cb.asc(expression));
  }

  private void addPathElement(StringBuilder externalPath, JPAAttribute attribute) {
    externalPath.append(attribute.getExternalName());
    externalPath.append(JPAPath.PATH_SEPERATOR);

  }

  // Only for streams e.g. .../OrganizationImages('9')/$value
  private List<JPAPath> buildPathValue(final JPAEntityType jpaEntity)
      throws ODataApplicationException {

    List<JPAPath> jpaPathList = new ArrayList<>();
    try {
      // Stream value
      jpaPathList.add(jpaEntity.getStreamAttributePath());
      jpaPathList.addAll(jpaEntity.getKeyPath());

    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return jpaPathList;
  }

  /**
   * In order to be able to link the result of a expand query with the super-ordinate query it is necessary to ensure
   * that the join columns are selected.<br>
   * The same columns are required for the count query, for select as well as order by.
   * @param uriResource
   * @param jpaPathList
   * @throws ODataApplicationException
   * @throws ODataJPAQueryException
   */
  private void buildSelectionAddExpandSelection(final UriInfoResource uriResource, List<JPAPath> jpaPathList)
      throws ODataApplicationException {

    final Map<JPAExpandItem, JPAAssociationPath> associationPathList = Util.determineAssoziations(sd, uriResource
        .getUriResourceParts(), uriResource.getExpandOption());
    if (!associationPathList.isEmpty()) {
      final List<JPAPath> tmpPathList = new ArrayList<>(jpaPathList);
      final List<JPAPath> addPathList = new ArrayList<>();

      Collections.sort(tmpPathList);
      for (final Entry<JPAExpandItem, JPAAssociationPath> item : associationPathList.entrySet()) {
        final JPAAssociationPath associationPath = item.getValue();
        try {
          for (final JPAPath joinItem : associationPath.getLeftColumnsList()) {
            final int pathIndex = Collections.binarySearch(tmpPathList, joinItem);
            final int insertIndex = Collections.binarySearch(addPathList, joinItem);
            if (pathIndex < 0 && insertIndex < 0)
              addPathList.add(Math.abs(insertIndex) - 1, joinItem);
          }
        } catch (ODataJPAModelException e) {
          throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
        }
      }
      jpaPathList.addAll(addPathList);
    }
  }

  private void buildSelectionAddKeys(final JPAEntityType jpaEntity, final List<JPAPath> jpaPathList)
      throws ODataJPAModelException {

    final List<? extends JPAAttribute> jpaKeyList = new ArrayList<>(jpaEntity.getKey());

    for (JPAPath selectItemPath : jpaPathList) {
      for (int i = 0; i < jpaKeyList.size(); i++) {
        JPAAttribute key = jpaKeyList.get(i);
        if (key.getExternalFQN().equals(selectItemPath.getLeaf().getExternalFQN()))
          jpaKeyList.remove(i);
      }
      if (jpaKeyList.isEmpty())
        break;
    }
    for (final JPAAttribute key : jpaKeyList) {
      jpaPathList.add(jpaEntity.getPath(key.getExternalName()));
    }
  }

  private void buildSelectionAddMimeType(final JPAEntityType jpaEntity, final List<JPAPath> jpaPathList)
      throws ODataJPAModelException {

    if (jpaEntity.hasStream()) {
      final JPAPath mimeTypeAttribute = jpaEntity.getContentTypeAttributePath();
      if (mimeTypeAttribute != null) {
        jpaPathList.add(mimeTypeAttribute);
      }
    }
  }

  private void buildSelectionAddNavigationAndSelect(final UriInfoResource uriResource, final List<JPAPath> jpaPathList,
      final SelectOption select) throws ODataApplicationException, ODataJPAModelException {

    final UriResource last = !uriResource.getUriResourceParts().isEmpty() ? uriResource.getUriResourceParts().get(
        uriResource.getUriResourceParts().size() - 1) : null;
    final boolean targetIsCollection = (last != null && last instanceof UriResourceProperty
        && ((UriResourceProperty) last).isCollection());
    final String pathPrefix = Util.determineProptertyNavigationPrefix(uriResource.getUriResourceParts());

    if (Util.VALUE_RESOURCE.equals(pathPrefix))
      jpaPathList.addAll(buildPathValue(jpaEntity));
    else if (select == null || select.getSelectItems().isEmpty() || select.getSelectItems().get(0).isStar()) {
      if (pathPrefix == null || pathPrefix.isEmpty())
        copyNonCollectionProperties(jpaPathList, buildEntityPathList(jpaEntity));
      else {
        expandPath(jpaEntity, jpaPathList, pathPrefix, targetIsCollection);
      }
    } else {
      for (SelectItem sItem : select.getSelectItems()) {
        String pathItem = sItem.getResourcePath().getUriResourceParts().stream().map(path -> (path
            .getSegmentValue())).collect(Collectors.joining(JPAPath.PATH_SEPERATOR));
        expandPath(jpaEntity, jpaPathList, pathPrefix.isEmpty() ? pathItem : pathPrefix + "/" + pathItem,
            targetIsCollection);
      }
    }
  }

  /**
   * Skips all those properties that are or belong to a collection property. E.g
   * (Organization)Comment or (Person)InhouseAddress/Room
   * @param jpaPathList
   * @param c
   */
  private void copyNonCollectionProperties(final List<JPAPath> jpaPathList, final List<JPAPath> c) {
    for (JPAPath p : c) {
      boolean skip = false;
      for (JPAElement pathElement : p.getPath()) {
        if (pathElement instanceof JPAAttribute && ((JPAAttribute) pathElement).isCollection()) {
          skip = true;
          break;
        }
      }
      if (!skip)
        jpaPathList.add(p);
    }
  }

  private Set<JPAPath> determineAllDescriptionPath(List<JPAPath> descriptionFields, JPAFilterComplier filter)
      throws ODataApplicationException {

    Set<JPAPath> allPath = new HashSet<>(descriptionFields);
    for (JPAPath path : filter.getMember()) {
      if (path.getLeaf() instanceof JPADescriptionAttribute)
        allPath.add(path);
    }
    return allPath;
  }
}
