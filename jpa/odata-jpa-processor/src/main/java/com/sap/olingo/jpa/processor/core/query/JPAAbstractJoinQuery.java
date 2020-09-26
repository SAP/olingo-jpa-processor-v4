package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_RESULT_ENTITY_TYPE_ERROR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
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
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterCrossComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAOperationConverter;
import com.sap.olingo.jpa.processor.core.processor.JPAODataRequestContextImpl;

public abstract class JPAAbstractJoinQuery extends JPAAbstractQuery implements JPAQuery {
  protected static final String ALIAS_SEPERATOR = ".";
  protected final UriInfoResource uriResource;
  protected final CriteriaQuery<Tuple> cq;
  protected Root<?> root;
  protected From<?, ?> target;
  protected final JPAODataCRUDContextAccess context;
  protected final JPAODataPage page;
  protected final List<JPANavigationProptertyInfo> navigationInfo;
  protected final JPANavigationProptertyInfo lastInfo;
  protected final JPAODataRequestContextAccess requestContext;

  public JPAAbstractJoinQuery(final OData odata, final JPAODataCRUDContextAccess sessionContext,
      final JPAEntityType jpaEntityType, final JPAODataRequestContextAccess requestContext,
      final Map<String, List<String>> requestHeaders, final List<JPANavigationProptertyInfo> navigationInfo)
      throws ODataException {

    this(odata, sessionContext, jpaEntityType, requestContext.getUriInfo(), requestContext, requestHeaders,
        navigationInfo);
  }

