package com.sap.olingo.jpa.processor.core.api;

import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;

public final class JPAODataCRUDHandler extends JPAODataGetHandler {
  /*
   * In general it is foreseen that each request gets its own CUD handler. With the introduction of the request context
   * setting the CUD handler has been mode there.
   */
  @Deprecated
  public JPAODataCRUDHandler(String pUnit) throws ODataException {
    super(pUnit);
    getJPAODataContext().setCUDRequestHandler(new JPADefaultCUDRequestHandler());
  }

  @Deprecated
  public JPAODataCRUDHandler(final String pUnit, final DataSource ds) throws ODataException {
    super(pUnit, ds);
    getJPAODataContext().setCUDRequestHandler(new JPADefaultCUDRequestHandler());
  }

  public JPAODataCRUDHandler(final JPAODataCRUDContextAccess serviceContext) {
    super(serviceContext);
  }

  @Override
  public JPAODataCRUDContext getJPAODataContext() {
    return (JPAODataCRUDContext) super.getJPAODataContext();
  }

  @Override
  public JPAODataCRUDRequestContext getJPAODataRequestContext() {
    return (JPAODataCRUDRequestContext) super.getJPAODataRequestContext();
  }

  class JPADefaultCUDRequestHandler extends JPAAbstractCUDRequestHandler {

  }

}
