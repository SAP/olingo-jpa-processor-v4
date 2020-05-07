package com.sap.olingo.jpa.processor.core.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.debug.DebugInformation;
import org.apache.olingo.server.api.debug.DebugSupport;
import org.apache.olingo.server.api.debug.RuntimeMeasurement;
import org.apache.olingo.server.api.processor.ErrorProcessor;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseProcessorFactory;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

public final class JPAODataServiceContext implements JPAODataCRUDContextAccess {
  /**
   * 
   */
  private List<EdmxReference> references = new ArrayList<>();
  private JPAODataDatabaseOperations operationConverter;
  private JPAEdmProvider jpaEdm;
  private JPAODataDatabaseProcessor databaseProcessor;
  private JPAEdmMetadataPostProcessor postProcessor;
  private String[] packageName;
  private ErrorProcessor errorProcessor;
  private JPAODataPagingProvider pagingProvider;
  private Optional<EntityManagerFactory> emf;
  private final String namespace;
  private String mappingPath;
  private JPAODataBatchProcessorFactory<JPAODataBatchProcessor> batchProcessorFactory;
  private boolean useAbsoluteContextURL;

  public static Builder with() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  private JPAODataServiceContext(final Builder builder) {

    operationConverter = builder.operationConverter;
    databaseProcessor = builder.databaseProcessor;
    references = builder.references;
    postProcessor = builder.postProcessor;
    packageName = builder.packageName;
    errorProcessor = builder.errorProcessor;
    pagingProvider = builder.pagingProvider;
    jpaEdm = builder.jpaEdm;
    emf = builder.emf;
    namespace = builder.namespace;
    mappingPath = builder.mappingPath;
    batchProcessorFactory = (JPAODataBatchProcessorFactory<JPAODataBatchProcessor>) builder.batchProcessorFactory;
    useAbsoluteContextURL = builder.useAbsoluteContextURL;
  }

  @Override
  public JPAODataDatabaseProcessor getDatabaseProcessor() {
    return databaseProcessor;
  }

  @Override
  public JPAEdmProvider getEdmProvider() throws ODataException {
    return jpaEdm;
  }

  public JPAEdmProvider getEdmProvider(@Nonnull final EntityManager em) throws ODataException {
    if (jpaEdm == null) {
      Objects.nonNull(em);
      jpaEdm = new JPAEdmProvider(this.namespace, em.getMetamodel(), postProcessor, packageName);
    }
    return jpaEdm;
  }

  @Override
  public Optional<EntityManagerFactory> getEntityManagerFactory() {
    return emf;
  }

  @Override
  public ErrorProcessor getErrorProcessor() {
    return this.errorProcessor == null ? new JPADefaultErrorProcessor() : this.errorProcessor;
  }

  @Override
  public JPAODataDatabaseOperations getOperationConverter() {
    return operationConverter;
  }

  @Override
  public List<String> getPackageName() {
    return Arrays.asList(packageName);
  }

  @Override
  public JPAODataPagingProvider getPagingProvider() {
    return pagingProvider;
  }

  @Override
  public List<EdmxReference> getReferences() {
    return references;
  }

  @Override
  public String getMappingPath() {
    return mappingPath;
  }

  @Override
  public boolean useAbsoluteContextURL() {
    return useAbsoluteContextURL;
  }

  @Override
  public JPAODataBatchProcessorFactory<JPAODataBatchProcessor> getBatchProcessorFactory() {
    return batchProcessorFactory;
  }

  public static class Builder {

    private String namespace;
    private List<EdmxReference> references = new ArrayList<>();
    private JPAODataDatabaseOperations operationConverter = new JPADefaultDatabaseProcessor();
    private JPAODataDatabaseProcessor databaseProcessor;
    private JPAEdmMetadataPostProcessor postProcessor;
    private String[] packageName;
    private ErrorProcessor errorProcessor;
    private JPAODataPagingProvider pagingProvider;
    private Optional<EntityManagerFactory> emf = Optional.empty();
    private DataSource ds;
    private JPAEdmProvider jpaEdm;
    private JPAEdmNameBuilder nameBuilder;
    private String mappingPath;
    private JPAODataBatchProcessorFactory<?> batchProcessorFactory;
    private boolean useAbsoluteContextURL = false;

