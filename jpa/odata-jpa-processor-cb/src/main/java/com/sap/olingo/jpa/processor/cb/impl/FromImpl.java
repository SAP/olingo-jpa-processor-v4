package com.sap.olingo.jpa.processor.cb.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.criteria.CollectionJoin;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.metamodel.CollectionAttribute;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAInheritanceType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exceptions.InternalServerError;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

/**
 * Represents a bound type, usually an entity that appears in
 * the from clause, but may also be an embeddable belonging to
 * an entity in the from clause.
 * <p>
 * Serves as a factory for Joins of associations, embeddables, and
 * collections belonging to the type, and for Paths of attributes
 * belonging to the type.
 *
 * @param <Z> the source type
 * @param <X> the target type
 *
 * @since Java Persistence 2.0
 */
@SuppressWarnings("hiding")
class FromImpl<Z, X> extends PathImpl<X> implements From<Z, X> {

  private static final Log LOGGER = LogFactory.getLog(FromImpl.class);
  private final Set<Join<X, ?>> joins;
  private final Set<Fetch<X, ?>> fetches;
  private final AliasBuilder aliasBuilder;
  private InheritanceInfo inInfo;
  private final CriteriaBuilder cb;
  Optional<? extends AbstractJoinImp<X, ?>> inheritanceJoin;

  FromImpl(final JPAEntityType type, final AliasBuilder aliasBuilder, final CriteriaBuilder cb) {
    this(type, null, aliasBuilder, cb);
  }

  FromImpl(final JPAEntityType type, final JPAPath path, final AliasBuilder aliasBuilder, final CriteriaBuilder cb) {
    super(Optional.ofNullable(path), Optional.empty(), type, Optional.of(aliasBuilder.getNext()));
    this.joins = new HashSet<>();
    this.fetches = new HashSet<>();
    this.aliasBuilder = aliasBuilder;
    this.cb = cb;
    this.inInfo = new InheritanceInfo(type);
    this.inheritanceJoin = addInheritanceJoin();
    this.inheritanceJoin.ifPresent(joins::add);
  }

  private Optional<InheritanceJoin<X, ?>> addInheritanceJoin() {
    var strategy = inInfo.getInheritanceType();
    try {
      if (st != null
          && strategy.isPresent()
          && strategy.get() == InheritanceType.JOINED
          && st.getBaseType() != null) {
        return Optional.of(new InheritanceJoin<>(st, this, aliasBuilder, cb));
      }
    } catch (ODataJPAModelException e) {
      throw new InternalServerError(e);
    }
    return Optional.empty();
  }

  private Optional<InheritanceJoinReversed<X, ?>> addReverseInheritanceJoin(JPAEntityType target) {
    try {
      if (st != null
          && target.getInheritanceInformation().getInheritanceType() == JPAInheritanceType.JOIN_TABLE
          && target.getBaseType() != null) {
        return Optional.of(new InheritanceJoinReversed<>(target, st, this, aliasBuilder, cb));
      }
    } catch (ODataJPAModelException e) {
      throw new InternalServerError(e);
    }
    return Optional.empty();
  }

  /**
   * Perform a type cast upon the expression, returning a new expression object.
   * This method does not cause type conversion:<br>
   * the runtime type is not changed.
   * Warning: may result in a runtime failure.
   * @param type intended type of the expression
   * @return new expression of the given type
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X> Expression<X> as(final Class<X> type) {
    try {
      final JPAEntityType target = ((CriteriaBuilderImpl) cb).getServiceDocument().getEntity(type);
      if (target == null)
        throw new IllegalArgumentException(type.getName() + " is unknown");
      if (isSubtype(type)) {
        if (target.getInheritanceInformation().getInheritanceType() == JPAInheritanceType.JOIN_TABLE) {
          inheritanceJoin = addReverseInheritanceJoin(target);
          inheritanceJoin.ifPresent(joins::add);
          return (Expression<X>) inheritanceJoin.map(join -> (InheritanceJoinReversed<?, ?>) join)
              .map(InheritanceJoinReversed::getTarget).orElse((From<Object, Object>) this);
        } else {
          st = target;
          inInfo = new InheritanceInfo(target);
        }
      }
      return (Expression<X>) this;
    } catch (final ODataJPAModelException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    statement.append(st.getTableName());
    tableAlias.ifPresent(alias -> statement.append(" ").append(alias));
    statement.append(joins.stream().collect(new StringBuilderCollector.ExpressionCollector(statement, " ")));

    return statement;
  }

  /**
   * Create a fetch join to the specified collection-valued
   * attribute using an inner join.
   * @param attribute target of the join
   * @return the resulting join
   */
  @Override
  public <Y> Fetch<X, Y> fetch(@Nonnull final PluralAttribute<? super X, ?, Y> attribute) {
    throw new NotImplementedException();
  }

