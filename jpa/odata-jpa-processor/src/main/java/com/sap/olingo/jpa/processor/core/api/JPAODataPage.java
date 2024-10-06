package com.sap.olingo.jpa.processor.core.api;

import java.util.Collections;
import java.util.List;

import org.apache.olingo.server.api.uri.UriInfoResource;

/**
 * A page in case a the amount of returned data shall be restricted by the server.
 *
 * @param uriInfo UriInfoResource of the original request
 * @param skip Skip value to be used
 * @param top Top value to be used
 * @param skipToken The skip token to be used in the next link for non expand requests.
 * @param expandInformation Provides for pages of restricted expand requests the path and key information needed to build the
 * next
 * request.
 *
 */
public record JPAODataPage(UriInfoResource uriInfo, int skip, int top, Object skipToken,
    List<JPAODataPageExpandInfo> expandInformation) {

  public JPAODataPage(final UriInfoResource uriInfo, final int skip, final int top, final Object skipToken) {
    this(uriInfo, skip, top, skipToken, Collections.emptyList());
  }
}
