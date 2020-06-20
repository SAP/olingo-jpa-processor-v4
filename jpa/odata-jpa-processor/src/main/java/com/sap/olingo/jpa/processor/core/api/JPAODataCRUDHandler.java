package com.sap.olingo.jpa.processor.core.api;

public final class JPAODataCRUDHandler extends JPAODataGetHandler {
  /*
   * In general it is foreseen that each request gets its own CUD handler. With the introduction of the request context
   * setting the CUD handler has been mode there.
   */

  public JPAODataCRUDHandler(final JPAODataCRUDContextAccess serviceContext) {
    super(serviceContext);
  }

  @Override
  public JPAODataCRUDRequestContext getJPAODataRequestContext() {
    return (JPAODataCRUDRequestContext) super.getJPAODataRequestContext();
  }

  static class JPADefaultCUDRequestHandler extends JPAAbstractCUDRequestHandler {

  }

}
