package com.sap.olingo.jpa.processor.core.api;

import org.apache.olingo.server.api.uri.UriInfoResource;

public record JPAODataExpandPage(UriInfoResource uriInfo, int skip, int top, JPAODataSkipTokenProvider skipToken) {

}
