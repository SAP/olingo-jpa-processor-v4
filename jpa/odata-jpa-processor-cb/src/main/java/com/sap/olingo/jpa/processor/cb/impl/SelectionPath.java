package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.Bindable;
import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;

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

  SelectionPath(@Nonnull final SqlSelection<X> selection, @Nonnull final Optional<String> tableAlias) {
    this.selection = selection;
    this.tableAlias = tableAlias;
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    tableAlias.ifPresent(alias -> {
      statement.append(alias);
      statement.append(DOT);
    });
    return statement.append(selection.getAlias().replaceAll(SELECTION_REPLACEMENT_REGEX, SELECTION_REPLACEMENT));
  }

  @Override
  public <Y> Path<Y> get(final SingularAttribute<? super X, Y> attribute) {
    throw new NotImplementedException();
  }

  @Override
  public <E, C extends Collection<E>> Expression<C> get(final PluralAttribute<X, C, E> attribute) {
    throw new NotImplementedException();
  }

  @Override
  public <K, V, M extends Map<K, V>> Expression<M> get(final MapAttribute<X, K, V> attribute) {
    throw new NotImplementedException();
  }

  @Override
  public <Y> Path<Y> get(final String attribute) {
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
