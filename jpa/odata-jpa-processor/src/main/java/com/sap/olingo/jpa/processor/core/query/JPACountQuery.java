package com.sap.olingo.jpa.processor.core.query;

import org.apache.olingo.server.api.ODataApplicationException;

public interface JPACountQuery {
  /**
   * Fulfill $count requests. For details see
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752288"
   * >OData Version 4.0 Part 1 - 11.2.5.5 System Query Option $count</a>
   * @return
   * @throws ODataApplicationException
   */
  Long countResults() throws ODataApplicationException;

}
