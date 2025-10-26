package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Objects;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exceptions.InternalServerError;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;

/**
 * Represents a join for an inheritance relation
 * @param <Z> the sub-type
 * @param <X> the super-type
 *
 * @author Oliver Grande
 * @since 2.4.0
 * @created 21.10.2025
 *
 */
class InheritanceJoin<Z, X> extends AbstractJoinImp<Z, X> {

  private final JPAEntityType subType;

  InheritanceJoin(@Nonnull final JPAEntityType subType, @Nonnull final From<?, Z> parent,
      @Nonnull final AliasBuilder aliasBuilder, @Nonnull final CriteriaBuilder cb) {
    super(getSuperType(subType), parent, aliasBuilder, cb);
    this.subType = subType;
  }

  @Override
  public Predicate getOn() {
    if (on == null)
      try {
        createOn(subType.getInheritanceInformation().getJoinColumnsList(), subType);
      } catch (ODataJPAModelException e) {
        throw new InternalServerError(e);
      }
    return on;
  }

  @Nonnull
  private static final JPAEntityType getSuperType(final JPAEntityType subType) {
    try {
      return Objects.requireNonNull((JPAEntityType) subType.getBaseType());
    } catch (ODataJPAModelException e) {
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

}
