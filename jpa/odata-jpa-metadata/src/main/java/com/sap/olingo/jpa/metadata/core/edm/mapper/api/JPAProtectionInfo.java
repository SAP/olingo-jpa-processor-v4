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
  public JPAAttribute getAttribute();

  /**
   * Path within the entity type to the attribute
   * @return
   */
  public JPAPath getPath();

  /**
   * Claim names that shall be used to protect this attribute
   * @return
   */
  public String getClaimName();

  /**
   * Returns the maintained wildcard setting.
   * @return
   */
  public boolean supportsWildcards();
}
