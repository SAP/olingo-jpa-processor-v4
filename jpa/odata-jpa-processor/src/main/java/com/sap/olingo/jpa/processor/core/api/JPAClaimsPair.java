package com.sap.olingo.jpa.processor.core.api;

/**
 * Allows to provide a single value or a closed interval.<p>
 * In case the min value is equals to {@linkplain JPAClaimsPair#ALL} the access is not restricted by this pair. This
 * value has to be used independent from the type of the attribute used to protect an entity.
 * @author Oliver Grande
 *
 * @param <T> Type of the attribute.
 */
public class JPAClaimsPair<T> {
  public static final String ALL = "*";
  public final T min;
  public final T max;
  public final boolean hasUpperBoundary;

  public JPAClaimsPair(final T min) {
    super();
    this.min = min;
    this.max = null;
    this.hasUpperBoundary = false;

  }

  public JPAClaimsPair(final T min, final T max) {
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
