package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.List;

final class JPAProtectionInfo {
  private final List<String> path;
  private final boolean wildcards;

  JPAProtectionInfo(List<String> path, boolean wildcards) {
    super();
    this.path = path;
    this.wildcards = wildcards;
  }

  @Override
  public String toString() {
    return "JPAProtectionInfo [path=" + path + ", wildcards=" + wildcards + "]";
  }

  List<String> getPath() {
    return path;
  }

  /**
   * Returns the maintained wildcard setting.
   * @return
   */
  boolean supportsWildcards() {
    return wildcards;
  }

  /**
   * Returns wildcard support if the protected property it of type <i>clazz</i>
   * @param <T>
   * @param clazz
   * @return
   */
  <T> boolean supportsWildcards(final Class<T> clazz) {
    if (clazz.equals(String.class))
      return wildcards;
    return false;
  }
}
