package com.sap.olingo.jpa.processor.core.api;

@Deprecated
public interface JPAODataCRUDContext extends JPAODataGetContext {
  /**
   * @deprecated (Will be removed with 1.0.0, setting the CUD handler has been mode to the
   * Request Context (<code>getJPAODataRequestContext</code>) )
   * @param jpaCUDRequestHandler
   */
  @Deprecated
  public void setCUDRequestHandler(final JPACUDRequestHandler jpaCUDRequestHandler);
}
