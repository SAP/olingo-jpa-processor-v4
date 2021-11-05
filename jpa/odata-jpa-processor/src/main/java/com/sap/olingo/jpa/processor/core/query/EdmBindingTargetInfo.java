package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.server.api.uri.UriParameter;

public interface EdmBindingTargetInfo {

  public EdmBindingTarget getEdmBindingTarget();

  public List<UriParameter> getKeyPredicates();

  public String getName();

  public String getNavigationPath();

  public EdmBindingTarget getTargetEdmBindingTarget();
}
