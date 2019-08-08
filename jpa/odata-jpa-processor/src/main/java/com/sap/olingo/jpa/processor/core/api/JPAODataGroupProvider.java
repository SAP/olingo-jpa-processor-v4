package com.sap.olingo.jpa.processor.core.api;

import java.util.List;

/**
 * Container that provides field groups
 * @author Oliver Grande
 * Created: 30.06.2019
 *
 */
public interface JPAODataGroupProvider {
  /**
   * Provides a list of all field groups to be taken into account
   * @return
   */
  public List<String> getGroups();
}
