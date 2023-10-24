package com.sap.olingo.jpa.processor.core.api;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Container that provides claims
 *
 * @author Oliver Grande
 * Created: 30.06.2019
 *
 */
public interface JPAODataClaimProvider {
  /**
   * @param attributeName
   * @return Provides a list claim values for a given attribute.
   */
  @Nonnull
  List<JPAClaimsPair<?>> get(final String attributeName); // NOSONAR

  /**
   *
   * @return An optional that may contain the user id for the current request
   */
  default Optional<String> user() {
    return Optional.empty();
  }
}