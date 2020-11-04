package com.sap.olingo.jpa.processor.cb.impl;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

class PathJoin<Z, X> extends AbstractJoinImp<Z, X> {

  private final FromImpl<?, Z> parentFrom;

  PathJoin(@Nonnull final FromImpl<?, Z> parent, @Nonnull final JPAPath joinAttribute,
      @Nonnull final AliasBuilder aliasBuilder, @Nonnull final CriteriaBuilder cb) {

    super(parent.st, parent, joinAttribute, aliasBuilder, cb);
    this.parentFrom = parent;
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
    return JoinType.INNER;
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    if (!getJoins().isEmpty()) {
      getJoins().stream().collect(new StringBuilderCollector.ExpressionCollector(statement, " "));
    }
    return statement;
  }

  @Override
  FromImpl<?, ?> determineParent() {
    return parentFrom.determineParent();
  }

  @Override
  Expression<Boolean> createInheritanceWhere() {
    return null;
  }
}
