package com.sap.olingo.jpa.processor.cb.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

/**
 * Represents a bound type, usually an entity that appears in
 * the from clause, but may also be an embeddable belonging to
 * an entity in the from clause.
 * <p> Serves as a factory for Joins of associations, embeddables, and
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

  private final Set<Join<X, ?>> joins;
  private final Set<Fetch<X, ?>> fetches;
  private final AliasBuilder aliasBuilder;
  private InheritanceType inType;
  private final CriteriaBuilder cb;

  FromImpl(final JPAEntityType type, final AliasBuilder ab, final CriteriaBuilder cb) {
    this(type, null, ab, cb);
  }

  FromImpl(final JPAEntityType type, final JPAPath path, final AliasBuilder ab, final CriteriaBuilder cb) {
    super(Optional.ofNullable(path), Optional.empty(), type, Optional.of(ab.getNext()));
    this.joins = new HashSet<>();
    this.fetches = new HashSet<>();
    this.aliasBuilder = ab;
    this.cb = cb;
    this.inType = determineInheritanceType(type);
  }

  /**
   * Perform a typecast upon the expression, returning a new expression object.
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
        st = target;
        inType = determineInheritanceType(target);
      }
      return (Expression<X>) this;
    } catch (final ODataJPAModelException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    statement.append(st.getTableName());
    tableAlias.ifPresent(p -> statement.append(" ").append(p));
    joins.stream().collect(new StringBuilderCollector.ExpressionCollector(statement, " "));
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
   * @param jt join type
   * @return the resulting join
   */
  @Override
  public <Y> Fetch<X, Y> fetch(@Nonnull final PluralAttribute<? super X, ?, Y> attribute, @Nonnull final JoinType jt) {
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
   * @param jt join type
   * @return the resulting fetch join
   */
  @Override
  public <Y> Fetch<X, Y> fetch(@Nonnull final SingularAttribute<? super X, Y> attribute, @Nonnull final JoinType jt) {
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
   * @param jt join type
   * @return the resulting fetch join
   * @throws IllegalArgumentException if attribute of the given
   * name does not exist
   */
  @Override
  public <X, Y> Fetch<X, Y> fetch(@Nonnull final String attributeName, @Nonnull final JoinType jt) {
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
    return join(collection, JoinType.INNER);
  }

  /**
   * Create an inner join to the specified Collection-valued
   * attribute.
   * @param collection target of the join
   * @return the resulting join
   */
  @Override
  public <Y> CollectionJoin<X, Y> join(@Nonnull final CollectionAttribute<? super X, Y> collection,
      @Nonnull final JoinType jt) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified List-valued attribute.
   * @param list target of the join
   * @return the resulting join
   */
  @Override
  public <Y> ListJoin<X, Y> join(@Nonnull final ListAttribute<? super X, Y> list) {
    return join(list, JoinType.INNER);
  }

  /**
   * Create an inner join to the specified List-valued attribute.
   * @param list target of the join
   * @return the resulting join
   */
  @Override
  public <Y> ListJoin<X, Y> join(@Nonnull final ListAttribute<? super X, Y> list, @Nonnull final JoinType jt) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified Map-valued attribute.
   * @param map target of the join
   * @return the resulting join
   */
  @Override
  public <K, V> MapJoin<X, K, V> join(@Nonnull final MapAttribute<? super X, K, V> map) {
    return join(map, JoinType.INNER);
  }

  /**
   * Create an inner join to the specified Map-valued attribute.
   * @param map target of the join
   * @return the resulting join
   */
  @Override
  public <K, V> MapJoin<X, K, V> join(@Nonnull final MapAttribute<? super X, K, V> map, @Nonnull final JoinType jt) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified Set-valued attribute.
   * @param set target of the join
   * @return the resulting join
   */
  @Override
  public <Y> SetJoin<X, Y> join(@Nonnull final SetAttribute<? super X, Y> set) {
    return join(set, JoinType.INNER);
  }

  /**
   * Create an inner join to the specified Set-valued attribute.
   * @param set target of the join
   * @return the resulting join
   */
  @Override
  public <Y> SetJoin<X, Y> join(@Nonnull final SetAttribute<? super X, Y> set, @Nonnull final JoinType jt) {
    throw new NotImplementedException();
  }

  /**
   * Create an inner join to the specified single-valued attribute.
   * @param attribute target of the join
   * @return the resulting join
   */
  @Override
  public <Y> Join<X, Y> join(@Nonnull final SingularAttribute<? super X, Y> attribute) {
    return join(attribute, JoinType.INNER);
  }

  /**
   * Create a join to the specified single-valued attribute
   * using the given join type.
   * @param attribute target of the join
   * @param jt join type
   * @return the resulting join
   */
  @Override
  public <Y> Join<X, Y> join(@Nonnull final SingularAttribute<? super X, Y> attribute, final JoinType jt) {
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
   * @param jt join type
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
          .orElse(getAssociation(source, attributeName));

      if (joinAttribute == null)
        throw new IllegalArgumentException(buildExceptionText(attributeName));
      final JPAPath joinPath = determinePath(joinAttribute);
      @SuppressWarnings("rawtypes")
      Join join;
      if (joinAttribute instanceof JPADescriptionAttribute) {
        final JoinType joinType = jt == null ? JoinType.LEFT : jt;
        final Optional<JPAAssociationPath> path = Optional.ofNullable(((JPADescriptionAttribute) joinAttribute)
            .asAssociationAttribute().getPath());
        join = new SimpleJoin<>(path.orElseThrow(() -> new IllegalArgumentException(buildExceptionText(attributeName))),
            joinType, determineParent(), aliasBuilder, cb);
      } else if (joinAttribute instanceof JPACollectionAttribute) {
        join = new CollectionJoinImpl<>(joinPath, determineParent(), aliasBuilder, cb);
      } else if (joinAttribute.isComplex()) {
        join = new PathJoin<>((FromImpl<X, Y>) determineParent(), joinPath, aliasBuilder, cb);
      } else {
        final JoinType joinType = jt == null ? JoinType.INNER : jt;
        Optional<JPAAssociationPath> associationPath;
        if (path.isPresent())
          associationPath = Optional.ofNullable(st.getAssociationPath(path.get().getAlias() + JPAPath.PATH_SEPARATOR
              + joinAttribute.getExternalName()));
        else
          associationPath = Optional.ofNullable(source.getAssociationPath(joinAttribute
              .getExternalName()));
        if (associationPath.orElseThrow(() -> new IllegalArgumentException(buildExceptionText(attributeName)))
            .hasJoinTable())
          join = new JoinTableJoin<>(associationPath.orElseThrow(() -> new IllegalArgumentException(buildExceptionText(
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
    return joinCollection(attributeName, JoinType.INNER);
  }

  /**
   * Create a join to the specified Collection-valued attribute using the given join type.
   * @param attributeName name of the attribute for the target of the join
   * @param jt join type
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given name does not exist
   */
  @Override
  public <X, Y> CollectionJoin<X, Y> joinCollection(@Nonnull final String attributeName, @Nonnull final JoinType jt) {
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
    return joinList(attributeName, JoinType.INNER);
  }

  /**
   * Create a join to the specified List-valued attribute using the given join type.
   * @param attributeName name of the attribute for the target of the join
   * @param jt join type
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given name does not exist
   */
  @Override
  public <X, Y> ListJoin<X, Y> joinList(@Nonnull final String attributeName, @Nonnull final JoinType jt) {
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
    return joinMap(attributeName, JoinType.INNER);
  }

  /**
   * Create a join to the specified Map-valued attribute using the given join type.
   * @param attributeName name of the attribute for the target of the join
   * @param jt join type
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given name does not exist
   */
  @Override
  public <X, K, V> MapJoin<X, K, V> joinMap(@Nonnull final String attributeName, @Nonnull final JoinType jt) {
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
    return joinSet(attributeName, JoinType.INNER);
  }

  /**
   * Create a join to the specified Set-valued attribute using the given join type.
   * @param attributeName name of the attribute for the target of the join
   * @param jt join type
   * @return the resulting join
   * @throws IllegalArgumentException if attribute of the given name does not exist
   */
  @Override
  public <X, Y> SetJoin<X, Y> joinSet(@Nonnull final String attributeName, @Nonnull final JoinType jt) {
    throw new NotImplementedException();
  }

  String buildExceptionText(final String attributeName) {
    return "'&a' is unknown at '&e'".replace("&a",
        attributeName).replace("&e", st.getInternalName());
  }

  Expression<Boolean> createInheritanceWhere() {
    if (inType == InheritanceType.SINGLE_TABLE) {
      final DiscriminatorColumn discriminatorColumn = st.getTypeClass().getSuperclass().getDeclaredAnnotation(
          DiscriminatorColumn.class);
      if (discriminatorColumn == null)
        throw new IllegalStateException("DiscriminatorColumn annotation missing at " + st.getTypeClass().getSuperclass()
            .getCanonicalName());
      final String columnName = discriminatorColumn.name();
      Path<?> columnPath = null;
      try {
        for (final JPAPath a : st.getPathList()) {
          if (a.getDBFieldName().equals(columnName))
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
    return null;
  }

  FromImpl<?, ?> determineParent() {
    return this;
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

  private InheritanceType determineInheritanceType(final JPAEntityType et) {
    if (et != null && et.getTypeClass().getSuperclass() != null) {
      final Inheritance inheritance = et.getTypeClass().getSuperclass().getDeclaredAnnotation(Inheritance.class);
      if (inheritance != null)
        return inheritance.strategy();
    }
    return null;
  }

  private JPAPath determinePath(final JPAAttribute joinAttribute) throws ODataJPAModelException {
    if (path.isPresent())
      return st.getPath(path.get().getAlias() + JPAPath.PATH_SEPARATOR + joinAttribute.getExternalName());
    return st.getPath(joinAttribute.getExternalName());
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
}