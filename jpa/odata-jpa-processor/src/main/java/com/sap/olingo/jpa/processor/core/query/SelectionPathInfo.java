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
  private final Set<T> requitedSelections;
  private final Set<T> transientSelections;
  private Set<T> joinedPersistant;
  private Set<T> joinedRequested;

  SelectionPathInfo(@Nullable final Set<T> odataSelections, @Nullable final Set<T> requitedSelections,
      @Nullable final Set<T> transientSelections) {
    super();
    this.odataSelections = odataSelections == null ? Collections.emptySet() : odataSelections;
    this.requitedSelections = requitedSelections == null ? Collections.emptySet() : requitedSelections;
    this.transientSelections = transientSelections == null ? Collections.emptySet() : transientSelections;
  }

  SelectionPathInfo(@Nonnull List<T> additionalODataSelections, @Nonnull SelectionPathInfo<T> jpaSelectionPath) {
    this.odataSelections = new HashSet<>(additionalODataSelections);
    this.odataSelections.addAll(jpaSelectionPath.odataSelections);
    this.requitedSelections = jpaSelectionPath.requitedSelections;
    this.transientSelections = jpaSelectionPath.transientSelections;
  }

  SelectionPathInfo() {
    this.odataSelections = new HashSet<>();
    this.requitedSelections = new HashSet<>();
    this.transientSelections = new HashSet<>();
  }

  Set<T> getODataSelections() {
    return odataSelections;
  }

  Set<T> getRequitedSelections() {
    return requitedSelections;
  }

  Set<T> getTransientSelections() {
    return transientSelections;
  }

  Set<T> joined() {
    final Set<T> joined = new HashSet<>(odataSelections);
    joined.addAll(requitedSelections);
    joined.addAll(transientSelections);
    return joined;
  }

  Set<T> joinedPersistant() {
    if (joinedPersistant == null) {
      joinedPersistant = new HashSet<>(odataSelections);
      joinedPersistant.addAll(requitedSelections);
    }
    return joinedPersistant;
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
    return "SelectionPathInfo [odatat=" + odataSelections + ", required=" + requitedSelections + ", transient="
        + transientSelections
        + "]";
  }
}
