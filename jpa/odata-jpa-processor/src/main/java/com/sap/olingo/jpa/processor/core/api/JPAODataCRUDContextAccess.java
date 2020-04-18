package com.sap.olingo.jpa.processor.core.api;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.debug.DebugSupport;
import org.apache.olingo.server.api.processor.ErrorProcessor;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;

/**
 * 
 * @author Oliver Grande
 *
 */
public interface JPAODataCRUDContextAccess {

  public JPAODataDatabaseProcessor getDatabaseProcessor();

  /**
   * @deprecated (will be removed with 1.0.0; use request context)
   * @return
   */
  @Deprecated
  public DebugSupport getDebugSupport();

  public JPAEdmProvider getEdmProvider() throws ODataException;

  public JPAODataDatabaseOperations getOperationConverter();

  public List<EdmxReference> getReferences();

  /**
   * @deprecated (will be removed with 1.0.0; use request context)
   * @return
   */
  @Deprecated
  public JPACUDRequestHandler getCUDRequestHandler();

  /**
   * Returns a list of packages that may contain Enumerations of Java implemented OData operations
   * @return
   */
  public String[] getPackageName();

  /**
   * If server side paging shall be supported <code>getPagingProvider</code> returns an implementation of a paging
   * provider. Details about the OData specification can be found under <a
   * href="https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Server-Driven_Paging">OData
   * Version 4.0 Part 1 - 11.2.5.7 Server-Driven Paging</a>
   * @return
   */
  public JPAODataPagingProvider getPagingProvider();

  public default Optional<EntityManagerFactory> getEntityManagerFactory() {
    return Optional.empty();
  }

  public default ErrorProcessor getErrorProcessor() {
    return null;
  }

  public default String getMappingPath() {
    return "";
  }

  public default boolean useAbsoluteContextURL() {
    return false;
  }
}