  protected JPAAbstractJoinQuery(final OData odata, final JPAODataCRUDContextAccess sessionContext,
      final JPAEntityType jpaEntityType, final UriInfoResource uriInfo,
      final JPAODataRequestContextAccess requestContext, final Map<String, List<String>> requestHeaders,
      final List<JPANavigationProptertyInfo> navigationInfo) throws ODataException {

    super(odata, sessionContext.getEdmProvider().getServiceDocument(), jpaEntityType, requestContext);
    this.requestContext = requestContext;
    this.locale = ExpressionUtil.determineLocale(requestHeaders);
    this.uriResource = uriInfo;
    this.cq = cb.createTupleQuery();
    this.context = sessionContext;
    this.page = requestContext.getPage();
    this.navigationInfo = navigationInfo;
    this.lastInfo = determineLastInfo(navigationInfo);
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
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  protected final void buildSelectionAddNavigationAndSelect(final UriInfoResource uriResource,
      final Set<JPAPath> jpaPathList, final SelectOption select) throws ODataApplicationException,
      ODataJPAModelException {

    final boolean targetIsCollection = determineTargetIsCollection(uriResource);
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
      convertSelectIntoPath(select, jpaPathList, targetIsCollection, pathPrefix);
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
  protected Set<JPAPath> buildSelectionPathList(final UriInfoResource uriResource)
      throws ODataApplicationException {
    // TODO It is also possible to request all actions or functions available for each returned entity:
    // http://host/service/Products?$select=DemoService.*

    final Set<JPAPath> jpaPathList = new HashSet<>();
    final SelectOption select = uriResource.getSelectOption();
    try {
      buildSelectionAddNavigationAndSelect(uriResource, jpaPathList, select);
      buildSelectionAddMimeType(jpaEntity, jpaPathList);
      buildSelectionAddKeys(jpaEntity, jpaPathList);
      buildSelectionAddExpandSelection(uriResource, jpaPathList);
      buildSelectionAddETag(jpaEntity, jpaPathList);
    } catch (final ODataJPAModelException e) {
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
   * @param lastInfo
   * @param queryRoot
   * @return
   * @throws ODataApplicationException
   * @throws JPANoSelectionException
   */
  protected Map<String, From<?, ?>> createFromClause(final List<JPAAssociationPath> orderByTarget,
      final Collection<JPAPath> selectionPath, final CriteriaQuery<?> query, final JPANavigationProptertyInfo lastInfo)
      throws ODataApplicationException, JPANoSelectionException {

    final HashMap<String, From<?, ?>> joinTables = new HashMap<>();
    // 1. Create navigation joins
    createFromClauseRoot(query, joinTables);
    target = root;
    createFromClauseNavigationJoins(joinTables);
    createFromClauseCollectionsJoins(joinTables);
    // 2. OrderBy navigation property
    createFromClauseOrderBy(orderByTarget, joinTables);
    // 3. Description Join determine
    createFromClauseDescriptionFields(selectionPath, joinTables);
    // 4. Collection Attribute Joins
    generateCollectionAttributeJoin(joinTables, selectionPath, lastInfo);

    return joinTables;
  }

  protected final javax.persistence.criteria.Expression<Boolean> createKeyWhere(
      final List<JPANavigationProptertyInfo> info) throws ODataApplicationException {

    javax.persistence.criteria.Expression<Boolean> whereCondition = null;
    // Given key: Organizations('1')/Roles(...)
    for (final JPANavigationProptertyInfo naviInfo : info) {
      if (naviInfo.getKeyPredicates() != null) {
        try {
          final JPAEntityType et = naviInfo.getEntityType();

          final From<?, ?> f = naviInfo.getFromClause();
          final List<UriParameter> keyPredicates = naviInfo.getKeyPredicates();
          whereCondition = createWhereByKey(f, whereCondition, keyPredicates, et);
        } catch (final ODataJPAModelException e) {
          throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
      }
    }
    return whereCondition;
  }

  protected javax.persistence.criteria.Expression<Boolean> createProtectionWhere(
      final Optional<JPAODataClaimProvider> claimsProvider) throws ODataJPAQueryException {

    javax.persistence.criteria.Expression<Boolean> restriction = null;
    for (final JPANavigationProptertyInfo navi : navigationInfo) { // for all participating entity types/tables
      try {
        final JPAEntityType et = navi.getEntityType();
        final From<?, ?> from = navi.getFromClause();
        restriction = addWhereClause(restriction, createProtectionWhereForEntityType(claimsProvider, et, from));
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(QUERY_RESULT_ENTITY_TYPE_ERROR, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    }
    return restriction;
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
   * @param requestedProperties
   * @param optional
   * @return
   * @throws ODataApplicationException
   */
  protected List<Selection<?>> createSelectClause(final Map<String, From<?, ?>> joinTables, // NOSONAR
      final Collection<JPAPath> requestedProperties, final From<?, ?> target, final List<String> groups)
      throws ODataApplicationException { // NOSONAR Allow subclasses to throw an exception

    final int handle = debugger.startRuntimeMeasurement(this, "createSelectClause");
    final List<Selection<?>> selections = new ArrayList<>();

    // Build select clause
    for (final JPAPath jpaPath : requestedProperties) {
      if (jpaPath.isPartOfGroups(groups)) {
        final Path<?> p = ExpressionUtil.convertToCriteriaPath(joinTables, target, jpaPath.getPath());
        p.alias(jpaPath.getAlias());
        selections.add(p);
      }
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
    } catch (final ODataApplicationException e) {
      debugger.stopRuntimeMeasurement(handle);
      throw e;
    }

    // http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398301
    // http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398094
    // https://tools.oasis-open.org/version-control/browse/wsvn/odata/trunk/spec/ABNF/odata-abnf-construction-rules.txt
    try {
      whereCondition = addWhereClause(whereCondition, navigationInfo.get(navigationInfo.size() - 1).getFilterCompiler()
          .compile());
    } catch (final ExpressionVisitException e) {
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
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  protected JPANavigationProptertyInfo determineLastInfo(final List<JPANavigationProptertyInfo> naviInfo) {
    return naviInfo.isEmpty() ? null : naviInfo.get(naviInfo.size() - 1);
  }

  protected final boolean determineTargetIsCollection(final UriInfoResource uriResource) {

    final UriResource last = !uriResource.getUriResourceParts().isEmpty() ? uriResource.getUriResourceParts().get(
        uriResource.getUriResourceParts().size() - 1) : null;
    return (last instanceof UriResourceProperty && ((UriResourceProperty) last).isCollection());
  }

  protected void expandPath(final JPAEntityType jpaEntity, final Collection<JPAPath> jpaPathList,
      final String selectItem, final boolean targetIsCollection) throws ODataJPAModelException, ODataJPAQueryException {

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
    } else if (!selectItemPath.getLeaf().isCollection() || targetIsCollection) {// Primitive Type
      jpaPathList.add(selectItemPath);
    }
  }

  protected List<JPAPath> extractDescriptionAttributes(final Collection<JPAPath> jpaPathList) {

    final List<JPAPath> result = new ArrayList<>();
    for (final JPAPath p : jpaPathList)
      if (p.getLeaf() instanceof JPADescriptionAttribute)
        result.add(p);
    return result;
  }

  /*
   * Create the join condition for a collection property. This attribute can be part of structure type, therefore the
   * path to the collection property needs to be traversed
   */
  protected void generateCollectionAttributeJoin(final Map<String, From<?, ?>> joinTables,
      final Collection<JPAPath> jpaPathList, final JPANavigationProptertyInfo lastInfo) throws JPANoSelectionException,
      ODataJPAProcessorException {

    for (final JPAPath path : jpaPathList) {
      // 1. check if path contains collection attribute
      final JPAElement collection = findCollection(lastInfo, path);
      // 2. Check if join exists and create join if not
      addCollection(joinTables, path, collection);
    }
  }

  @Override
  protected Locale getLocale() {
    return locale;
  }

  @Override
  JPAODataCRUDContextAccess getContext() {
    return context;
  }

  private void addCollection(final Map<String, From<?, ?>> joinTables, final JPAPath path,
      final JPAElement collection) {

    if (collection != null && !joinTables.containsKey(collection.getExternalName())) {
      From<?, ?> f = target;
      for (final JPAElement element : path.getPath()) {
        f = f.join(element.getInternalName());
        if (element instanceof JPACollectionAttribute) {
          break;
        }
      }
      joinTables.put(collection.getExternalName(), f);
    }
  }

  // Only for streams e.g. .../OrganizationImages('9')/$value
  private List<JPAPath> buildPathValue(final JPAEntityType jpaEntity)
      throws ODataApplicationException {

    final List<JPAPath> jpaPathList = new ArrayList<>();
    try {
      // Stream value
      jpaPathList.add(jpaEntity.getStreamAttributePath());
      jpaPathList.addAll(jpaEntity.getKeyPath());

    } catch (final ODataJPAModelException e) {
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
  private void buildSelectionAddExpandSelection(final UriInfoResource uriResource,
      final Collection<JPAPath> jpaPathList)
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
        } catch (final ODataJPAModelException e) {
          throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
        }
      }
      jpaPathList.addAll(addPathList);
    }
  }

  private void buildSelectionAddETag(final JPAEntityType jpaEntity, final Collection<JPAPath> jpaPathList)
      throws ODataJPAModelException {
    if (jpaEntity.hasEtag())
      jpaPathList.add(jpaEntity.getEtagPath());

  }

  private void buildSelectionAddKeys(final JPAEntityType jpaEntity, final Collection<JPAPath> jpaPathList)
      throws ODataJPAModelException {

    final List<? extends JPAAttribute> jpaKeyList = new ArrayList<>(jpaEntity.getKey());

    for (final JPAPath selectItemPath : jpaPathList) {
      for (int i = 0; i < jpaKeyList.size(); i++) {
        final JPAAttribute key = jpaKeyList.get(i);
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

  private void buildSelectionAddMimeType(final JPAEntityType jpaEntity, final Collection<JPAPath> jpaPathList)
      throws ODataJPAModelException {

    if (jpaEntity.hasStream()) {
      final JPAPath mimeTypeAttribute = jpaEntity.getContentTypeAttributePath();
      if (mimeTypeAttribute != null) {
        jpaPathList.add(mimeTypeAttribute);
      }
    }
  }

  private boolean checkCollectionIsPartOfGroup(final String collectionPath) throws ODataJPAProcessorException {

    try {
      final JPAPath path = jpaEntity.getPath(collectionPath);
      return path.isPartOfGroups(groups);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private void convertSelectIntoPath(final SelectOption select, final Collection<JPAPath> jpaPathList,
      final boolean targetIsCollection, final String pathPrefix) throws ODataJPAModelException, ODataJPAQueryException {

    for (final SelectItem sItem : select.getSelectItems()) {
      final String pathItem = sItem.getResourcePath().getUriResourceParts().stream().map(path -> (path
          .getSegmentValue())).collect(Collectors.joining(JPAPath.PATH_SEPERATOR));
      expandPath(jpaEntity, jpaPathList, pathPrefix.isEmpty() ? pathItem : pathPrefix + "/" + pathItem,
          targetIsCollection);
    }
  }

  /**
   * Skips all those properties that are or belong to a collection property. E.g
   * (Organization)Comment or (Person)InhouseAddress/Room
   * @param jpaPathList
   * @param c
   */
  private void copyNonCollectionProperties(final Collection<JPAPath> jpaPathList, final List<JPAPath> c) {
    for (final JPAPath p : c) {
      boolean skip = false;
      for (final JPAElement pathElement : p.getPath()) {
        if (pathElement instanceof JPAAttribute && ((JPAAttribute) pathElement).isCollection()) {
          skip = true;
          break;
        }
      }
      if (!skip)
        jpaPathList.add(p);
    }
  }

  private void createFromClauseCollectionsJoins(final HashMap<String, From<?, ?>> joinTables)
      throws ODataJPAQueryException {

    try {
      if (lastInfo.getAssociationPath() != null
          && lastInfo.getAssociationPath().getLeaf() instanceof JPACollectionAttribute
          && !uriResource.getUriResourceParts().isEmpty()
          && uriResource.getUriResourceParts().get(uriResource.getUriResourceParts().size() - 1)
              .getKind() == UriResourceKind.complexProperty) {
        Path<?> p = target;
        JPAElement element = null;
        for (final JPAElement pathElement : lastInfo.getAssociationPath().getPath()) {
          p = p.get(pathElement.getInternalName());
          element = pathElement;
        }
        joinTables.put(lastInfo.getAssociationPath().getAlias(), (From<?, ?>) p);
        final JPAEntityType targetEt = (JPAEntityType) ((JPAAssociationAttribute) element).getTargetEntity();
        final JPAOperationConverter converter = new JPAOperationConverter(cb, context.getOperationConverter());
        final JPAODataRequestContextAccess subContext = new JPAODataRequestContextImpl(uriResource, requestContext);
        lastInfo.setFilterCompiler(new JPAFilterCrossComplier(odata, sd, targetEt, converter, this, (From<?, ?>) p,
            lastInfo.getAssociationPath(), subContext));
      } else {
        final JPAOperationConverter converter = new JPAOperationConverter(cb, context.getOperationConverter());
        final JPAODataRequestContextAccess subContext = new JPAODataRequestContextImpl(uriResource, requestContext);
        lastInfo.setFilterCompiler(new JPAFilterCrossComplier(odata, sd, jpaEntity, converter, this, lastInfo
            .getAssociationPath(), subContext));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
          HttpStatusCode.BAD_REQUEST, e);
    }
    lastInfo.setFromClause(target);
  }

  private void createFromClauseDescriptionFields(final Collection<JPAPath> selectionPath,
      final HashMap<String, From<?, ?>> joinTables) throws ODataApplicationException {
    final List<JPAPath> descriptionFields = extractDescriptionAttributes(selectionPath);
    for (final JPANavigationProptertyInfo info : this.navigationInfo) {
      if (info.getFilterCompiler() != null) {
        generateDesciptionJoin(joinTables,
            determineAllDescriptionPath(info.getFromClause() == target ? descriptionFields : Collections.emptyList(),
                info.getFilterCompiler()), info.getFromClause());
      }
    }
  }

  /**
   * Completes NavigationInfo and add Joins for navigation parts e.g. from <code>../Organizations('3')/Roles</code>
   * @param joinTables
   * @throws ODataJPAQueryException
   */
  private void createFromClauseNavigationJoins(final HashMap<String, From<?, ?>> joinTables)
      throws ODataJPAQueryException {

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
          final JPAOperationConverter converter = new JPAOperationConverter(cb, context.getOperationConverter());
          final JPAODataRequestContextAccess subContext = new JPAODataRequestContextImpl(naviInfo.getUriInfo(),
              requestContext);
          naviInfo.setFilterCompiler(new JPAFilterCrossComplier(odata, sd, naviInfo.getEntityType(), converter, this,
              naviInfo.getFromClause(), null, subContext));
        } catch (final ODataJPAModelException e) {
          throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
              HttpStatusCode.BAD_REQUEST, e);
        }
      }
      target = createJoinFromPath(naviInfo.getAssociationPath().getAlias(), naviInfo.getAssociationPath().getPath(),
          target, JoinType.INNER);
      joinTables.put(naviInfo.getAssociationPath().getAlias(), target);
    }
  }

  /**
   * Add from clause that is needed for orderby clauses that are not part of the navigation part e.g.
   * <code>"Organizations?$orderby=Roles/$count desc,Address/Region desc"</code>
   * @param orderByTarget
   * @param joinTables
   */
  private void createFromClauseOrderBy(final List<JPAAssociationPath> orderByTarget,
      final HashMap<String, From<?, ?>> joinTables) {
    for (final JPAAssociationPath orderBy : orderByTarget) {
      From<?, ?> join = target;
      for (final JPAElement o : orderBy.getPath())
        join = join.join(o.getInternalName(), JoinType.LEFT);
      // Take on condition from JPA metadata; no explicit on
      joinTables.put(orderBy.getAlias(), join);
    }
  }

  /**
   * Start point of a Join Query e.g. triggered by <code>../Organizations</code> or
   * <code>../Organizations('3')/Roles</code>
   * @param query
   * @param joinTables
   * @throws ODataJPAQueryException
   */
  private void createFromClauseRoot(final CriteriaQuery<?> query, final HashMap<String, From<?, ?>> joinTables)
      throws ODataJPAQueryException {
    try {
      final JPAEntityType sourceEt = this.navigationInfo.get(0).getEntityType();
      this.root = query.from(sourceEt.getTypeClass());
      joinTables.put(sourceEt.getExternalFQN().getFullQualifiedNameAsString(), root);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private Set<JPAPath> determineAllDescriptionPath(final List<JPAPath> descriptionFields,
      final JPAFilterComplier filter)
      throws ODataApplicationException {

    final Set<JPAPath> allPath = new HashSet<>(descriptionFields);
    for (final JPAPath path : filter.getMember()) {
      if (path.getLeaf() instanceof JPADescriptionAttribute)
        allPath.add(path);
    }
    return allPath;
  }

  private JPAElement findCollection(final JPANavigationProptertyInfo lastInfo, final JPAPath path)
      throws ODataJPAProcessorException, JPANoSelectionException {

    JPAElement collection = null;
    final StringBuilder collectionPath = new StringBuilder();
    for (final JPAElement element : path.getPath()) {
      collectionPath.append(element.getExternalName());
      if (element instanceof JPACollectionAttribute) {
        if (checkCollectionIsPartOfGroup(collectionPath.toString())) {
          collection = element;
        } else if (lastInfo.getAssociationPath() != null
            && (lastInfo.getAssociationPath().getLeaf() instanceof JPACollectionAttribute)) {
          throw new JPANoSelectionException();
        }
        break;
      }
      collectionPath.append(JPAPath.PATH_SEPERATOR);
    }
    return collection;
  }

  protected <Y extends Comparable<? super Y>> javax.persistence.criteria.Expression<Boolean> createBoundary(
      final List<JPANavigationProptertyInfo> info, final Optional<JPAKeyBoundary> keyBoundary)
      throws ODataJPAQueryException {

    if (keyBoundary.isPresent()) {
      // Given key: Organizations('1')/Roles(...)
      // First is the root
      final JPANavigationProptertyInfo naviInfo = info.get(keyBoundary.get().getNoHops() - 1);
      try {
        final JPAEntityType et = naviInfo.getEntityType();
        final From<?, ?> f = naviInfo.getFromClause();

        if (keyBoundary.get().getKeyBoundary().hasUpperBoundary()) {
          return createBoundaryWithUpper(et, f, keyBoundary.get().getKeyBoundary());
        } else {
          return createBoundaryEquals(et, f, keyBoundary.get().getKeyBoundary());
        }
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private <Y extends Comparable<? super Y>> javax.persistence.criteria.Expression<Boolean> createBoundaryWithUpper(
      final JPAEntityType et,
      final From<?, ?> f, final JPAKeyPair jpaKeyPair)
      throws ODataJPAModelException {

    final List<JPAAttribute> keyElements = et.getKey();
    javax.persistence.criteria.Expression<Boolean> lowerExpression = null;
    javax.persistence.criteria.Expression<Boolean> upperExpression = null;
    for (int primaryIndex = 0; primaryIndex < keyElements.size(); primaryIndex++) {
      for (int secondaryIndex = primaryIndex; secondaryIndex < keyElements.size(); secondaryIndex++) {
        final JPAAttribute keyElement = keyElements.get(secondaryIndex);
        final Path<Y> keyPath = (Path<Y>) ExpressionUtil.convertToCriteriaPath(f,
            et.getPath(keyElement.getExternalName()).getPath());
        final Y lowerBoundary = jpaKeyPair.getMinElement(keyElement);
        final Y upperBoundary = jpaKeyPair.getMaxElement(keyElement);
        if (secondaryIndex == primaryIndex) {
          if (primaryIndex == 0) {
            lowerExpression = cb.greaterThanOrEqualTo(keyPath, lowerBoundary);
            upperExpression = cb.lessThanOrEqualTo(keyPath, upperBoundary);
          } else {
            lowerExpression = cb.or(lowerExpression, cb.greaterThan(keyPath, lowerBoundary));
            upperExpression = cb.or(upperExpression, cb.lessThan(keyPath, upperBoundary));
          }
        } else {
          lowerExpression = cb.and(lowerExpression, cb.equal(keyPath, lowerBoundary));
          upperExpression = cb.and(upperExpression, cb.equal(keyPath, upperBoundary));
        }
      }

    }
    return cb.and(lowerExpression, upperExpression);
  }

  @SuppressWarnings("unchecked")
  private <Y extends Comparable<? super Y>> javax.persistence.criteria.Expression<Boolean> createBoundaryEquals(
      final JPAEntityType et, final From<?, ?> f, final JPAKeyPair jpaKeyPair) throws ODataJPAModelException {

    javax.persistence.criteria.Expression<Boolean> whereCondition = null;
    for (final JPAAttribute keyElement : et.getKey()) {
      final Path<Y> keyPath = (Path<Y>) ExpressionUtil.convertToCriteriaPath(f, et.getPath(keyElement.getExternalName())
          .getPath());
      final javax.persistence.criteria.Expression<Boolean> eqFragment = cb.equal(keyPath, jpaKeyPair.getMin().get(
          keyElement));
      if (whereCondition == null)
        whereCondition = eqFragment;
      else
        whereCondition = cb.and(whereCondition, eqFragment);
    }
    return whereCondition;
  }
}
