package com.sap.olingo.jpa.processor.core.testmodel;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;

/**
 * Empty implementation to check inheritance of query provider.
 * @author Oliver Grande
 *
 */
public class EmptyQueryExtensionProvider implements EdmQueryExtensionProvider {

  @Override
  public Expression<Boolean> getFilterExtension(final CriteriaBuilder cb, final From<?, ?> from) {
    return null;
  }

}
