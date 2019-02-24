package com.sap.olingo.jpa.processor.core.api;

/**
 * Allows to provide a single value or a closed interval.
 * @author Oliver Grande
 *
 * @param <T>
 */
public class JPAClaimsPair<T> {
  public final T min;
  public final T max;
  public final boolean hasUpperBoundary;

  public JPAClaimsPair(T min) {
    super();
    this.min = min;
    this.max = null;
    this.hasUpperBoundary = false;

  }

  public JPAClaimsPair(T min, T max) {
    super();
    this.min = min;
    this.max = max;
    this.hasUpperBoundary = true;
  }

  @Override
  public String toString() {
    return "JPAClaimsPair [min=" + min + ", max=" + max + "]";
  }

  @SuppressWarnings("unchecked")
  public <Y> Y minAs() {
    return (Y) min;
  }

  @SuppressWarnings("unchecked")
  public <Y> Y maxAs() {
    return (Y) max;
  }
}
