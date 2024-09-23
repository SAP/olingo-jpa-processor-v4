package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.MISSING_CLAIM;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.MISSING_CLAIMS_PROVIDER;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ORDER_BY_NOT_SUPPORTED;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_RESULT_ENTITY_TYPE_ERROR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.WILDCARD_UPPER_NOT_SUPPORTED;
import static java.util.stream.Collectors.toList;
import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAClaimsPair;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalArgumentException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterComplier;
import com.sap.olingo.jpa.processor.core.processor.JPAEmptyDebugger;
import com.sap.olingo.jpa.processor.core.properties.JPAOrderByPropertyFactory;
import com.sap.olingo.jpa.processor.core.properties.JPAProcessorAttribute;

public abstract class JPAAbstractQuery {
  protected static final String PERMISSIONS_REGEX = ".*[\\*\\%\\+\\_].*";
  protected static final String SELECT_ITEM_SEPARATOR = ",";
  protected static final String SELECT_ALL = "*";
  protected static final String ROW_NUMBER_COLUMN_NAME = "rowNumber";
  protected static final String COUNT_COLUMN_NAME = "\"$count\"";
  protected final EntityManager em;
  protected final CriteriaBuilder cb;
  protected final JPAEntityType jpaEntity; // Entity type of the result, which may not be the same as the start of a
                                           // navigation
  protected final JPAServiceDocument sd;
  protected JPAServiceDebugger debugger;
  protected final OData odata;
  protected Locale locale;
  protected final Optional<JPAODataClaimProvider> claimsProvider;
  protected final List<String> groups;

  JPAAbstractQuery(final OData odata, final JPAServiceDocument sd, final EdmEntityType edmEntityType,
      final EntityManager em, final Optional<JPAODataClaimProvider> claimsProvider) throws ODataApplicationException {

    this(odata, sd, asJPAEntityType(sd, edmEntityType), em, claimsProvider);
  }

  JPAAbstractQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntityType,
      final EntityManager em, final Optional<JPAODataClaimProvider> claimsProvider) {

    super();
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
    this.jpaEntity = jpaEntityType;
    this.debugger = new JPAEmptyDebugger();
    this.odata = odata;
    this.claimsProvider = claimsProvider;
    this.groups = Collections.emptyList();
  }

  JPAAbstractQuery(final OData odata, final JPAEntityType jpaEntityType,
      final JPAODataRequestContextAccess requestContext) throws ODataException {
    super();
    final Optional<JPAODataGroupProvider> groupsProvider = requestContext.getGroupsProvider();
    this.em = requestContext.getEntityManager();
    this.cb = em.getCriteriaBuilder();
    this.sd = requestContext.getEdmProvider().getServiceDocument();
    this.jpaEntity = jpaEntityType;
    this.debugger = requestContext.getDebugger();
    this.odata = odata;
    this.claimsProvider = requestContext.getClaimsProvider();
    this.groups = groupsProvider.isPresent() ? groupsProvider.get().getGroups() : Collections.emptyList();
  }

  public JPAServiceDebugger getDebugger() {
    return debugger;
  }

  /**
   *
   * @param <T> the type of the result
   * @return
   */
  public abstract <T> AbstractQuery<T> getQuery();

  public abstract <S, T> From<S, T> getRoot();

  protected static JPAEntityType asJPAEntityType(final JPAServiceDocument sd, final EdmEntityType edmEntityType)
      throws ODataJPAQueryException {
    try {
      return sd.getEntity(edmEntityType);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  protected jakarta.persistence.criteria.Expression<Boolean> addWhereClause(
      jakarta.persistence.criteria.Expression<Boolean> whereCondition,
      final jakarta.persistence.criteria.Expression<Boolean> additionalExpression) {

    if (additionalExpression != null) {
      if (whereCondition == null) {
        whereCondition = additionalExpression;
      } else {
        whereCondition = cb.and(whereCondition, additionalExpression);
      }
    }
    return whereCondition;
  }

  protected final void createFromClauseDescriptionFields(final Collection<JPAPath> selectionPath,
      final Map<String, From<?, ?>> joinTables, final From<?, ?> from,
      final List<JPANavigationPropertyInfo> navigationInfo)
      throws ODataApplicationException {

    final List<JPAPath> descriptionFields = extractDescriptionAttributes(selectionPath);
    for (final JPANavigationPropertyInfo info : navigationInfo) {
      generateDescriptionJoin(joinTables,
          determineAllDescriptionPath(info.getFromClause() == from ? descriptionFields : Collections.emptyList(),
              info.getFilterCompiler()), info.getFromClause());
    }
  }

  /**
   * Add from clause that is needed for orderby clauses that are not part of the navigation part e.g.
   * <code>"Organizations?$orderby=Roles/$count desc,Address/Region desc"</code>
   * @param orderByTarget
   * @param joinTables
   */
  protected void createFromClauseOrderBy(final List<JPAProcessorAttribute> orderByTarget,
      final Map<String, From<?, ?>> joinTables, final From<?, ?> from) {
    orderByTarget.stream()
        .map(property -> property.setTarget(from, joinTables, cb))
        .filter(JPAProcessorAttribute::requiresJoin)
        .forEach(property -> joinTables.put(property.getAlias(), property.createJoin()));
  }

  protected List<jakarta.persistence.criteria.Expression<?>> createGroupBy(final Map<String, From<?, ?>> joinTables, // NOSONAR
      final From<?, ?> from, final Collection<JPAPath> selectionPathList, @Nonnull final Set<Path<?>> orderByPaths) {

    try (JPARuntimeMeasurement serializerMeasurement = debugger.newMeasurement(this, "createGroupBy")) {
      final List<jakarta.persistence.criteria.Expression<?>> groupBy = new ArrayList<>();
      for (final JPAPath jpaPath : selectionPathList) {
        final var path = ExpressionUtility.convertToCriteriaPath(joinTables, from, jpaPath);
        orderByPaths.remove(path);
        groupBy.add(path);
      }

      for (final var path : orderByPaths) {
        groupBy.add(path);
      }
      return groupBy;
    }
  }

  protected List<jakarta.persistence.criteria.Expression<?>> createGroupBy(final Map<String, From<?, ?>> joinTables,
      final From<?, ?> from, final Collection<JPAPath> selectionPathList,
      @Nonnull final List<JPAProcessorAttribute> orderByAttributes) {

    try (JPARuntimeMeasurement serializerMeasurement = debugger.newMeasurement(this, "createGroupBy")) {
      final Set<Path<?>> orderByPaths = new HashSet<>();
      final List<jakarta.persistence.criteria.Expression<?>> groupBy = new ArrayList<>();
      for (final JPAPath jpaPath : selectionPathList) {
        final var path = ExpressionUtility.convertToCriteriaPath(joinTables, from, jpaPath);
        orderByPaths.add(path);
        groupBy.add(path);
      }

      for (final var attribute : orderByAttributes) {
        final var path = attribute.getPath();
        if (path != null && !orderByPaths.contains(path))
          groupBy.add(path);
      }
      return groupBy;
    }
  }

  protected <T, S> Join<T, S> createJoinFromPath(final String alias, final List<JPAElement> pathList,
      final From<T, S> root, final JoinType finalJoinType) {

    Join<T, S> join = null;
    JoinType joinType;
    for (int i = 0; i < pathList.size(); i++) {
      if (i == pathList.size() - 1) {
        joinType = finalJoinType;
      } else {
        joinType = JoinType.INNER;
      }
      if (i == 0) {
        join = root.join(pathList.get(i).getInternalName(), joinType);
        join.alias(alias);
      } else if (i < pathList.size()) {
        join = join.join(pathList.get(i).getInternalName(), joinType);
        join.alias(pathList.get(i).getExternalName());
      }
    }
    return join;
  }

  /**
   * The value of the $select query option is a comma-separated list of <b>properties</b>, qualified action names,
   * qualified function names, the <b>star operator (*)</b>, or the star operator prefixed with the namespace or alias
   * of the schema in order to specify all operations defined in the schema. See:
   * <a
   * href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398297"
   * >OData Version 4.0 Part 1 - 11.2.4.1 System Query Option $select</a>
   * <p>
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

    try (JPARuntimeMeasurement serializerMeasurement = debugger.newMeasurement(this, "createSelectClause")) {
      final List<Selection<?>> selections = new ArrayList<>();

      // Build select clause
      for (final JPAPath jpaPath : requestedProperties) {
        if (jpaPath.isPartOfGroups(groups)) {
          final Path<?> path = ExpressionUtility.convertToCriteriaPath(joinTables, target, jpaPath);
          path.alias(jpaPath.getAlias());
          selections.add(path);
        }
      }
      return selections;
    }
  }

  protected Expression<Boolean> createProtectionWhereForEntityType(
      final Optional<JPAODataClaimProvider> claimsProvider, final JPAEntityType et, final From<?, ?> from)
      throws ODataJPAQueryException {
    try {
      jakarta.persistence.criteria.Expression<Boolean> restriction = null;
      final Map<String, From<?, ?>> dummyJoinTables = new HashMap<>(1);
      for (final JPAProtectionInfo protection : et.getProtections()) { // look for protected attributes
        final List<JPAClaimsPair<?>> values = claimsProvider.get().get(protection.getClaimName()); // NOSONAR
        if (values.isEmpty()) {
          throw new ODataJPAQueryException(MISSING_CLAIM, HttpStatusCode.FORBIDDEN);
        }
        if (!(containsAll(values))) {
          final Path<?> path = ExpressionUtility.convertToCriteriaPath(dummyJoinTables, from, protection.getPath());
          restriction = addWhereClause(restriction, createProtectionWhereForAttribute(values, path, protection
              .supportsWildcards()));
        }

      }
      return restriction;
    } catch (final NoSuchElementException e) {
      throw new ODataJPAQueryException(MISSING_CLAIMS_PROVIDER, HttpStatusCode.FORBIDDEN);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(QUERY_RESULT_ENTITY_TYPE_ERROR, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private boolean containsAll(final List<JPAClaimsPair<?>> values) {
    return values.stream()
        .anyMatch(value -> JPAClaimsPair.ALL.equals(value.min));
  }

  protected Expression<Boolean> createWhereByKey(final JPANavigationPropertyInfo navigationInfo)
      throws ODataJPAModelException, ODataApplicationException {
    return createWhereByKey(navigationInfo.getFromClause(), navigationInfo.getKeyPredicates(), navigationInfo
        .getEntityType());
  }

  protected Expression<Boolean> createWhereByKey(final From<?, ?> root,
      final List<UriParameter> keyPredicates, final JPAEntityType et) throws ODataApplicationException {
    // .../Organizations('3')
    // .../BusinessPartnerRoles(BusinessPartnerID='6',RoleCategory='C')
    jakarta.persistence.criteria.Expression<Boolean> compoundCondition = null;

    if (keyPredicates != null) {
      for (final UriParameter keyPredicate : keyPredicates) {
        try {
          final jakarta.persistence.criteria.Expression<Boolean> equalCondition =
              ExpressionUtility.createEQExpression(odata, cb, root, et, keyPredicate);
          compoundCondition = addWhereClause(compoundCondition, equalCondition);
        } catch (final ODataJPAModelException e) {
          throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
        }
      }
    }
    return compoundCondition;
  }

  @SuppressWarnings("unchecked")
  protected final Expression<Boolean> createWhereKeyIn(final JPAAssociationPath associationPath,
      final From<?, ?> target, final Subquery<?> subQuery) throws ODataJPAQueryException {

    try {
      final List<Path<Comparable<?>>> paths = (List<Path<Comparable<?>>>) createWhereKeyInPathList(associationPath,
          target);
      debugger.trace(this, "Creating WHERE snipped for in clause %s", paths);
      return ((ProcessorCriteriaBuilder) cb).in(paths, (Subquery<List<Comparable<?>>>) subQuery);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  protected List<?> createWhereKeyInPathList(final JPAAssociationPath associationPath,
      final From<?, ?> target) throws ODataJPAModelException {

    if (associationPath.hasJoinTable()) {
      final JPAJoinTable joinTable = associationPath.getJoinTable();
      // jt.getInverseJoinColumns().get(0).getLeftPath()
      debugger.trace(this, "Creating WHERE snipped for key in with join conditions %s", joinTable.getJoinColumns());
      return joinTable
          .getJoinColumns()
          .stream()
          .map(JPAOnConditionItem::getRightPath)
          .map(JPAPath::getLeaf)
          .map(leaf -> target.get(leaf.getInternalName()))
          .collect(toList()); // NOSONAR
    }
    return associationPath.getJoinColumnsList().stream()
        .map(key -> mapOnToWhere(key, target))
        .collect(toList()); // NOSONAR
  }

  protected final List<JPAPath> extractDescriptionAttributes(final Collection<JPAPath> jpaPathList) {

    final List<JPAPath> result = new ArrayList<>();
    for (final JPAPath p : jpaPathList) {
      if (p.getLeaf() instanceof JPADescriptionAttribute) {
        result.add(p);
      }
    }
    return result;
  }

  protected List<JPAProcessorAttribute> getOrderByAttributes(final OrderByOption orderBy)
      throws ODataApplicationException {
    if (orderBy != null) {
      try {
        final var factory = new JPAOrderByPropertyFactory();
        final var orderByAttributes = orderBy.getOrders().stream()
            .map(o -> factory.createProperty(o, jpaEntity, getLocale()))
            .collect(Collectors.toList()); // NOSONAR
        for (final var orderByAttribute : orderByAttributes) {
          if (!orderByAttribute.isSortable())
            throw new ODataJPAQueryException(QUERY_PREPARATION_ORDER_BY_NOT_SUPPORTED, BAD_REQUEST, orderByAttribute
                .getAlias());
        }
        return orderByAttributes;

      } catch (final ODataJPAIllegalArgumentException e) {
        throw new ODataJPAQueryException(QUERY_PREPARATION_ORDER_BY_NOT_SUPPORTED, BAD_REQUEST, e.getParams());
      }
    }
    return new ArrayList<>();
  }

  protected void generateDescriptionJoin(final Map<String, From<?, ?>> joinTables, final Set<JPAPath> pathSet,
      final From<?, ?> target) {

    for (final JPAPath descriptionFieldPath : pathSet) {
      final JPADescriptionAttribute descriptionField = ((JPADescriptionAttribute) descriptionFieldPath.getLeaf());
      final Join<?, ?> join = createJoinFromPath(descriptionFieldPath.getAlias(), descriptionFieldPath.getPath(),
          target, JoinType.LEFT);
      if (descriptionField.isLocationJoin()) {
        join.on(createOnCondition(join, descriptionField, getLocale().toString()));
      } else {
        join.on(createOnCondition(join, descriptionField, getLocale().getLanguage()));
      }
      joinTables.put(descriptionFieldPath.getAlias(), join);
    }
  }

  protected abstract Locale getLocale();

  protected Expression<Boolean> orWhereClause(Expression<Boolean> whereCondition,
      final Expression<Boolean> additionalExpression) {

    if (additionalExpression != null) {
      if (whereCondition == null) {
        whereCondition = additionalExpression;
      } else {
        whereCondition = cb.or(whereCondition, additionalExpression);
      }
    }
    return whereCondition;
  }

  public JPAEntityType getJpaEntity() {
    return jpaEntity;
  }

  Set<JPAPath> determineAllDescriptionPath(final List<JPAPath> descriptionFields,
      final JPAFilterComplier filter) throws ODataApplicationException {

    final Set<JPAPath> allPath = new HashSet<>(descriptionFields);
    if (filter != null) {
      for (final JPAPath path : filter.getMember()) {
        if (path.getLeaf() instanceof JPADescriptionAttribute) {
          allPath.add(path);
        }
      }
    }
    return allPath;
  }

  abstract JPAODataRequestContextAccess getContext();

  @SuppressWarnings({ "unchecked" })
  private <Y extends Comparable<? super Y>> Predicate createBetween(final JPAClaimsPair<?> value, final Path<?> path) {
    return cb.between((jakarta.persistence.criteria.Expression<? extends Y>) path, (Y) value.min, (Y) value.max);
  }

  private Expression<Boolean> createOnCondition(final Join<?, ?> join, final JPADescriptionAttribute descriptionField,
      final String localValue) {
    final Predicate existingOn = join.getOn();
    Expression<Boolean> result = cb.equal(determineLocalePath(join, descriptionField.getLocaleFieldName()), localValue);
    if (existingOn != null) {
      result = cb.and(existingOn, result);
    }
    for (final JPAPath value : descriptionField.getFixedValueAssignment().keySet()) {
      result = cb.and(result,
          cb.equal(determineLocalePath(join, value), descriptionField.getFixedValueAssignment().get(value)));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private Expression<Boolean> createProtectionWhereForAttribute(final List<JPAClaimsPair<?>> values, final Path<?> path,
      final boolean wildcardsSupported) throws ODataJPAQueryException {

    jakarta.persistence.criteria.Expression<Boolean> attributeRestriction = null;
    for (final JPAClaimsPair<?> value : values) { // for each given claim value
      if (value.hasUpperBoundary) {
        if (wildcardsSupported && containsWildcard((String) value.min)) {
          throw new ODataJPAQueryException(WILDCARD_UPPER_NOT_SUPPORTED, HttpStatusCode.BAD_REQUEST);
        } else {
          attributeRestriction = orWhereClause(attributeRestriction, createBetween(value, path));
        }
      } else {
        if (wildcardsSupported && containsWildcard((String) value.min)) {
          attributeRestriction = orWhereClause(attributeRestriction, cb.like((Path<String>) path,
              ((String) value.min).replace('*', '%').replace('+', '_')));
        } else {
          attributeRestriction = orWhereClause(attributeRestriction, cb.equal(path, value.min));
        }
      }
    }
    return attributeRestriction;
  }

  private boolean containsWildcard(final String min) {
    return min.contains("*")
        || min.contains("+")
        || min.contains("%")
        || min.contains("_");
  }

  private Expression<?> determineLocalePath(final Join<?, ?> join, final JPAPath jpaPath) {
    Path<?> path = join;
    for (final JPAElement pathElement : jpaPath.getPath()) {
      path = path.get(pathElement.getInternalName());
    }
    return path;
  }

  private Path<Comparable<?>> mapOnToWhere(final JPAOnConditionItem on, final From<?, ?> target) {
    return target.get(on.getRightPath().getLeaf().getInternalName());
  }

  protected boolean hasRowLimit(final JPANavigationPropertyInfo hop) {
    final TopOption top = hop.getUriInfo().getTopOption();
    final SkipOption skip = hop.getUriInfo().getSkipOption();
    return top != null || skip != null;
  }

  protected Expression<Boolean> createWhereByRowNumber(final From<?, ?> target, final JPANavigationPropertyInfo hop) {

    final Expression<? extends Number> rowNumberPath = target.get(ROW_NUMBER_COLUMN_NAME);
    final Optional<TopOption> top = Optional.ofNullable(hop.getUriInfo().getTopOption());
    final Optional<SkipOption> skip = Optional.ofNullable(hop.getUriInfo().getSkipOption());
    final Integer firstRow = skip.map(SkipOption::getValue).orElse(0);
    final Predicate offset = cb.gt(rowNumberPath, firstRow);
    final Predicate limit = top
        .map(t -> t.getValue() + firstRow)
        .map(l -> cb.le(rowNumberPath, l))
        .orElse(null);
    return addWhereClause(offset, limit);
  }

  protected Expression<Boolean> createWhereTableJoin(final From<?, ?> joinRoot, final From<?, ?> joinTable,
      final JPAAssociationPath association, final boolean useInverse) throws ODataJPAQueryException {
    if (association.hasJoinTable()) {
      try {
        final JPAJoinTable jpaJoinTable = association.getJoinTable();
        final List<JPAOnConditionItem> joinColumns = useInverse ? jpaJoinTable.getInverseJoinColumns() : jpaJoinTable
            .getJoinColumns();
        debugger.trace(this, "Creating WHERE snipped for join table %s with join conditions %s and inverse: %s",
            jpaJoinTable.toString(), joinColumns, useInverse);
        debugger.trace(this, "Creating WHERE snipped for join table, with target: '%s', root: '%s'", joinTable
            .getJavaType(), joinRoot.getJavaType());
        Expression<Boolean> whereCondition = null;
        for (final JPAOnConditionItem jc : joinColumns) {
          final String leftColumn = jc.getLeftPath().getLeaf().getInternalName();
          final String rightColumn = jc.getRightPath().getLeaf().getInternalName();
          final Path<?> left = useInverse ? joinRoot.get(leftColumn) : joinRoot.get(rightColumn);
          final Path<?> right = useInverse ? joinTable.get(rightColumn) : joinTable.get(leftColumn);
          whereCondition = addWhereClause(whereCondition, cb.equal(left, right));
        }
        return whereCondition;
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
      }
    }
    return null;
  }

  /**
   * Any resource path or path expression identifying a collection of entities or complex type instances can be appended
   * with a path segment containing the qualified name of a type derived from the declared type of the collection.</br>
   * The use of the unqualified name of a derived type is <b>not</b> supported.
   * <p>
   * See also:
   * <a href="
   * https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html#sec_AddressingDerivedTypes">
   * 4.11 Addressing Derived Types
   * </a>
   *
   * @param baseType
   * @param potentialDerivedType
   * @return true if potentialDerivedType is indeed a derived type of baseType
   */
  boolean derivedTypeRequested(@Nonnull final JPAStructuredType baseType,
      @Nonnull final JPAStructuredType potentialDerivedType) {
    JPAStructuredType type = potentialDerivedType;
    while (type != null && type.getBaseType() != null) {
      if (baseType.equals(type.getBaseType()))
        return true;
      type = type.getBaseType();
    }
    return false;
  }

  /**
   * Add key restrictions as well as the filter enhancement from the entity definition from superordinate entities
   * @param info
   * @return
   * @throws ODataApplicationException
   */
  protected final Expression<Boolean> createKeyWhere(final List<JPANavigationPropertyInfo> info)
      throws ODataApplicationException {

    Expression<Boolean> whereCondition = null;
    // Given key: Organizations('1')/Roles(...)
    for (final JPANavigationPropertyInfo naviInfo : info) {
      try {
        final JPAEntityType et = naviInfo.getEntityType();
        final From<?, ?> from = naviInfo.getFromClause();
        if (naviInfo.getKeyPredicates() != null) {
          final List<UriParameter> keyPredicates = naviInfo.getKeyPredicates();
          whereCondition = addWhereClause(whereCondition, createWhereByKey(from, keyPredicates, et));
        }
        whereCondition = addWhereClause(whereCondition, createWhereEnhancement(et, from));
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
      }
    }
    return whereCondition;
  }

  protected abstract Expression<Boolean> createWhereEnhancement(final JPAEntityType et, final From<?, ?> from)
      throws ODataJPAProcessorException;
}