    public JPAODataCRUDContextAccess build() throws ODataException {
      try {
        if (nameBuilder == null)
          nameBuilder = new JPADefaultEdmNameBuilder(namespace);
        if (packageName == null)
          packageName = new String[0];
        if (!emf.isPresent() && ds != null && namespace != null)
          emf = Optional.ofNullable(JPAEntityManagerFactory.getEntityManagerFactory(namespace, ds));
        if (emf.isPresent())
          jpaEdm = new JPAEdmProvider(emf.get().getMetamodel(), postProcessor, packageName, nameBuilder);
        if (databaseProcessor == null) {
          databaseProcessor = new JPAODataDatabaseProcessorFactory().create(ds);
        }
        if (batchProcessorFactory == null)
          batchProcessorFactory = new JPADefaultBatchProcessorFactory();
      } catch (SQLException | PersistenceException e) {
        throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
      return new JPAODataServiceContext(this);
    }

    /**
     * A database processor allows database specific implementations for search and odata function with function import
     * that are implemented as database functions.<br>
     * In case no database processor is provided and non could be determined via an data source
     * {@link JPADefaultDatabaseProcessor} is used.
     * @param databaseProcessor
     * @return
     */
    public Builder setDatabaseProcessor(final JPAODataDatabaseProcessor databaseProcessor) {
      this.databaseProcessor = databaseProcessor;
      return this;
    }

    /**
     * The data source is used to create an entity manager factory if not provided, see
     * {@link Builder#setEntityManagerFactory(EntityManagerFactory)}, and to determine the type of
     * database used to select an integrated database processor, in case the database processor was not set via
     * {@link Builder#setDatabaseProcessor(JPAODataDatabaseProcessor)}}.
     * @param ds
     * @return
     */
    public Builder setDataSource(final DataSource ds) {
      this.ds = ds;
      return this;
    }

    /**
     * Allows to provide an Olingo error processor. The error processor allows to enrich an error response. See
     * <a
     * href="http://docs.oasis-open.org/odata/odata-json-format/v4.0/errata03/os/odata-json-format-v4.0-errata03-os-complete.html#_Toc453766668"
     * >JSON Error Response</a> or
     * <a
     * href="http://docs.oasis-open.org/odata/odata-atom-format/v4.0/cs02/odata-atom-format-v4.0-cs02.html#_Toc372792829">Atom
     * Error Response</a>.
     * @param errorProcessor
     */
    public Builder setErrorProcessor(final ErrorProcessor errorProcessor) {
      this.errorProcessor = errorProcessor;
      return this;
    }

    /**
     * 
     * @param postProcessor
     * @return
     */
    public Builder setMetadataPostProcessor(final JPAEdmMetadataPostProcessor postProcessor) {
      this.postProcessor = postProcessor;
      return this;
    }

    /**
     * 
     * @param jpaOperationConverter
     * @return
     */
    public Builder setOperationConverter(final JPAODataDatabaseOperations jpaOperationConverter) {
      this.operationConverter = jpaOperationConverter;
      return this;
    }

    /**
     * Register a provider that is able to decides based on a given query if the server like to return only a sub set of
     * the requested results as well as a $skiptoken.
     * @param provider
     */
    public Builder setPagingProvider(final JPAODataPagingProvider provider) {
      this.pagingProvider = provider;
      return this;
    }

    /**
     * The name of the persistence-unit to be used. It is taken to create a entity manager factory
     * ({@link Builder#setEntityManagerFactory(EntityManagerFactory)}), if not provided and
     * as namespace of the OData service, in case the default name builder shall be used.
     * @param pUnit
     * @return
     */
    public Builder setPUnit(final String pUnit) {
      this.namespace = pUnit;
      return this;
    }

    /**
     * 
     * @param references
     * @return
     */
    public Builder setReferences(final List<EdmxReference> references) {
      this.references = references;
      return this;
    }

    /**
     * Name of the top level package to look for
     * <ul>
     * <li> Enumeration Types
     * <li> Java class based Functions
     * </ul>
     * @param packageName
     */
    public Builder setTypePackage(final String... packageName) {
      this.packageName = packageName;
      return this;
    }

    public Builder setRequestMappingPath(final String mappingPath) {
      this.mappingPath = mappingPath;
      return this;
    }

    /**
     * Set an externally created entity manager factory.<br>
     * This is necessary e.g. in case a spring based service shall run without a <code>persistance.xml</code>.
     * @param emf
     * @return
     */
    public Builder setEntityManagerFactory(final EntityManagerFactory emf) {
      this.emf = Optional.of(emf);
      return this;
    }

    /**
     * Set a custom EDM name builder {@link JPAEdmNameBuilder}. If non is provided {@link JPADefaultEdmNameBuilder} is
     * used, which uses the provided persistence-unit name ({@link JPAODataServiceContext.Builder#setPUnit}) as
     * namespace.
     * @param nameBuilder
     * @return
     */
    public Builder setEdmNameBuilder(final JPAEdmNameBuilder nameBuilder) {
      this.nameBuilder = nameBuilder;
      return this;
    }

    public <T extends JPAODataBatchProcessor> Builder setBatchProcessorFactory(
        JPAODataBatchProcessorFactory<T> batchProcessorFactory) {
      this.batchProcessorFactory = batchProcessorFactory;
      return this;
    }

    /**
     * Some clients, like Excel, require context url's with an absolute path. The default generation of relative paths
     * can be overruled.<br>
     * @see <a href="https://issues.apache.org/jira/browse/OLINGO-787">Issue OLINGO-787</a>
     * @param useAbsoluteContextURL
     * @return
     */
    public Builder setUseAbsoluteContextURL(boolean useAbsoluteContextURL) {
      this.useAbsoluteContextURL = useAbsoluteContextURL;
      return this;
    }
  }

