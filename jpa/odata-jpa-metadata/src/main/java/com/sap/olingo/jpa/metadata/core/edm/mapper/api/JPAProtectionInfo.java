package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

/**
 * Provides information about a protected attribute
 * @author Oliver Grande
 *
 */
public interface JPAProtectionInfo {
  /**
   * The protected attribute
   * @return
   */
  JPAAttribute getAttribute();

  /**
   * Path within the entity type to the attribute
   * @return
   */
  JPAPath getPath();

  /**
   * Claim names that shall be used to protect this attribute
   * @return
   */
  String getClaimName();

  /**
   * Returns the maintained wildcard setting.<p>
   * In case wildcards are supported, only for attributes of type string, '*' and '%' representing
   * zero or more characters and '+' as well as '_' for a single character.
   * @return
   */
  boolean supportsWildcards();
}
