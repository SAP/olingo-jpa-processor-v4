package com.sap.olingo.jpa.processor.cb.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Selection;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.cb.ProcessorSelection;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

class CompoundSelectionImpl<X> extends SelectionImpl<X> implements CompoundSelection<X> {
  public CompoundSelectionImpl(final List<Selection<?>> selections, final Class<X> resultType) {
    super(selections, resultType);
  }

  @Override
  public StringBuilder asSQL(@Nonnull final StringBuilder statement) {
    final List<Expression<?>> selItems = new ArrayList<>();
    for (final Selection<?> sel : selections) {
      if (sel instanceof PathImpl<?>) {
        selItems.addAll(((PathImpl<?>) sel).resolvePathElements());
      } else {
        selItems.add((Expression<?>) sel);
      }
    }
    selItems.stream()
        .map(s -> (ExpressionImpl<?>) s)
        .collect(new StringBuilderCollector.ExpressionCollector(statement, ", "));
    return statement;
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
    return selections;
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

  @Override
  protected List<Map.Entry<String, JPAPath>> resolveSelectionLate() {
    final AliasBuilder ab = new AliasBuilder("S");
    final List<Map.Entry<String, JPAPath>> resolved = new ArrayList<>();
    for (final Selection<?> sel : selections) {
      resolveSelectionItem(ab, resolved, sel);
    }
    resolvedSelection = Optional.of(resolved);
    return resolvedSelection.get();
  }

  private void resolveSelectionItem(final AliasBuilder ab, final List<Map.Entry<String, JPAPath>> resolved,
      final Selection<?> sel) {

    if (sel instanceof PathImpl<?>) {
      final List<JPAPath> selItems = ((PathImpl<?>) sel).getPathList();
      if (selItems.size() == 1) {
        addSingleSelectionItem(ab, resolved, sel, selItems);
      } else {
        addSelectionList(ab, resolved, sel);
      }
    } else {
      resolved.add(new ProcessorSelection.SelectionItem(sel.getAlias(), new JPAPathWrapper(sel)));
    }
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
}