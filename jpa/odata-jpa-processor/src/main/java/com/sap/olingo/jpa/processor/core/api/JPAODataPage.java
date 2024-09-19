package com.sap.olingo.jpa.processor.core.api;

import java.util.Collections;
import java.util.List;

import org.apache.olingo.server.api.uri.UriInfoResource;

public record JPAODataPage(UriInfoResource uriInfo, int skip, int top, Object skipToken,
    List<JPAODataPageExpandInfo> expandInfo) {

  public JPAODataPage(final UriInfoResource uriInfo, final int skip, final int top, final Object skipToken) {
    this(uriInfo, skip, top, skipToken, Collections.emptyList());
  }
}
