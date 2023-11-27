package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;

public class LauFilter implements EdmQueryExtensionProvider {

  @Override
  public Expression<Boolean> getFilterExtension(final CriteriaBuilder cb, final From<?, ?> from) {

    return cb.and(
        cb.equal(from.get(AnnotationsParent.CODE_PUBLISHER), "Eurostat"),
        cb.like(from.get(AnnotationsParent.CODE_ID), "LAU%"));
  }

}
