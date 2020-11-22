package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;

class SubqueryRootImpl<X> extends FromImpl<X, X> implements Root<X> {

  private final Subquery<X> query;

  SubqueryRootImpl(@Nonnull final ProcessorSubquery<X> inner, @Nonnull final AliasBuilder ab,
      final JPAServiceDocument sd) throws ODataJPAModelException {

    super(sd.getEntity(inner.getJavaType()), ab, null);
    this.query = inner;
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    statement.append(OPENING_BRACKET);
    ((SqlConvertible) query).asSQL(statement);
    statement.append(CLOSING_BRACKET)
        .append(" AS ")
        .append(tableAlias
            .orElseThrow(() -> new IllegalStateException("Missing table alias for a sub query in FROM clause")));

    return statement;
  }

  @Override
  public EntityType<X> getModel() {
    throw new NotImplementedException();
  }

  /**
   * @param attributeName name of the attribute
   * @return path corresponding to the referenced attribute
   * @throws IllegalStateException if invoked on a path that
   * corresponds to a basic type
   * @throws IllegalArgumentException if attribute of the given
   * name does not otherwise exist
   **/
  @SuppressWarnings("unchecked")
  @Override
  public <Y> Path<Y> get(final String attributeName) {

    final Optional<Selection<?>> selection = query.getCompoundSelectionItems()
        .stream()
        .filter(s -> s.getAlias().equals(attributeName))
        .findFirst();
    if (selection.isPresent()) {
      final Selection<?> s = selection.get();
      if (s instanceof Path<?>)
        return (Path<Y>) s;
      if (s instanceof WindowFunctionExpression<?>)
        return ((WindowFunctionExpression<Y>) s).asPath(tableAlias.orElse(""));
    } else {
      return super.get(attributeName);
    }
    throw new IllegalArgumentException("Attribute unknown: " + attributeName);
  }
}
