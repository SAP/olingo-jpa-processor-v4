package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmMapping;
import org.apache.olingo.commons.api.edm.EdmNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.EdmTerm;

class EdmBoundCast implements EdmBindingTarget {
  private final EdmEntityType edmType;
  private final EdmBindingTarget edmBindingTarget;

  EdmBoundCast(final EdmEntityType edmType, final EdmBindingTarget edmBindingTarget) {
    super();
    this.edmType = edmType;
    this.edmBindingTarget = edmBindingTarget;
  }

  @Override
  public String getName() {
    return edmType.getName();
  }

  @Override
  public EdmAnnotation getAnnotation(final EdmTerm term, final String qualifier) {
    return edmType.getAnnotation(term, qualifier);
  }

  @Override
  public List<EdmAnnotation> getAnnotations() {
    return edmType.getAnnotations();
  }

  @Override
  public EdmMapping getMapping() {
    return null;
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public EdmBindingTarget getRelatedBindingTarget(final String path) {
    return null;
  }

  @Override
  public List<EdmNavigationPropertyBinding> getNavigationPropertyBindings() {
    return getNavigationPropertyBindings();
  }

  @Override
  public EdmEntityContainer getEntityContainer() {
    return edmBindingTarget.getEntityContainer();
  }

  @Override
  public EdmEntityType getEntityType() {
    return edmType;
  }

  @Override
  public EdmEntityType getEntityTypeWithAnnotations() {
    return getEntityType();
  }

}
