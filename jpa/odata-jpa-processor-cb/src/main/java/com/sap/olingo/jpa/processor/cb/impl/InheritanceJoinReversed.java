package com.sap.olingo.jpa.processor.cb.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exceptions.InternalServerError;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;

/**
 * Represents a reversed join for an inheritance relation. From super-type to sub-type
 * @param <Z> the sub-type
 * @param <X> the super-type
 *
 * @author Oliver Grande
 * @since 2.4.0
 * @created 21.10.2025
 *
 */
class InheritanceJoinReversed<Z, X> extends AbstractJoinImp<Z, X> {

  private final JPAEntityType subType;
  private List<FromImpl<?, ?>> inheritanceHierarchy;

  InheritanceJoinReversed(@Nonnull final JPAEntityType targetType, @Nonnull final JPAEntityType superType,
      @Nonnull final From<?, Z> root, @Nonnull final AliasBuilder aliasBuilder, @Nonnull final CriteriaBuilder cb) {
    super(getHop(superType, targetType), root, aliasBuilder, cb);
    this.subType = getHop(superType, targetType);
    this.inheritanceJoin = Optional.empty();
    this.getJoins().clear();
    if (this.st != targetType) {
      this.inheritanceJoin = Optional.of(
          new InheritanceJoinReversed<>(targetType, this.st, this, aliasBuilder, cb));
      this.inheritanceJoin.ifPresent(this.getJoins()::add);
    }
  }

  @Override
  public Predicate getOn() {
    if (on == null) {
      try {
        createOn(subType.getInheritanceInformation().getReversedJoinColumnsList(), subType);
      } catch (final ODataJPAModelException e) {
        throw new InternalServerError(e);
      }
    }
    return on;
  }

  private static JPAEntityType getHop(@Nonnull final JPAEntityType superType, @Nonnull final JPAEntityType targetType) {
    try {
      JPAStructuredType result = targetType;
      while (result.getBaseType() != null && result.getBaseType() != superType) // NOSONAR
        result = result.getBaseType();
      return (JPAEntityType) result;
    } catch (final ODataJPAModelException e) {
      throw new InternalServerError(e);
    }
  }

  @Override
  public Attribute<? super Z, ?> getAttribute() {
    throw new NotImplementedException();
  }

  @Override
  public JoinType getJoinType() {
    return JoinType.INNER;
  }

  @SuppressWarnings("unchecked")
  public From<Object, Object> getTarget() {
    return inheritanceJoin.map(join -> (InheritanceJoinReversed<?, ?>) join)
        .map(InheritanceJoinReversed::getTarget).orElse((From<Object, Object>) this);
  }

  @Override
  Optional<String> getAlias(final JPAPath jpaPath) {
    if (isKeyPath(jpaPath))
      return tableAlias;
    final var hierarchy = getInheritanceHierarchy();
    for (int i = hierarchy.size() - 1; i >= 0; i--) {
      final var alias = hierarchy.get(i).getOwnAlias(jpaPath);
      if (alias.isPresent())
        return alias;
    }
    return getOwnAlias(jpaPath);
  }

  private List<FromImpl<?, ?>> getInheritanceHierarchy() {
    if (inheritanceHierarchy == null) {
      inheritanceHierarchy = new ArrayList<>();
      AbstractJoinImp<?, ?> start = this;
      while (start != null &&
          start.related != null &&
          start.related instanceof AbstractJoinImp<?, ?>) {
        inheritanceHierarchy.add((FromImpl<?, ?>) start.related);
        start = (AbstractJoinImp<?, ?>) start.related;
      }
    }
    return inheritanceHierarchy;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    return super.equals(other);
  }
}
