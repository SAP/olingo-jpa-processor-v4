package com.sap.olingo.jpa.processor.core.api;

import java.util.List;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.processor.ErrorProcessor;

import com.sap.olingo.jpa.metadata.api.JPAApiVersion;
import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataServiceContext.Builder;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;

public interface JPAODataServiceContextBuilder {

  JPAODataSessionContextAccess build() throws ODataException;

  /**
   * A database processor allows database specific implementations for search and odata function with function import
   * that are implemented as database functions.<br>
   * In case no database processor is provided and non could be determined via an data source
   * {@link JPADefaultDatabaseProcessor} is used.
   * @param databaseProcessor
   * @return
   */
  JPAODataServiceContextBuilder setDatabaseProcessor(JPAODataDatabaseProcessor databaseProcessor);

  /**
   * The data source is used to create an entity manager factory if not provided, see
   * {@link Builder#setEntityManagerFactory(EntityManagerFactory)}, and to determine the type of
   * database used to select an integrated database processor, in case the database processor was not set via
   * {@link Builder#setDatabaseProcessor(JPAODataDatabaseProcessor)}}.
   * @param ds
   * @return
   */
  JPAODataServiceContextBuilder setDataSource(DataSource ds);

  /**
   * Allows to provide an Olingo error processor. The error processor allows to enrich an error response. See
   * <a
   * href=
   * "http://docs.oasis-open.org/odata/odata-json-format/v4.0/errata03/os/odata-json-format-v4.0-errata03-os-complete.html#_Toc453766668"
   * >JSON Error Response</a> or
   * <a
   * href=
   * "http://docs.oasis-open.org/odata/odata-atom-format/v4.0/cs02/odata-atom-format-v4.0-cs02.html#_Toc372792829">Atom
   * Error Response</a>.
   * @param errorProcessor
   */
  JPAODataServiceContextBuilder setErrorProcessor(ErrorProcessor errorProcessor);

  /**
   *
   * @param postProcessor
   * @return
   */
  JPAODataServiceContextBuilder setMetadataPostProcessor(JPAEdmMetadataPostProcessor postProcessor);

  /**
   *
   * @param jpaOperationConverter
   * @return
   */
  JPAODataServiceContextBuilder setOperationConverter(JPAODataDatabaseOperations jpaOperationConverter);

  /**
   * Register a provider that is able to decides based on a given query if the server like to return only a sub set of
   * the requested results as well as a $skiptoken.
   * @param provider
   */
  JPAODataServiceContextBuilder setPagingProvider(JPAODataPagingProvider provider);

  /**
   * The name of the persistence-unit to be used. It is taken to create a entity manager factory
   * ({@link Builder#setEntityManagerFactory(EntityManagerFactory)}), if not provided and
   * as namespace of the OData service, in case the default name builder shall be used.
   * @param pUnit
   * @return
   */
  JPAODataServiceContextBuilder setPUnit(String pUnit);

  /**
   *
   * @param references
   * @return
   */
  JPAODataServiceContextBuilder setReferences(List<EdmxReference> references);

  /**
   * Name of the top level package to look for
   * <ul>
   * <li>Enumeration Types
   * <li>Java class based Functions
   * </ul>
   * @param packageName
   */
  JPAODataServiceContextBuilder setTypePackage(String... packageName);

  JPAODataServiceContextBuilder setRequestMappingPath(String mappingPath);

  /**
   * Set an externally created entity manager factory.<br>
   * This is necessary e.g. in case a spring based service shall run without a <code>persistance.xml</code>.
   * @param emf
   * @return
   */
  JPAODataServiceContextBuilder setEntityManagerFactory(EntityManagerFactory emf);

  /**
   * Set a custom EDM name builder {@link JPAEdmNameBuilder}. If non is provided {@link JPADefaultEdmNameBuilder} is
   * used, which uses the provided persistence-unit name ({@link JPAODataServiceContext.Builder#setPUnit}) as
   * namespace.
   * @param nameBuilder
   * @return
   */
  JPAODataServiceContextBuilder setEdmNameBuilder(JPAEdmNameBuilder nameBuilder);

  <T extends JPAODataBatchProcessor> JPAODataServiceContextBuilder setBatchProcessorFactory(
      JPAODataBatchProcessorFactory<T> batchProcessorFactory);

  /**
   * Some clients, like Excel, require context url's with an absolute path. The default generation of relative paths
   * can be overruled.<br>
   * @see <a href="https://issues.apache.org/jira/browse/OLINGO-787">Issue OLINGO-787</a>
   * @param useAbsoluteContextURL
   */
  JPAODataServiceContextBuilder setUseAbsoluteContextURL(boolean useAbsoluteContextURL);

  JPAODataServiceContextBuilder setAnnotationProvider(AnnotationProvider... annotationProvider);

  /**
   *
   */
  JPAODataQueryDirectivesBuilder useQueryDirectives();

  /**
   * Some database use different clauses for a certain function. E.g., to limit the number of rows returned.
   * Some databases use LIMIT and OFFSET, other OFFSET ... ROWS and FETCH NEXT ... ROWS.<br>
   * This is relevant when module <i>odata-jpa-processor-cb</i> is used.
   * @since 2.2.0
   */
  JPAODataServiceContextBuilder setSqlPatternProvider(ProcessorSqlPatternProvider sqlPattern);

  /**
   * Set an API version. If no version is provided, a version is created from the corresponding setters.
   * @see <a href=
   * "https://github.com/SAP/olingo-jpa-processor-v4/blob/main/jpa-tutorial/Questions/HowToHandleApiVersions.adoc">How
   * to handle API versions?<a>
   * @param apiVersion
   * @return
   * @since 2.3.0
   */
  JPAODataServiceContextBuilder setVersions(JPAApiVersion... apiVersions);

}