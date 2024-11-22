package com.sap.olingo.jpa.processor.core.api;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.processor.ErrorProcessor;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;

/**
 *
 * @author Oliver Grande
 *
 */
public interface JPAODataSessionContextAccess {

  public JPAODataDatabaseProcessor getDatabaseProcessor();

  public JPAODataDatabaseOperations getOperationConverter();

  public List<EdmxReference> getReferences();

  /**
   * Returns a list of packages that may contain Enumerations of Java implemented OData operations
   * @deprecated (method won't return correct value in case of multiple versions, use getApiVersion)
   */
  @Deprecated(since = "2.2.3", forRemoval = true)
  public default List<String> getPackageName() {
    return List.of();
  }

  /**
   * If server side paging shall be supported <code>getPagingProvider</code> returns an implementation of a paging
   * provider. Details about the OData specification can be found under <a
   * href=
   * "https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Server-Driven_Paging">OData
   * Version 4.0 Part 1 - 11.2.5.7 Server-Driven Paging</a>
   * @return
   */
  public JPAODataPagingProvider getPagingProvider();

  /**
   * @deprecated (method won't return correct value in case of multiple versions, use getApiVersion)
   */
  @Deprecated(since = "2.2.3", forRemoval = true)
  public default Optional<? extends EntityManagerFactory> getEntityManagerFactory() {
    return Optional.empty();
  }

  public default ErrorProcessor getErrorProcessor() {
    return null;
  }

  /**
   * @deprecated (method won't return correct value in case of multiple versions, use getApiVersion)
   */

  @Deprecated(since = "2.2.3", forRemoval = true)
  public default String getMappingPath() {
    return "";
  }

  public default <T extends JPAODataBatchProcessor> JPAODataBatchProcessorFactory<T> getBatchProcessorFactory() {
    return null;
  }

  public default boolean useAbsoluteContextURL() {
    return false;
  }

  public List<AnnotationProvider> getAnnotationProvider();

  public JPAODataQueryDirectives getQueryDirectives();

  public ProcessorSqlPatternProvider getSqlPatternProvider();

  public JPAODataApiVersionAccess getApiVersion(String id);

}
