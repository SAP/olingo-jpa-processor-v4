package com.sap.olingo.jpa.processor.core.api;

import org.apache.olingo.server.api.uri.UriInfo;

public class JPAODataPage {
  private final int skip;
  private final int top;
  private final String skiptoken;
  private final UriInfo uriInfo;

  public JPAODataPage(final UriInfo uriInfo, final int skip, final int top, final String skiptoken) {
    super();
    this.skip = skip;
    this.top = top;
    this.skiptoken = skiptoken;
    this.uriInfo = uriInfo;
  }

  public int getSkip() {
    return skip;
  }

  public int getTop() {
    return top;
  }

  public String getSkiptoken() {
    return skiptoken;
  }

  public UriInfo getUriInfo() {
    return uriInfo;
  }
}