  /**
   * Create a fetch join to the specified collection-valued
   * attribute using the given join type.
   * @param attribute target of the join
   * @param joinType join type
   * @return the resulting join
   */
  @Override
  public <Y> Fetch<X, Y> fetch(@Nonnull final PluralAttribute<? super X, ?, Y> attribute,
      @Nonnull final JoinType joinType) {
    throw new NotImplementedException();
  }

  /**
   * Create a fetch join to the specified single-valued attribute
   * using an inner join.
   * @param attribute target of the join
   * @return the resulting fetch join
   */
  @Override
  public <Y> Fetch<X, Y> fetch(@Nonnull final SingularAttribute<? super X, Y> attribute) {
    throw new NotImplementedException();
  }

  /**
   * Create a fetch join to the specified single-valued attribute
   * using the given join type.
   * @param attribute target of the join
   * @param joinType join type
   * @return the resulting fetch join
   */
  @Override
  public <Y> Fetch<X, Y> fetch(@Nonnull final SingularAttribute<? super X, Y> attribute,
      @Nonnull final JoinType joinType) {
    throw new NotImplementedException();
  }

  /**
   * Create a fetch join to the specified attribute using an
   * inner join.
   * @param attributeName name of the attribute for the
   * target of the join
   * @return the resulting fetch join
   * @throws IllegalArgumentException if attribute of the given
   * name does not exist
   */

  @Override
  public <X, Y> Fetch<X, Y> fetch(@Nonnull final String attributeName) {
    throw new NotImplementedException();
  }

  /**
   * Create a fetch join to the specified attribute using
   * the given join type.
   * @param attributeName name of the attribute for the
   * target of the join
   * @param joinType join type
   * @return the resulting fetch join
   * @throws IllegalArgumentException if attribute of the given
   * name does not exist
   */
  @Override
  public <X, Y> Fetch<X, Y> fetch(@Nonnull final String attributeName, @Nonnull final JoinType joinType) {
    throw new NotImplementedException();
  }

  /**
   * Returns the parent <code>From</code> object from which the correlated
   * <code>From</code> object has been obtained through correlation (use
   * of a <code>Subquery</code> <code>correlate</code> method).
   * @return the parent of the correlated From object
   * @throws IllegalStateException if the From object has
   * not been obtained through correlation
   */
  @Override
  public From<Z, X> getCorrelationParent() {
    throw new NotImplementedException();
  }

