package com.sap.olingo.jpa.processor.core.converter;

import java.util.Collection;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;

public interface JPACollectionResult extends JPAExpandResult {

  /**
   * Returns the result of a query for a collection property, which is either a list of attributes e.g. Integer for
   * primitive properties or a list of ComplexValues
   * @param key
   * @return
   */
  public Collection<Object> getPropertyCollection(final String key);

  public JPAAssociationPath getAssoziation();
}
