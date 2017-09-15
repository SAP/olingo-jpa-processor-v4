package com.sap.olingo.jpa.processor.core.api;

public interface JPAODataCRUDContext extends JPAODataGetContext {
  public void setCUDRequestHandler(final JPACUDRequestHandler jpaCUDRequestHandler);
}