  /**
   * Return the fetch joins that have been made from this type.
   * Returns empty set if no fetch joins have been made from
   * this type.
   * Modifications to the set do not affect the query.
   * @return fetch joins made from this type
   */
  @Override
  public Set<Fetch<X, ?>> getFetches() {
    return fetches;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends X> getJavaType() {
    return (Class<? extends X>) st.getTypeClass();
  }

  /**
   * Return the joins that have been made from this bound type.
   * Returns empty set if no joins have been made from this
   * bound type.
   * Modifications to the set do not affect the query.
   * @return joins made from this type
   */
  @Override
  public Set<Join<X, ?>> getJoins() {
    return joins;
  }

  /**
   * Whether the <code>From</code> object has been obtained as a result of
   * correlation (use of a <code>Subquery</code> <code>correlate</code>
   * method).
   * @return boolean indicating whether the object has been
   * obtained through correlation
   */
  @Override
  public boolean isCorrelated() {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified Collection-valued
   * attribute.
   * @param collection target of the join
   * @return the resulting join
   */
  @Override
  public <Y> CollectionJoin<X, Y> join(@Nonnull final CollectionAttribute<? super X, Y> collection) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified Collection-valued
   * attribute.
   * @param collection target of the join
   * @return the resulting join
   */
  @Override
  public <Y> CollectionJoin<X, Y> join(@Nonnull final CollectionAttribute<? super X, Y> collection,
      @Nonnull final JoinType joinType) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified List-valued attribute.
   * @param list target of the join
   * @return the resulting join
   */
  @Override
  public <Y> ListJoin<X, Y> join(@Nonnull final ListAttribute<? super X, Y> list) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified List-valued attribute.
   * @param list target of the join
   * @return the resulting join
   */
  @Override
  public <Y> ListJoin<X, Y> join(@Nonnull final ListAttribute<? super X, Y> list, @Nonnull final JoinType joinType) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified Map-valued attribute.
   * @param map target of the join
   * @return the resulting join
   */
  @Override
  public <K, V> MapJoin<X, K, V> join(@Nonnull final MapAttribute<? super X, K, V> map) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified Map-valued attribute.
   * @param map target of the join
   * @return the resulting join
   */
  @Override
  public <K, V> MapJoin<X, K, V> join(@Nonnull final MapAttribute<? super X, K, V> map,
      @Nonnull final JoinType joinType) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified Set-valued attribute.
   * @param set target of the join
   * @return the resulting join
   */
  @Override
  public <Y> SetJoin<X, Y> join(@Nonnull final SetAttribute<? super X, Y> set) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified Set-valued attribute.
   * @param set target of the join
   * @return the resulting join
   */
  @Override
  public <Y> SetJoin<X, Y> join(@Nonnull final SetAttribute<? super X, Y> set, @Nonnull final JoinType joinType) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified single-valued attribute.
   * @param attribute target of the join
   * @return the resulting join
   */
  @Override
  public <Y> Join<X, Y> join(@Nonnull final SingularAttribute<? super X, Y> attribute) {
    throw new NotImplementedException();
  }

  /**
   * Create a join to the specified single-valued attribute
   * using the given join type.
   * @param attribute target of the join
   * @param joinType join type
   * @return the resulting join
   */
  @Override
  public <Y> Join<X, Y> join(@Nonnull final SingularAttribute<? super X, Y> attribute, final JoinType joinType) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified attribute.
   * @param attributeName name of the attribute for the
   * target of the join
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given
   * name does not exist
   */
  @Override
  public <X, Y> Join<X, Y> join(@Nonnull final String attributeName) {
    return join(attributeName, null);
  }

  /**
   * Create a join to the specified attribute using the given join type.
   * @param attributeName name of the attribute for the target of the join
   * @param joinType join type
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given name does not exist
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X, Y> Join<X, Y> join(@Nonnull final String attributeName, final JoinType jt) {

    try {
      final JPAStructuredType source = determineSource();
      final JPAAttribute joinAttribute = source
          .getAttribute(Objects.requireNonNull(attributeName))
          .orElseGet(() -> getAssociation(source, attributeName));

      if (joinAttribute == null)
        throw new IllegalArgumentException(buildExceptionText(attributeName));
      @SuppressWarnings("rawtypes")
      Join join;
      if (joinAttribute instanceof final JPADescriptionAttribute attribute) {
        final JoinType joinType = jt == null ? JoinType.LEFT : jt;
        final Optional<JPAAssociationPath> path = Optional.ofNullable(attribute.asAssociationAttribute().getPath());
        join = new SimpleJoin<>(path.orElseThrow(() -> new IllegalArgumentException(buildExceptionText(
            attributeName))),
            joinType, determineParent(), aliasBuilder, cb);
      } else if (joinAttribute instanceof JPACollectionAttribute) {
        join = new CollectionJoinImpl<>(getJPAPath(joinAttribute), determineParent(), aliasBuilder, cb, jt);
      } else if (joinAttribute.isComplex()) {
        join = new PathJoin<>((FromImpl<X, Y>) determineParent(), getJPAPath(joinAttribute), aliasBuilder, cb);
      } else {
        final JoinType joinType = jt == null ? JoinType.INNER : jt;
        Optional<JPAAssociationPath> associationPath;
        if (path.isPresent())
          associationPath = Optional.ofNullable(st.getAssociationPath(path.get().getAlias() + JPAPath.PATH_SEPARATOR
              + joinAttribute.getExternalName()));
        else
          associationPath = Optional.ofNullable(source.getAssociationPath(joinAttribute.getExternalName()));
        if (associationPath.orElseThrow(() -> new IllegalArgumentException(buildExceptionText(attributeName)))
            .hasJoinTable())
          join = new JoinTableJoin<>(associationPath.orElseThrow(() -> new IllegalArgumentException(
              buildExceptionText(
                  attributeName))), joinType, determineParent(), aliasBuilder, cb);
        else
          join = new SimpleJoin<>(associationPath.orElseThrow(() -> new IllegalArgumentException(buildExceptionText(
              attributeName))), joinType, determineParent(), aliasBuilder, cb);
      }
      joins.add(join);
      return join;
    } catch (ODataJPAModelException | IllegalArgumentException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Create an inner join to the specified Collection-valued attribute.
   * @param attributeName name of the attribute for the target of the join
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given name does not exist
   */
  @Override
  public <X, Y> CollectionJoin<X, Y> joinCollection(@Nonnull final String attributeName) {
    throw new NotImplementedException();
  }

  /**
   * Create a join to the specified Collection-valued attribute using the given join type.
   * @param attributeName name of the attribute for the target of the join
   * @param joinType join type
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given name does not exist
   */
  @Override
  public <X, Y> CollectionJoin<X, Y> joinCollection(@Nonnull final String attributeName,
      @Nonnull final JoinType joinType) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified List-valued attribute.
   * @param attributeName name of the attribute for the
   * target of the join
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given
   * name does not exist
   */
  @Override
  public <X, Y> ListJoin<X, Y> joinList(@Nonnull final String attributeName) {
    throw new NotImplementedException();
  }

  /**
   * Create a join to the specified List-valued attribute using the given join type.
   * @param attributeName name of the attribute for the target of the join
   * @param joinType join type
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given name does not exist
   */
  @Override
  public <X, Y> ListJoin<X, Y> joinList(@Nonnull final String attributeName, @Nonnull final JoinType joinType) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified Map-valued attribute.
   * @param attributeName name of the attribute for the
   * target of the join
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given
   * name does not exist
   */
  @Override
  public <X, K, V> MapJoin<X, K, V> joinMap(@Nonnull final String attributeName) {
    throw new NotImplementedException();
  }

  /**
   * Create a join to the specified Map-valued attribute using the given join type.
   * @param attributeName name of the attribute for the target of the join
   * @param joinType join type
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given name does not exist
   */
  @Override
  public <X, K, V> MapJoin<X, K, V> joinMap(@Nonnull final String attributeName, @Nonnull final JoinType joinType) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified Set-valued attribute.
   * @param attributeName name of the attribute for the
   * target of the join
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given
   * name does not exist
   */
  @Override
  public <X, Y> SetJoin<X, Y> joinSet(@Nonnull final String attributeName) {
    throw new NotImplementedException();
  }

  /**
   * Create a join to the specified Set-valued attribute using the given join type.
   * @param attributeName name of the attribute for the target of the join
   * @param joinType join type
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given name does not exist
   */
  @Override
  public <X, Y> SetJoin<X, Y> joinSet(@Nonnull final String attributeName, @Nonnull final JoinType joinType) {
    throw new NotImplementedException();
  }

  String buildExceptionText(final String attributeName) {
    return "'&a' is unknown at '&e'".replace("&a",
        attributeName).replace("&e", st.getInternalName());
  }

  Expression<Boolean> createInheritanceWhere() {
    if (inInfo.getInheritanceType().filter(type -> type == InheritanceType.SINGLE_TABLE).isPresent()) {
      return createInheritanceWhereSingleTable();
    } else if (inInfo.getInheritanceType().filter(type -> type == InheritanceType.JOINED).isPresent()) {
      return createInheritanceWhereJoined();
    }
    return null;
  }

  private final Expression<Boolean> createInheritanceWhereJoined() {
    // attribute from base, value from leave
    final Optional<String> columnName = inInfo.getDiscriminatorColumn();
//    if (!columnName.isPresent()) {
//      LOGGER.warn("Now discriminator column found at " + inInfo.getBaseClass().map(Class::getCanonicalName).orElse(
//          "?"));
//    } else {
//      if (!(this instanceof AbstractJoinImp)) {
//        var root = getInheritanceRoot();
//        final List<JPAPath> pathList = getInheritanceRootPathList(root);
//        final Path<?> columnPath = getDiscriminatorColumn(columnName, root, pathList);
//        final DiscriminatorValue value = st.getTypeClass().getDeclaredAnnotation(DiscriminatorValue.class);
//        if (value == null || columnPath == null)
//          throw new IllegalStateException("DiscriminatorValue annotation missing at " + st.getTypeClass()
//              .getCanonicalName());
//        return cb.equal(columnPath, value.value());
//      }
//    }
    if (columnName.isPresent()) {
      LOGGER.warn("Discriminator column found at " +
          inInfo.getBaseClass().map(Class::getCanonicalName).orElse("?"));
      LOGGER.warn("Discriminator columns are ignored in case of inheritance type JOINED");
    }
    return null;
  }

  private final Path<?> getDiscriminatorColumn(final Optional<String> columnName, Optional<FromImpl<?, ?>> root,
      final List<JPAPath> pathList) {
    if (columnName.isPresent() && root.isPresent()) {
      PathImpl<?> parent = root.get();
      Path<?> columnPath = null;
      for (final JPAPath p : pathList) {
        if (p.getDBFieldName().equals(columnName.get()))
          columnPath = new PathImpl<>(p, Optional.of(parent), root.get().st, tableAlias);
      }
      return columnPath;
    }
    return null;
  }

  private final List<JPAPath> getInheritanceRootPathList(Optional<FromImpl<?, ?>> root) {

    if (root.isPresent()) {
      try {
        return root.get().st.getPathList();
      } catch (ODataJPAModelException e) {
        throw new InternalServerError(e);
      }
    }
    return List.of();
  }

  @SuppressWarnings("unchecked")
  private final Optional<FromImpl<?, ?>> getInheritanceRoot() {
    var from = this;
    while (from != null && from.inheritanceJoin.isPresent())
      from = (FromImpl<Z, X>) from.inheritanceJoin.get();
    return Optional.ofNullable(from);
  }

  private final Expression<Boolean> createInheritanceWhereSingleTable() {
    final Optional<String> columnName = inInfo.getDiscriminatorColumn();
    if (!columnName.isPresent())
      throw new IllegalStateException("DiscriminatorColumn annotation missing at " + st.getTypeClass().getSuperclass()
          .getCanonicalName());
    Path<?> columnPath = null;
    try {
      for (final JPAPath a : st.getPathList()) {
        if (a.getDBFieldName().equals(columnName.get()))
          columnPath = new PathImpl<>(a, parent, st, tableAlias);
      }
    } catch (final ODataJPAModelException e) {
      throw new IllegalStateException("Internal server error", e);
    }

    final DiscriminatorValue value = st.getTypeClass().getDeclaredAnnotation(DiscriminatorValue.class);
    if (value == null)
      throw new IllegalStateException("DiscriminatorValue annotation missing at " + st.getTypeClass()
          .getCanonicalName());
    return cb.equal(columnPath, value.value());
  }

  @SuppressWarnings("unchecked")
  <U, V> FromImpl<U, V> determineParent() {
    return (FromImpl<U, V>) this;
  }

  JPAStructuredType determineSource() throws ODataJPAModelException {
    final JPAStructuredType source;
    if (path.isPresent() && path.get().getLeaf().isComplex()) {
      source = path.get().getLeaf().getStructuredType();
    } else {
      source = st;
    }
    return source;
  }

  @Override
  List<JPAPath> getPathList() {
    try {
      return st.getPathList();
    } catch (final ODataJPAModelException e) {
      throw new IllegalStateException(e);
    }
  }

  Optional<InheritanceType> getInheritanceType() {
    return inInfo.getInheritanceType();
  }

  Optional<? extends AbstractJoinImp<X, ?>> getInheritanceJoin() {
    return inheritanceJoin;
  }

  private JPAPath determinePath(final JPAAttribute joinAttribute) throws ODataJPAModelException {
    if (path.isPresent())
      return st.getPath(path.get().getAlias() + JPAPath.PATH_SEPARATOR + joinAttribute.getExternalName());
    return st.getPath(joinAttribute.getExternalName());
  }

  @Nonnull
  private JPAPath getJPAPath(final JPAAttribute joinAttribute) throws ODataJPAModelException {
    final var jpaPath = determinePath(joinAttribute);
    if (jpaPath != null)
      return jpaPath;
    else
      throw new IllegalStateException();
  }

  private JPAAssociationAttribute getAssociation(final JPAStructuredType source, final String attributeName) {
    try {
      return source.getAssociation(attributeName);
    } catch (final ODataJPAModelException e) {
      throw new IllegalArgumentException(buildExceptionText(attributeName), e);
    }
  }

  private boolean isSubtype(final Class<?> type) {
    return st.getTypeClass().isAssignableFrom(type);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object object) {
    return super.equals(object);
  }

  Optional<String> getAlias(JPAPath jpaPath) {

    if (isKeyPath(jpaPath))
      return tableAlias;
    if (inheritanceJoin.isPresent()) {
      return Optional.ofNullable(
          inheritanceJoin.get().getAlias(jpaPath)
              .orElseGet(() -> getOwnAlias(jpaPath).orElse(null)));
    }
    return getOwnAlias(jpaPath);

  }

  final Optional<String> getOwnAlias(JPAPath jpaPath) {
    try {
      if (st.getDeclaredAttribute(jpaPath.getPath().get(0).getInternalName()).isPresent()) {
        return tableAlias;
      }
      return Optional.empty();
    } catch (ODataJPAModelException e) {
      throw new InternalServerError(e);
    }

  }

}