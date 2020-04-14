package com.sap.olingo.jpa.processor.cb.impl;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.api.SqlConvertable;
import com.sap.olingo.jpa.processor.cb.api.SqlJoinType;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

class JoinTableJoin<Z, X> extends AbstractJoinImp<Z, X> {
  private final JPAAssociationPath assoziation;
  private boolean inner = false;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  JoinTableJoin(@Nonnull final JPAAssociationPath path, @Nonnull final JoinType jt,
      @Nonnull final From<?, Z> parent, @Nonnull final AliasBuilder aliasBuilder, @Nonnull CriteriaBuilder cb)
      throws ODataJPAModelException {

    super((JPAEntityType) path.getTargetType(), (From<?, Z>) new InnerJoin(parent, aliasBuilder, cb, path, jt),
        aliasBuilder, cb);
    this.assoziation = path;
    related.getJoins().add(this);
    createOn(assoziation.getJoinTable().getRawInversJoinInformation());
  }

  @Override
  public Attribute<? super Z, ?> getAttribute() {
    throw new NotImplementedException();
  }

  @Override
  public JoinType getJoinType() {
    return JoinType.INNER;
  }

  private static class InnerJoin<Z, X> extends AbstractJoinImp<Z, X> {
    private final JoinType joinType;
    private final JPAAssociationPath association;

    public InnerJoin(final From<?, Z> parent, @Nonnull final AliasBuilder ab, @Nonnull CriteriaBuilder cb,
        @Nonnull JPAAssociationPath path, JoinType jt) {

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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public StringBuilder asSQL(final StringBuilder statment) {

      statment.append(" ")
          .append(SqlJoinType.byJoinType(getJoinType()))
          .append(" ");
      if (!getJoins().isEmpty())
        statment.append(OPENING_BRACKET);
      statment.append(association.getJoinTable().getTableName());
      tableAlias.ifPresent(p -> statment.append(" ").append(p));
      getJoins().stream().collect(new StringBuilderCollector.ExpressionCollector(statment, " "));
      if (!getJoins().isEmpty())
        statment.append(CLOSING_BRACKET);
      statment.append(" ON ");
      ((SqlConvertable) on).asSQL(statment);
      return statment;
    }
  }

  @Override
  public StringBuilder asSQL(StringBuilder statment) {
    if (inner) {
      return super.asSQL(statment);
    } else {
      inner = true;
      return ((SqlConvertable) related).asSQL(statment);
    }
  }
}
