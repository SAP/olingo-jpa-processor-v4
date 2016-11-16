package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.uri.UriParameter;

/**
 * Container to provide result e.g. of target entity set determination
 * @author Oliver Grande
 *
 */
class EdmEntitySetResult implements EdmEntitySetInfo {

  private final EdmEntitySet edmEntitySet;
  private final List<UriParameter> keyPredicates;

  EdmEntitySetResult(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates) {
    super();
    this.edmEntitySet = edmEntitySet;
    this.keyPredicates = keyPredicates;
  }

  @Override
  public EdmEntitySet getEdmEntitySet() {
    return this.edmEntitySet;
  }

  @Override
  public List<UriParameter> getKeyPredicates() {
    return this.keyPredicates;
  }

  @Override
  public String getName() {
    return edmEntitySet.getName();
  }

}