package org.apache.olingo.jpa.processor.core.api;

import org.apache.olingo.jpa.processor.core.processor.JPACUDRequestHandler;

public interface JPAODataCRUDContext extends JPAODataGetContext {
  public void setCUDRequestHandler(final JPACUDRequestHandler jpaCUDRequestHandler);
}
