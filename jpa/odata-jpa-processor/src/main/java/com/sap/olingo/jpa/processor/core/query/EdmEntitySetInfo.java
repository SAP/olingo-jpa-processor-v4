package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.uri.UriParameter;

public interface EdmEntitySetInfo {

  public EdmEntitySet getEdmEntitySet();

  public List<UriParameter> getKeyPredicates();

  public String getName();

  public String getNavigationPath();

  public EdmEntitySet getTargetEdmEntitySet();
}
