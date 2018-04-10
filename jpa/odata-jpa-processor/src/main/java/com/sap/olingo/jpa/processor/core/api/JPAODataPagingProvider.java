package com.sap.olingo.jpa.processor.core.api;

import org.apache.olingo.server.api.uri.UriInfo;

public interface JPAODataPagingProvider {
  /**
   * Returns the page related to a given skiptoken.
   * If the skiptoken is not known the method returns null.
   * @param skiptoken
   * @return
   */
  JPAODataPage getNextPage(final String skiptoken);

  /**
   * Based on the query the provider decides if a paging is required and return the first page.
   * @param uriInfo
   * @return
   */
  JPAODataPage getFristPage(final UriInfo uriInfo);

}
