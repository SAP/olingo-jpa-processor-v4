package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;

/**
 *
 * @author Oliver Grande
 * @since 1.0.1
 * @created 29.12.2020
 * @param <X>
 */
class SelectionPath<X> extends ExpressionImpl<X> implements Path<X> {
  final SqlSelection<X> selection;
  final Optional<String> tableAlias;

  SelectionPath(@Nonnull final SqlSelection<X> selImpl, @Nonnull final Optional<String> tableAlias) {
    this.selection = selImpl;
    this.tableAlias = tableAlias;
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    tableAlias.ifPresent(p -> {
      statement.append(p);
      statement.append(DOT);
    });
    return statement.append(selection.getAlias().replaceAll(SELECTION_REPLACMENT_REGEX, SELECTION_REPLACMENT));
  }

  @Override
  public <Y> Path<Y> get(final SingularAttribute<? super X, Y> arg0) {
    throw new NotImplementedException();
  }

  @Override
  public <E, C extends Collection<E>> Expression<C> get(final PluralAttribute<X, C, E> arg0) {
    throw new NotImplementedException();
  }

  @Override
  public <K, V, M extends Map<K, V>> Expression<M> get(final MapAttribute<X, K, V> arg0) {
    throw new NotImplementedException();
  }

  @Override
  public <Y> Path<Y> get(final String arg0) {
    throw new NotImplementedException();
  }

  @Override
  public Bindable<X> getModel() {
    throw new NotImplementedException();
  }

  @Override
  public Path<?> getParentPath() {
    throw new NotImplementedException();
  }

  @Override
  public Expression<Class<? extends X>> type() {
    throw new NotImplementedException();
  }
}
