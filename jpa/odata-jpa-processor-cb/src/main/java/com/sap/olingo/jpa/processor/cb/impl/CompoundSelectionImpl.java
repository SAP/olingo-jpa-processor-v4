package com.sap.olingo.jpa.processor.cb.impl;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.cb.ProcessorSelection;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

final class CompoundSelectionImpl<X> implements CompoundSelection<X>, SqlSelection<X> {
  private Optional<String> alias;
  private final Class<X> resultType;
  private final List<Selection<?>> rawSelections;
  private Optional<List<Map.Entry<String, JPAPath>>> resolvedSelection = Optional.empty();
  private Optional<List<Selection<?>>> selections = Optional.empty();
  private final AliasBuilder aliasBuilder;

  public CompoundSelectionImpl(final List<Selection<?>> selections, final Class<X> resultType,
      final AliasBuilder aliasBuilder) {
    this.resultType = resultType;
    this.rawSelections = selections;
    this.alias = Optional.empty();
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

    getCompoundSelectionItems().stream()
        .map(s -> (Selection<?>) s) // NOSONAR
        .collect(new StringBuilderCollector.SelectionCollector(statement, ", "));
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
    return selections.orElseGet(this::asSelectionLate);
  }

  private List<Selection<?>> asSelectionLate() {
    final List<Selection<?>> selItems = new ArrayList<>();
    for (final Selection<?> sel : rawSelections) {
      if (sel instanceof PathImpl<?>) {
        selItems.addAll(((PathImpl<?>) sel)
            .resolvePathElements()
            .stream()
            .map(element -> new SelectionImpl<>(element, element.getJavaType(), aliasBuilder))
            .collect(toList()));
      } else {
        selItems.add(new SelectionImpl<>(sel, sel.getJavaType(), aliasBuilder));
      }
    }
    selections = Optional.of(selItems);
    return selections.get();
  }

  @Override
  public Class<? extends X> getJavaType() {
    return resultType;
  }

  @Override
  public List<Entry<String, JPAPath>> getResolvedSelection() {
    return resolvedSelection.orElseGet(this::resolveSelectionLate);
  }

  /**
   * Whether the selection item is a compound selection.
   * @return boolean indicating whether the selection is a compound
   * selection
   */
  @Override
  public boolean isCompoundSelection() {
    return true;
  }

  List<Map.Entry<String, JPAPath>> resolveSelectionLate() {
    final AliasBuilder ab = new AliasBuilder("S");
    final List<Map.Entry<String, JPAPath>> resolved = new ArrayList<>();
    for (final Selection<?> sel : rawSelections) {
      resolveSelectionItem(ab, resolved, sel);
    }
    resolvedSelection = Optional.of(resolved);
    return resolvedSelection.get();
  }

  private void addSelectionList(final AliasBuilder ab, final List<Map.Entry<String, JPAPath>> resolved,
      final Selection<?> sel) {
    for (final JPAPath p : ((PathImpl<?>) sel).getPathList()) {
      resolved.add(new ProcessorSelection.SelectionItem(sel.getAlias().isEmpty()
          ? ab.getNext() : (sel.getAlias() + "." + p.getAlias()), p));
    }
  }

  private void addSingleSelectionItem(final AliasBuilder ab, final List<Map.Entry<String, JPAPath>> resolved,
      final Selection<?> sel, final List<JPAPath> selItems) {
    resolved.add(new ProcessorSelection.SelectionItem(sel.getAlias().isEmpty()
        ? ab.getNext() : sel.getAlias(), selItems.get(0)));
  }

  private void resolveSelectionItem(final AliasBuilder ab, final List<Map.Entry<String, JPAPath>> resolved,
      final Selection<?> sel) {

    if (sel instanceof PathImpl<?> || sel instanceof SelectionPath<?>) {
      final List<JPAPath> selItems;
      if (sel instanceof PathImpl<?>)
        selItems = ((PathImpl<?>) sel).getPathList();
      else
        selItems = ((PathImpl<?>) ((SelectionPath<?>) sel).selection.getSelection()).getPathList();
      if (selItems.size() == 1) {
        addSingleSelectionItem(ab, resolved, sel, selItems);
      } else {
        addSelectionList(ab, resolved, sel);
      }
    } else {
      resolved.add(new ProcessorSelection.SelectionItem(sel.getAlias().isEmpty()
          ? ab.getNext() : sel.getAlias(), new JPAPathWrapper(sel)));
    }
  }

  @Override
  public Selection<X> getSelection() {
    return null;
  }

}