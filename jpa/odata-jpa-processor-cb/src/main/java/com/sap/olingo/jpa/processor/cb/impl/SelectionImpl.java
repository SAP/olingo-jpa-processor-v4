package com.sap.olingo.jpa.processor.cb.impl;

import static com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.SELECTION_REPLACMENT;
import static com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.SELECTION_REPLACMENT_REGEX;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;

/**
 *
 * @author Oliver Grande
 *
 * @param <X> the type of the selection item
 */
final class SelectionImpl<X> implements SqlSelection<X> {
  private Optional<String> alias;
  private final Class<X> resultType;
  final Selection<?> selection;
  protected Optional<List<Map.Entry<String, JPAPath>>> resolvedSelection = Optional.empty();
  protected final AliasBuilder aliasBuilder;

  SelectionImpl(final Selection<?> selection, final Class<X> resultType, final AliasBuilder aliasBuilder) {
    this.resultType = resultType;
    this.selection = selection;
    if (selection instanceof Path<?>)
      // Use a generated alias for standard columns
      this.alias = Optional.empty();
    else
      // Take the given alias for ROW_NUMBER, so that no mapping is needed e.g. when used in WHERE clause
      this.alias = Optional.ofNullable(selection.getAlias() == null || selection.getAlias().isEmpty()
          ? null : selection.getAlias());

    this.aliasBuilder = aliasBuilder;
  }

  /**
   * Assigns an alias to the selection item.
   * Once assigned, an alias cannot be changed or reassigned.
   * Returns the same selection item.
   * @param name alias
   * @return selection item
   */
  @Override
  public SqlSelection<X> alias(@Nonnull final String name) {
    if (!alias.isPresent())
      alias = Optional.of(name);
    return this;
  }

  @Override
  public StringBuilder asSQL(@Nonnull final StringBuilder statement) {

    return ((SqlConvertible) selection)
        .asSQL(statement)
        .append(" ")
        .append(getAlias().replaceAll(SELECTION_REPLACMENT_REGEX, SELECTION_REPLACMENT));
  }

  /**
   * Return the alias assigned to the tuple element or creates on,
   * if no alias has been assigned.
   * @return alias if not set returns an empty string
   */
  @Override
  public String getAlias() {
    return alias.orElseGet(this::createAlias);
  }

  /**
   * Return the selection items composing a compound selection.
   * Modifications to the list do not affect the query.
   * @return list of selection items
   * @throws IllegalStateException if selection is not a
   * compound selection
   */
  @Override
  public List<Selection<?>> getCompoundSelectionItems() {
    throw new IllegalStateException("Call of getCompoundSelectionItems on single selection");
  }

  /**
   * Return the Java type of the tuple element.
   * @return the Java type of the tuple element
   */
  @Override
  public Class<? extends X> getJavaType() {
    return resultType;
  }

  @Override
  public List<Map.Entry<String, JPAPath>> getResolvedSelection() {
    return resolvedSelection.orElseGet(this::resolveSelectionLate);
  }

  /**
   * Whether the selection item is a compound selection.
   * @return boolean indicating whether the selection is a compound
   * selection
   */
  @Override
  public boolean isCompoundSelection() {
    return false;
  }

  protected List<Map.Entry<String, JPAPath>> resolveSelectionLate() {
    return Collections.emptyList();
  }

  private String createAlias() {
    alias = Optional.of(aliasBuilder.getNext());
    return alias.get();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Selection<X> getSelection() {
    return (Selection<X>) selection;
  }
}