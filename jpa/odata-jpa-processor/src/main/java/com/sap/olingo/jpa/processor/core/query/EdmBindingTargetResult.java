package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmNavigationPropertyBinding;
import org.apache.olingo.server.api.uri.UriParameter;

/**
 * Container to provide result e.g. of target entity set or singleton determination
 * @author Oliver Grande
 *
 */
final class EdmBindingTargetResult implements EdmBindingTargetInfo {

  private final EdmBindingTarget edmBindingTarget;
  private final List<UriParameter> keyPredicates;
  private final String navigationPath;

  EdmBindingTargetResult(final EdmBindingTarget targetEdmBindingTarget, final List<UriParameter> keyPredicates,
      final String navigationPath) {
    super();
    this.edmBindingTarget = targetEdmBindingTarget;
    this.keyPredicates = keyPredicates;
    this.navigationPath = navigationPath;
  }

  @Override
  public EdmBindingTarget getEdmBindingTarget() {
    return this.edmBindingTarget;
  }

  @Override
  public List<UriParameter> getKeyPredicates() {
    return this.keyPredicates;
  }

  @Override
  public String getName() {
    return edmBindingTarget.getName();
  }

  @Override
  public String getNavigationPath() {
    return navigationPath;
  }

  @Override
  public EdmBindingTarget getTargetEdmBindingTarget() {
    if (navigationPath == null || navigationPath.isEmpty())
      return this.edmBindingTarget;
    else {
      for (final EdmNavigationPropertyBinding navigation : this.edmBindingTarget.getNavigationPropertyBindings()) {
        if (navigation.getPath().equals(navigationPath))
          return edmBindingTarget.getEntityContainer().getEntitySet(navigation.getTarget());
      }
      return this.edmBindingTarget;
    }
  }
}