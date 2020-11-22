package com.sap.olingo.jpa.processor.cb.impl;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;

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
}
