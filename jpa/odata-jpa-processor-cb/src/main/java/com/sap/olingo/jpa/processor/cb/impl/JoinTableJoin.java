package com.sap.olingo.jpa.processor.cb.impl;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

class JoinTableJoin<Z, X> extends AbstractJoinImp<Z, X> {
  private final JPAAssociationPath association;
  private boolean inner = false;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  JoinTableJoin(@Nonnull final JPAAssociationPath path, @Nonnull final JoinType jt,
      @Nonnull final From<?, Z> parent, @Nonnull final AliasBuilder aliasBuilder, @Nonnull final CriteriaBuilder cb)
      throws ODataJPAModelException {

    super((JPAEntityType) path.getTargetType(), (From<?, Z>) new InnerJoin(parent, aliasBuilder, cb, path, jt),
        aliasBuilder, cb);
    this.association = path;
    related.getJoins().add(this);
    createOn(association.getJoinTable().getRawInverseJoinInformation());
  }

  @Override
  public Attribute<? super Z, ?> getAttribute() {
    throw new NotImplementedException();
  }

  @Override
  public JoinType getJoinType() {
    return JoinType.INNER;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  private static class InnerJoin<Z, X> extends AbstractJoinImp<Z, X> {
    private final JoinType joinType;
    private final JPAAssociationPath association;

    public InnerJoin(final From<?, Z> parent, @Nonnull final AliasBuilder ab, @Nonnull final CriteriaBuilder cb,
        @Nonnull final JPAAssociationPath path, final JoinType jt) {

      super(path.getJoinTable().getEntityType(), parent, ab, cb);
      this.joinType = jt;
      this.association = path;
      createOn(path.getJoinTable().getRawJoinInformation());
    }

    @Override
    public Attribute<? super Z, ?> getAttribute() {
      throw new NotImplementedException();
    }

    @Override
    public JoinType getJoinType() {
      return joinType;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {

      statement.append(" ")
          .append(SqlJoinType.byJoinType(getJoinType()))
          .append(" ");
      if (!getJoins().isEmpty())
        statement.append(OPENING_BRACKET);
      statement.append(association.getJoinTable().getTableName());
      tableAlias.ifPresent(p -> statement.append(" ").append(p));
      statement.append(getJoins().stream().collect(new StringBuilderCollector.ExpressionCollector(statement, " ")));
      if (!getJoins().isEmpty())
        statement.append(CLOSING_BRACKET);
      statement.append(" ON ");
      ((SqlConvertible) on).asSQL(statement);
      return statement;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
      // As always a new alias is created super call is sufficant
      return super.equals(obj);
    }

  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    if (inner) {
      return super.asSQL(statement);
    } else {
      inner = true;
      return ((SqlConvertible) related).asSQL(statement);
    }
  }
}
