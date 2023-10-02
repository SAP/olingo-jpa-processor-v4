package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;

public class LauFilter implements EdmQueryExtensionProvider {

  @Override
  public Expression<Boolean> getFilterExtension(final CriteriaBuilder cb, final From<?, ?> from) {

    return cb.and(
        cb.equal(from.get(AnnotationsParent.CODE_PUBLISHER), "Eurostat"),
        cb.like(from.get(AnnotationsParent.CODE_ID), "LAU%"));
  }

}
