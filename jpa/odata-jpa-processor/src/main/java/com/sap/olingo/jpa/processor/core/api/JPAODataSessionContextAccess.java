package com.sap.olingo.jpa.processor.core.api;

import java.util.List;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.debug.DebugSupport;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;

public interface JPAODataSessionContextAccess {
  public JPAODataDatabaseProcessor getDatabaseProcessor();

  public JPAServiceDebugger getDebugger();

  public DebugSupport getDebugSupport();

  public JPAEdmProvider getEdmProvider() throws ODataException;

  public JPAODataDatabaseOperations getOperationConverter();

  public List<EdmxReference> getReferences();

  public JPACUDRequestHandler getCUDRequestHandler();

  public String[] getPackageName();

  public JPAODataPagingProvider getPagingProvider();
}
