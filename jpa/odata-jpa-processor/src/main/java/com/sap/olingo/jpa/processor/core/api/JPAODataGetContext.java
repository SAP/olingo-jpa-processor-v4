package com.sap.olingo.jpa.processor.core.api;

import java.util.List;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.debug.DebugSupport;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;

public interface JPAODataGetContext {
  /**
   * Registers the debug support handler.
   * @param debugSupport
   */
  public void setReferences(final List<EdmxReference> references);

  /**
   * 
   * @param postProcessor
   * @throws ODataException
   */
  public void setOperationConverter(final JPAODataDatabaseOperations jpaOperationConverter);

  /**
   * 
   * @param postProcessor
   * @throws ODataException
   */
  public void setMetadataPostProcessor(final JPAEdmMetadataPostProcessor postProcessor) throws ODataException;

  /**
   * 
   * @param postProcessor
   * @throws ODataException
   */
  public void setDatabaseProcessor(final JPAODataDatabaseProcessor databaseProcessor);

  public void setDebugSupport(final DebugSupport jpaDebugSupport);

  public void initDebugger(final String debugFormat);

  /**
   * Name of the top level package to look for
   * <ul>
   * <li> Enumeration Types
   * <li> Java class based Functions
   * </ul>
   * @param packageName
   */
  public void setTypePackage(final String... packageName);
}
