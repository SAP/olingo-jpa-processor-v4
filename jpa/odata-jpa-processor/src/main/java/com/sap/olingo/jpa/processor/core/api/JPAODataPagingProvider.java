package com.sap.olingo.jpa.processor.core.api;

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.processor.core.query.JPACountQuery;

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
   * @throws ODataApplicationException
   */
  JPAODataPage getFristPage(final UriInfo uriInfo, final Integer preferedPageSize, final JPACountQuery countQuery,
      final EntityManager em) throws ODataApplicationException;

}
