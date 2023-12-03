package com.sap.olingo.jpa.processor.cb.impl;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;

class SubqueryRootImpl<X> extends FromImpl<X, X> implements Root<X> {

  private final Subquery<X> query;

  SubqueryRootImpl(@Nonnull final ProcessorSubquery<X> inner, @Nonnull final AliasBuilder ab,
      @Nonnull final JPAServiceDocument sd) throws ODataJPAModelException {

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
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public <Y> Path<Y> get(final String attributeName) {

    for (final Selection<?> selection : query.getCompoundSelectionItems()) {
      if (selection instanceof final SelectionImpl<?> selectionImpl) {
        final Selection<?> x = selectionImpl.selection;

        if (x instanceof PathImpl<?>) {
          if (x.getAlias().equals(attributeName)
              || ((PathImpl<?>) x).path
                  .orElseThrow(IllegalStateException::new)
                  .getAlias().equals(attributeName)
              || ((PathImpl<?>) x).path
                  .orElseThrow(IllegalStateException::new)
                  .getLeaf().getInternalName().equals(attributeName)) {
            return new SelectionPath(selectionImpl, tableAlias);
          }
        } else if (x instanceof WindowFunctionExpression<?>
            && x.getAlias().equals(attributeName)) {
          return ((WindowFunctionExpression<Y>) x).asPath(tableAlias.orElse(""));
        }
      }
    }
    return super.get(attributeName);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + query.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object object) {
    return (object instanceof final SubqueryRootImpl<?> other)
        && super.equals(other)
        && query.equals(other.query);
  }
}
