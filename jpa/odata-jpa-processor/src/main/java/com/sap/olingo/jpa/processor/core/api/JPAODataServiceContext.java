package com.sap.olingo.jpa.processor.core.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.apache.olingo.server.api.debug.DefaultDebugSupport;
import org.apache.olingo.server.api.debug.RuntimeMeasurement;
import org.apache.olingo.server.api.processor.ErrorProcessor;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseProcessorFactory;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

public final class JPAODataServiceContext implements JPAODataCRUDContext, JPAODataCRUDContextAccess {
  /**
   * 
   */
  private final JPAODataGetHandler jpaODataGetHandler;
  private List<EdmxReference> references = new ArrayList<>(); //
  private JPADebugSupportWrapper debugSupport; //
  private JPAODataDatabaseOperations operationConverter; //
  private JPAEdmProvider jpaEdm;
  private JPAODataDatabaseProcessor databaseProcessor; //
  private JPAEdmMetadataPostProcessor postProcessor; //
  private JPACUDRequestHandler jpaCUDRequestHandler; //
  private String[] packageName; //
  private ErrorProcessor errorProcessor; //
  private JPAODataPagingProvider pagingProvider;
  private Optional<EntityManagerFactory> emf;

  public static Builder with() {
    return new Builder();
  }

  /**
   * @deprecated will be removed with 1.0.0; use newly created builder (<code>JPAODataServiceContext.with()</code>)
   * instead
   */
  @Deprecated
  JPAODataServiceContext(JPAODataGetHandler jpaODataGetHandler) throws ODataException {
    super();
    this.jpaODataGetHandler = jpaODataGetHandler;
    this.debugSupport = new JPADebugSupportWrapper(new DefaultDebugSupport());
    operationConverter = new JPADefaultDatabaseProcessor();
    try {
      databaseProcessor = new JPAODataDatabaseProcessorFactory().create(this.jpaODataGetHandler.ds);
    } catch (SQLException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

  }

  private JPAODataServiceContext(final Builder builder) throws ODataException {

    this.jpaODataGetHandler = null;
    operationConverter = builder.operationConverter;
    databaseProcessor = builder.databaseProcessor;
    references = builder.references;
    postProcessor = builder.postProcessor;
    jpaCUDRequestHandler = builder.jpaCUDRequestHandler;
    packageName = builder.packageName;
    errorProcessor = builder.errorProcessor;
    pagingProvider = builder.pagingProvider;
    jpaEdm = builder.jpaEdm;
    emf = builder.emf;
  }

  @Override
  public JPACUDRequestHandler getCUDRequestHandler() {
    return jpaCUDRequestHandler;
  }

  @Override
  public JPAODataDatabaseProcessor getDatabaseProcessor() {
    return databaseProcessor;
  }

  @Override
  public DebugSupport getDebugSupport() {
    return debugSupport;
  }

  @Override
  public JPAEdmProvider getEdmProvider() throws ODataException {
    if (jpaEdm == null)
      jpaEdm = new JPAEdmProvider(this.jpaODataGetHandler.namespace, this.jpaODataGetHandler.jpaMetamodel,
          postProcessor, packageName);
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
  public String[] getPackageName() {
    return packageName;
  }

  @Override
  public JPAODataPagingProvider getPagingProvider() {
    return pagingProvider;
  }

  @Override
  public List<EdmxReference> getReferences() {
    return references;
  }

  /**
   * @deprecated will be removed with 1.0.0;
   */
  @Deprecated
  @Override
  public void initDebugger(String debugFormat) {
    // method deprecated
  }

  /**
   * @deprecated will be removed with 1.0.0; use newly created builder (<code>JPAODataServiceContext.with()</code>)
   * instead
   */
  @Deprecated
  @Override
  public void setCUDRequestHandler(JPACUDRequestHandler jpaCUDRequestHandler) {
    this.jpaCUDRequestHandler = jpaCUDRequestHandler;
  }

  /**
   * @deprecated will be removed with 1.0.0; use newly created builder (<code>JPAODataServiceContext.with()</code>)
   * instead
   */
  @Deprecated
  @Override
  public void setDatabaseProcessor(final JPAODataDatabaseProcessor databaseProcessor) {
    this.databaseProcessor = databaseProcessor;
  }

  /**
   * @deprecated will be removed with 1.0.0; use newly created builder (<code>JPAODataServiceContext.with()</code>)
   * instead
   */
  @Deprecated
  @Override
  public void setDebugSupport(final DebugSupport jpaDebugSupport) {
    this.debugSupport = new JPADebugSupportWrapper(jpaDebugSupport);
  }

  /**
   * @deprecated will be removed with 1.0.0; use newly created builder (<code>JPAODataServiceContext.with()</code>)
   * instead
   */
  @Deprecated
  @Override
  public void setErrorProcessor(ErrorProcessor errorProcessor) {
    this.errorProcessor = errorProcessor;
  }

  /**
   * @deprecated will be removed with 1.0.0; use newly created builder (<code>JPAODataServiceContext.with()</code>)
   * instead
   */
  @Deprecated
  @Override
  public void setMetadataPostProcessor(final JPAEdmMetadataPostProcessor postProcessor) throws ODataException {
    if (this.jpaODataGetHandler.jpaMetamodel != null)
      jpaEdm = new JPAEdmProvider(this.jpaODataGetHandler.namespace, this.jpaODataGetHandler.jpaMetamodel,
          postProcessor, packageName);
    else
      this.postProcessor = postProcessor;
  }

  /**
   * @deprecated will be removed with 1.0.0; use newly created builder (<code>JPAODataServiceContext.with()</code>)
   * instead
   */
  @Deprecated
  @Override
  public void setOperationConverter(final JPAODataDatabaseOperations jpaOperationConverter) {
    operationConverter = jpaOperationConverter;
  }

  /**
   * @deprecated will be removed with 1.0.0; use newly created builder (<code>JPAODataServiceContext.with()</code>)
   * instead
   */
  @Deprecated
  @Override
  public void setPagingProvider(final JPAODataPagingProvider provider) {
    this.pagingProvider = provider;
  }

  /**
   * @deprecated will be removed with 1.0.0; use newly created builder (<code>JPAODataServiceContext.with()</code>)
   * instead
   */
  @Deprecated
  @Override
  public void setReferences(final List<EdmxReference> references) {
    this.references = references;
  }

  /**
   * @deprecated will be removed with 1.0.0; use newly created builder (<code>JPAODataServiceContext.with()</code>)
   * instead
   */
  @Deprecated
  @Override
  public void setTypePackage(final String... packageName) {
    this.packageName = packageName;
  }

  public static class Builder {

    private String namespace;
    private List<EdmxReference> references = new ArrayList<>();
    private JPAODataDatabaseOperations operationConverter = new JPADefaultDatabaseProcessor();
    private JPAODataDatabaseProcessor databaseProcessor;
    private JPAEdmMetadataPostProcessor postProcessor;
    private JPACUDRequestHandler jpaCUDRequestHandler;
    private String[] packageName;
    private ErrorProcessor errorProcessor;
    private JPAODataPagingProvider pagingProvider;
    private Optional<EntityManagerFactory> emf;
    private DataSource ds;
    private JPAEdmProvider jpaEdm;

    public JPAODataCRUDContextAccess build() throws ODataException {
      try {
        if (packageName == null)
          packageName = new String[0];
        if (ds != null && namespace != null)
          emf = Optional.ofNullable(JPAEntityManagerFactory.getEntityManagerFactory(namespace, ds));
        else
          emf = Optional.empty();
        if (emf.isPresent())
          jpaEdm = new JPAEdmProvider(namespace, emf.get().getMetamodel(), postProcessor, packageName);
        if (databaseProcessor == null) {
          databaseProcessor = new JPAODataDatabaseProcessorFactory().create(ds);
        }
      } catch (SQLException | PersistenceException e) {
        throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
      return new JPAODataServiceContext(this);
    }

    public Builder setDatabaseProcessor(final JPAODataDatabaseProcessor databaseProcessor) {
      this.databaseProcessor = databaseProcessor;
      return this;
    }

    /**
     * 
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
     * 
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