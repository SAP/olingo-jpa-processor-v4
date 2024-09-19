package com.sap.olingo.jpa.processor.core.uri;

import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;

public interface JPAUriInfoResource extends UriInfoResource {
  public UriResource getLastResourcePart();
}
