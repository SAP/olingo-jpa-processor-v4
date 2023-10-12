package com.sap.olingo.jpa.processor.core.api;

import org.apache.olingo.server.api.uri.UriInfo;

public record JPAODataPage(UriInfo uriInfo, int skip, int top, Object skipToken) {}
