package org.apache.olingo.jpa.processor.core.api;

import java.util.List;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.jpa.metadata.api.JPAEdmProvider;
import org.apache.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import org.apache.olingo.server.api.debug.DebugSupport;

public interface JPAODataSessionContextAccess {
  public JPAODataDatabaseProcessor getDatabaseProcessor();

  public JPAServiceDebugger getDebugger();

  public DebugSupport getDebugSupport();

  public JPAEdmProvider getEdmProvider();

  public JPAODataDatabaseOperations getOperationConverter();

  public List<EdmxReference> getReferences();
}
