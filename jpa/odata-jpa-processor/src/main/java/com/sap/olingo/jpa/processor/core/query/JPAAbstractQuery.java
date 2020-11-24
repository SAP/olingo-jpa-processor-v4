package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.MISSING_CLAIM;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.MISSING_CLAIMS_PROVIDER;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_RESULT_ENTITY_TYPE_ERROR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.WILDCARD_UPPER_NOT_SUPPORTED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.debug.RuntimeMeasurement;
import org.apache.olingo.server.api.uri.UriParameter;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAClaimsPair;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public abstract class JPAAbstractQuery {

  protected static final String SELECT_ITEM_SEPARATOR = ",";
  protected static final String SELECT_ALL = "*";
  protected final EntityManager em;
  protected final CriteriaBuilder cb;
  protected final JPAEntityType jpaEntity;
  protected final JPAServiceDocument sd;
  protected final JPAServiceDebugger debugger;
  protected final OData odata;
  protected Locale locale;
  protected final Optional<JPAODataClaimProvider> claimsProvider;
  protected final List<String> groups;

  public JPAAbstractQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntityType,
      final EntityManager em, final Optional<JPAODataClaimProvider> claimsProvider) {

    super();
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
    this.jpaEntity = jpaEntityType;
    this.debugger = new EmptyDebugger();
    this.odata = odata;
    this.claimsProvider = claimsProvider;
    this.groups = Collections.emptyList();
  }

  public JPAAbstractQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntityType,
      final EntityManager em, final JPAServiceDebugger debugger, final Optional<JPAODataClaimProvider> claimsProvider) {

    super();
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
    this.jpaEntity = jpaEntityType;
    this.debugger = debugger;
    this.odata = odata;
    this.claimsProvider = claimsProvider;
    this.groups = Collections.emptyList();
  }

  public JPAAbstractQuery(final OData odata, final JPAServiceDocument sd, final EdmEntityType edmEntityType,
      final EntityManager em, final Optional<JPAODataClaimProvider> claimsProvider) throws ODataApplicationException {
    super();
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
    try {
      this.jpaEntity = sd.getEntity(edmEntityType);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    this.debugger = new EmptyDebugger();
    this.odata = odata;
    this.claimsProvider = claimsProvider;
    this.groups = Collections.emptyList();
  }

  public JPAAbstractQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntityType,
      final JPAODataRequestContextAccess requestContext) {
    super();
    final Optional<JPAODataGroupProvider> groupsProvider = requestContext.getGroupsProvider();
    this.em = requestContext.getEntityManager();
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
    this.jpaEntity = jpaEntityType;
    this.debugger = requestContext.getDebugger();
    this.odata = odata;
    this.claimsProvider = requestContext.getClaimsProvider();
    this.groups = groupsProvider.isPresent() ? groupsProvider.get().getGroups() : Collections.emptyList();
  }

  protected javax.persistence.criteria.Expression<Boolean> createWhereByKey(final From<?, ?> root,
      final javax.persistence.criteria.Expression<Boolean> whereCondition, final List<UriParameter> keyPredicates,
      final JPAEntityType et) throws ODataApplicationException {
    // .../Organizations('3')
    // .../BusinessPartnerRoles(BusinessPartnerID='6',RoleCategory='C')
    javax.persistence.criteria.Expression<Boolean> compoundCondition = whereCondition;

    if (keyPredicates != null) {
      for (final UriParameter keyPredicate : keyPredicates) {
        javax.persistence.criteria.Expression<Boolean> equalCondition;
        try {
          equalCondition = ExpressionUtil.createEQExpression(odata, cb, root, et, keyPredicate);
        } catch (final ODataJPAModelException e) {
          throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
        }
        if (compoundCondition == null)
          compoundCondition = equalCondition;
        else
          compoundCondition = cb.and(compoundCondition, equalCondition);
      }
    }
    return compoundCondition;
  }

  public abstract From<?, ?> getRoot();

  /**
   *
   * @param <T> the type of the result
   * @return
   */
  public abstract <T> AbstractQuery<T> getQuery();

  public JPAServiceDebugger getDebugger() {
    return debugger;
  }

  protected abstract Locale getLocale();

  protected void generateDescriptionJoin(final HashMap<String, From<?, ?>> joinTables, final Set<JPAPath> pathSet,
      final From<?, ?> target) {

    for (final JPAPath descriptionFieldPath : pathSet) {
      final JPADescriptionAttribute descriptionField = ((JPADescriptionAttribute) descriptionFieldPath.getLeaf());
      final Join<?, ?> join = createJoinFromPath(descriptionFieldPath.getAlias(), descriptionFieldPath.getPath(),
          target,
          JoinType.LEFT);
      if (descriptionField.isLocationJoin())
        join.on(createOnCondition(join, descriptionField, getLocale().toString()));
      else
        join.on(createOnCondition(join, descriptionField, getLocale().getLanguage()));
      joinTables.put(descriptionField.getInternalName(), join);
    }
  }

  protected <T, S> Join<T, S> createJoinFromPath(final String alias, final List<JPAElement> pathList,
      final From<T, S> root, final JoinType finalJoinType) {

    Join<T, S> join = null;
    JoinType jt;
    for (int i = 0; i < pathList.size(); i++) {
      if (i == pathList.size() - 1)
        jt = finalJoinType;
      else
        jt = JoinType.INNER;
      if (i == 0) {
        join = root.join(pathList.get(i).getInternalName(), jt);
        join.alias(alias);
      } else if (i < pathList.size()) {
        join = join.join(pathList.get(i).getInternalName(), jt);
        join.alias(pathList.get(i).getExternalName());
      }
    }
    return join;
  }

  private Expression<Boolean> createOnCondition(final Join<?, ?> join, final JPADescriptionAttribute descriptionField,
      final String localValue) {
    final Predicate existingOn = join.getOn();
    Expression<Boolean> result = cb.equal(determineLocalePath(join, descriptionField.getLocaleFieldName()), localValue);
    if (existingOn != null)
      result = cb.and(existingOn, result);
    for (final JPAPath value : descriptionField.getFixedValueAssignment().keySet()) {
      result = cb.and(result,
          cb.equal(determineLocalePath(join, value), descriptionField.getFixedValueAssignment().get(value)));
    }
    return result;
  }

  private javax.persistence.criteria.Expression<?> determineLocalePath(final Join<?, ?> join,
      final JPAPath jpaPath) {
    Path<?> p = join;
    for (final JPAElement pathElement : jpaPath.getPath()) {
      p = p.get(pathElement.getInternalName());
    }
    return p;
  }

  abstract JPAODataSessionContextAccess getContext();

  protected javax.persistence.criteria.Expression<Boolean> addWhereClause(
      javax.persistence.criteria.Expression<Boolean> whereCondition,
      final javax.persistence.criteria.Expression<Boolean> additionalExpression) {

    if (additionalExpression != null) {
      if (whereCondition == null)
        whereCondition = additionalExpression;
      else
        whereCondition = cb.and(whereCondition, additionalExpression);
    }
    return whereCondition;
  }

  protected javax.persistence.criteria.Expression<Boolean> orWhereClause(
      javax.persistence.criteria.Expression<Boolean> whereCondition,
      final javax.persistence.criteria.Expression<Boolean> additionalExpression) {

    if (additionalExpression != null) {
      if (whereCondition == null)
        whereCondition = additionalExpression;
      else
        whereCondition = cb.or(whereCondition, additionalExpression);
    }
    return whereCondition;
  }

  @SuppressWarnings({ "unchecked" })
  private <Y extends Comparable<? super Y>> Predicate createBetween(final JPAClaimsPair<?> value, final Path<?> p) {
    return cb.between((javax.persistence.criteria.Expression<? extends Y>) p, (Y) value.min, (Y) value.max);
  }

  @SuppressWarnings("unchecked")
  private javax.persistence.criteria.Expression<Boolean> createProtectionWhereForAttribute(
      final List<JPAClaimsPair<?>> values, final Path<?> p, final boolean wildcardsSupported)
      throws ODataJPAQueryException {

    javax.persistence.criteria.Expression<Boolean> attributeRestriction = null;
    for (final JPAClaimsPair<?> value : values) { // for each given claim value
      if (value.hasUpperBoundary) {
        if (wildcardsSupported && ((String) value.min).matches(".*[\\*|\\%|\\+|\\_].*"))
          throw new ODataJPAQueryException(WILDCARD_UPPER_NOT_SUPPORTED, HttpStatusCode.INTERNAL_SERVER_ERROR);
        else
          attributeRestriction = orWhereClause(attributeRestriction, createBetween(value, p));
      } else {
        if (wildcardsSupported && ((String) value.min).matches(".*[\\*|\\%|\\+|\\_].*"))
          attributeRestriction = orWhereClause(attributeRestriction, cb.like((Path<String>) p,
              ((String) value.min).replace('*', '%').replace('+', '_')));
        else
          attributeRestriction = orWhereClause(attributeRestriction, cb.equal(p, value.min));
      }
    }
    return attributeRestriction;
  }

  protected javax.persistence.criteria.Expression<Boolean> createProtectionWhereForEntityType(
      final Optional<JPAODataClaimProvider> claimsProvider, final JPAEntityType et, final From<?, ?> from)
      throws ODataJPAQueryException {
    try {
      javax.persistence.criteria.Expression<Boolean> restriction = null;
      final Map<String, From<?, ?>> dummyJoinTables = new HashMap<>(1);
      for (final JPAProtectionInfo protection : et.getProtections()) { // look for protected attributes
        final List<JPAClaimsPair<?>> values = claimsProvider.get().get(protection.getClaimName()); // NOSONAR
        if (values.isEmpty())
          throw new ODataJPAQueryException(MISSING_CLAIM, HttpStatusCode.FORBIDDEN);
        final Path<?> p = ExpressionUtil.convertToCriteriaPath(dummyJoinTables, from, protection.getPath().getPath());
        restriction = addWhereClause(restriction, createProtectionWhereForAttribute(values, p, protection
            .supportsWildcards()));
      }
      return restriction;
    } catch (final NoSuchElementException e) {
      throw new ODataJPAQueryException(MISSING_CLAIMS_PROVIDER, HttpStatusCode.FORBIDDEN);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(QUERY_RESULT_ENTITY_TYPE_ERROR, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  // TODO clean-up
  private class EmptyDebugger implements JPAServiceDebugger {

    @Override
    public int startRuntimeMeasurement(final Object instance, final String methodName) {
      return 0;
    }

    @Override
    public void stopRuntimeMeasurement(final int handle) {
      // not needed
    }

    @Override
    public Collection<RuntimeMeasurement> getRuntimeInformation() {
      return new ArrayList<>();
    }

  }
}