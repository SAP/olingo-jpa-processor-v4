package com.sap.olingo.jpa.processor.core.query;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Immutable triple of sets, that are related to each other.
 *
 * @author Oliver Grande
 * Created: 27.03.2020
 *
 * @param <T>
 */
class SelectionPathInfo<T> {
  private final Set<T> odataSelections;
  private final Set<T> requiredSelections;
  private final Set<T> transientSelections;
  private Set<T> joinedPersistent;
  private Set<T> joinedRequested;

  SelectionPathInfo(@Nullable final Set<T> odataSelections, @Nullable final Set<T> requitedSelections,
      @Nullable final Set<T> transientSelections) {
    super();
    this.odataSelections = odataSelections == null ? Collections.emptySet() : odataSelections;
    this.requiredSelections = requitedSelections == null ? Collections.emptySet() : requitedSelections;
    this.transientSelections = transientSelections == null ? Collections.emptySet() : transientSelections;
  }

  SelectionPathInfo(@Nonnull final List<T> additionalODataSelections,
      @Nonnull final SelectionPathInfo<T> jpaSelectionPath) {
    this.odataSelections = new HashSet<>(additionalODataSelections);
    this.odataSelections.addAll(jpaSelectionPath.odataSelections);
    this.requiredSelections = jpaSelectionPath.requiredSelections;
    this.transientSelections = jpaSelectionPath.transientSelections;
  }

  SelectionPathInfo() {
    this.odataSelections = new HashSet<>();
    this.requiredSelections = new HashSet<>();
    this.transientSelections = new HashSet<>();
  }

  Set<T> getODataSelections() {
    return odataSelections;
  }

  Set<T> getRequiredSelections() {
    return requiredSelections;
  }

  Set<T> getTransientSelections() {
    return transientSelections;
  }

  Set<T> joined() {
    final Set<T> joined = new HashSet<>(odataSelections);
    joined.addAll(requiredSelections);
    joined.addAll(transientSelections);
    return joined;
  }

  Set<T> joinedPersistent() {
    if (joinedPersistent == null) {
      joinedPersistent = new HashSet<>(odataSelections);
      joinedPersistent.addAll(requiredSelections);
    }
    return joinedPersistent;
  }

  Set<T> joinedRequested() {
    if (joinedRequested == null) {
      joinedRequested = new HashSet<>(odataSelections);
      joinedRequested.addAll(transientSelections);
    }
    return joinedRequested;
  }

  @Override
  public String toString() {
    return "SelectionPathInfo [odata=" + odataSelections + ", required=" + requiredSelections + ", transient="
        + transientSelections
        + "]";
  }
}