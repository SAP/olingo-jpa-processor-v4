package org.apache.olingo.jpa.processor.core.api;

import java.util.List;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import org.apache.olingo.jpa.processor.core.filter.JPAOperationConverter;
import org.apache.olingo.server.api.debug.DebugSupport;
import org.apache.olingo.server.api.edmx.EdmxReference;

public interface JPAODataContext extends JPAODataContextAccess {
  /**
   * Registers the debug support handler.
   * @param debugSupport
   */
  public void register(DebugSupport debugSupport);

  public void setReferences(List<EdmxReference> references);

  public void setOperationConverter(JPAOperationConverter jpaOperationConverter);

  public void setMetadataPostProcessor(JPAEdmMetadataPostProcessor postProcessor) throws ODataException;

  public void setDatabaseProcessor(JPAODataDatabaseProcessor databaseProcessor);
}