  class JPADebugSupportWrapper implements DebugSupport {

    private final DebugSupport debugSupport;
    private JPAServiceDebugger debugger;

    public JPADebugSupportWrapper(final DebugSupport debugSupport) {
      super();
      this.debugSupport = debugSupport;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.olingo.server.api.debug.DebugSupport#createDebugResponse(java.lang.String,
     * org.apache.olingo.server.api.debug.DebugInformation)
     */
    @Override
    public ODataResponse createDebugResponse(final String debugFormat, final DebugInformation debugInfo) {
      joinRuntimeInfo(debugInfo);
      return debugSupport.createDebugResponse(debugFormat, debugInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.olingo.server.api.debug.DebugSupport#init(org.apache.olingo.server.api.OData)
     */
    @Override
    public void init(final OData odata) {
      debugSupport.init(odata);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.olingo.server.api.debug.DebugSupport#isUserAuthorized()
     */
    @Override
    public boolean isUserAuthorized() {
      return debugSupport.isUserAuthorized();
    }

    void setDebugger(final JPAServiceDebugger debugger) {
      this.debugger = debugger;
    }

    private void joinRuntimeInfo(final DebugInformation debugInfo) {
      // Olingo create a tree for runtime measurement in DebugTabRuntime.add(final RuntimeMeasurement
      // runtimeMeasurement). The current algorithm (V4.3.0) not working well for batch requests if the own runtime info
      // is just appended (addAll), so insert sorted:
      final List<RuntimeMeasurement> olingoInfo = debugInfo.getRuntimeInformation();
      int startIndex = 0;
      for (RuntimeMeasurement m : debugger.getRuntimeInformation()) {
        for (; startIndex < olingoInfo.size(); startIndex++) {
          if (olingoInfo.get(startIndex).getTimeStarted() > m.getTimeStarted()) {
            break;
          }
        }
        olingoInfo.add(startIndex, m);
        startIndex += 1;
      }
    }
  }
}