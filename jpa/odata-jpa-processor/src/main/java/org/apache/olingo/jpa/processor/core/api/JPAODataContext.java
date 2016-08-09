package org.apache.olingo.jpa.processor.core.api;

import java.util.List;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import org.apache.olingo.jpa.processor.core.filter.JPAOperationConverter;
import org.apache.olingo.server.api.debug.DebugSupport;

public interface JPAODataContext extends JPAODataSessionContextAccess {
  /**
   * Registers the debug support handler.
   * @param debugSupport
   */
  public void setReferences(final List<EdmxReference> references);

  public void setOperationConverter(final JPAOperationConverter jpaOperationConverter);

  public void setMetadataPostProcessor(final JPAEdmMetadataPostProcessor postProcessor) throws ODataException;

  public void setDatabaseProcessor(final JPAODataDatabaseProcessor databaseProcessor);

  public void setDebugSupport(final DebugSupport jpaDebugSupport);

  public void initDebugger(String debugFormat);
}
