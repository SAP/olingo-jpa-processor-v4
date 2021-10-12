package com.sap.olingo.jpa.metadata.core.edm.mapper.extention;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;

/**
 * @author Oliver Grande
 *
 */
public interface IntermediateModelItemAccess extends JPAElement {
  /**
   * Element shall be ignored for metadata generation.
   * @return
   */
  boolean ignore();

  /**
   * @deprecated (0.3.10, Overriding the external name not working correctly. Create an {@link JPAEdmNameBuilder} instead )
   */
  @Deprecated
  void setExternalName(String externalName);

  /**
   * Enables to switch if a model item shall be ignored during runtime.
   * @param ignore
   */
  void setIgnore(boolean ignore);

}