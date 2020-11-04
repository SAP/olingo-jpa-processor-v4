package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Selection;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.cb.api.ProcessorSelection;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

class SelectionImpl<X> implements ProcessorSelection<X> {
  private Optional<String> alias;
  private final Class<X> resultType;
  protected final List<Selection<?>> selections;
  protected Optional<List<Map.Entry<String, JPAPath>>> resolvedSelection = Optional.empty();

  public SelectionImpl(final List<Selection<?>> selections, final Class<X> resultType) {
    this.resultType = resultType;
    this.selections = selections;
  }

  public SelectionImpl(final Selection<?> selection, final Class<X> resultType) {
    this(Arrays.asList(selection), resultType);
  }

  /**
   * Assigns an alias to the selection item.
   * Once assigned, an alias cannot be changed or reassigned.
   * Returns the same selection item.
   * @param name alias
   * @return selection item
   */
  @Override
  public Selection<X> alias(@Nonnull final String name) {
    if (!alias.isPresent())
      alias = Optional.of(name);
    return this;
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    selections.stream().collect(new StringBuilderCollector.SelectionCollector(statement, ", "));
    return statement;
  }

  /**
   * Return the alias assigned to the tuple element or null,
   * if no alias has been assigned.
   * @return alias
   */

  @Override
  public String getAlias() {
    return alias.orElse("");
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
}