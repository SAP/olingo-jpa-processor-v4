package com.sap.olingo.jpa.processor.core.api;

import org.apache.olingo.server.api.uri.UriInfo;

public class JPAODataPage {
  private final int skip;
  private final int top;
  private final Object skipToken;
  private final UriInfo uriInfo;

  public JPAODataPage(final UriInfo uriInfo, final int skip, final int top, final Object skipToken) {
    super();
    this.skip = skip;
    this.top = top;
    this.skipToken = skipToken;
    this.uriInfo = uriInfo;
  }

  public int getSkip() {
    return skip;
  }

  public int getTop() {
    return top;
  }

  public Object getSkipToken() {
    return skipToken;
  }

  public UriInfo getUriInfo() {
    return uriInfo;
  }
}
