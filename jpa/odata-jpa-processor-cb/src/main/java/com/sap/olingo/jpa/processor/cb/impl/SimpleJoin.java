package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Objects;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;

/**
 *
 * @author Oliver Grande
 * @since 1.0.0
 * @created 21.11.2020
 *
 * @param <Z> the source type of the join
 * @param <X> the target type of the join
 */
class SimpleJoin<Z, X> extends AbstractJoinImp<Z, X> {

  private final JoinType joinType;
  final JPAAssociationPath association;

  SimpleJoin(@Nonnull final JPAAssociationPath path, @Nonnull final JoinType jt,
      @Nonnull final From<?, Z> parent, @Nonnull final AliasBuilder aliasBuilder, @Nonnull final CriteriaBuilder cb)
      throws ODataJPAModelException {

    super((JPAEntityType) path.getTargetType(), parent, aliasBuilder, cb);
    this.joinType = jt;
    this.association = path;
    createOn(association.getJoinColumnsList(), (JPAEntityType) association.getTargetType());
  }

  /**
   * Return the metamodel attribute corresponding to the join.
   * @return metamodel attribute corresponding to the join
   */
  @Override
  public Attribute<? super Z, ?> getAttribute() {
    throw new NotImplementedException();
  }

  /**
   * Return the join type.
   * @return join type
   */
  @Override
  public JoinType getJoinType() {
    return joinType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hash(association, joinType);
    return result;
  }

  @Override
  public boolean equals(final Object object) {
    return object instanceof SimpleJoin<?, ?> other
        && Objects.equals(association, other.association)
        && joinType == other.joinType;
  }

}
