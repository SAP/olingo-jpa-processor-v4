package com.sap.olingo.jpa.processor.core.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.debug.DebugSupport;
import org.apache.olingo.server.api.debug.DefaultDebugSupport;
import org.apache.olingo.server.api.processor.ErrorProcessor;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGetHandler.JPADebugSupportWrapper;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseProcessorFactory;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

final class JPAODataContextImpl implements JPAODataCRUDContext, JPAODataSessionContextAccess {
  /**
   * 
   */
  private final JPAODataGetHandler jpaoDataGetHandler;
  private List<EdmxReference> references = new ArrayList<>();
  private JPADebugSupportWrapper debugSupport;
  private JPAODataDatabaseOperations operationConverter;
  private JPAEdmProvider jpaEdm;
  private JPAODataDatabaseProcessor databaseProcessor;
  private JPAServiceDebugger debugger;
  private JPAEdmMetadataPostProcessor postProcessor;
  private JPACUDRequestHandler jpaCUDRequestHandler;
  private String[] packageName;
  private ErrorProcessor errorProcessor;
  private JPAODataPagingProvider pagingProvider;

  public JPAODataContextImpl(JPAODataGetHandler jpaoDataGetHandler) throws ODataException {
    super();
    this.jpaoDataGetHandler = jpaoDataGetHandler;
    this.debugSupport = this.jpaoDataGetHandler.new JPADebugSupportWrapper(new DefaultDebugSupport());
    operationConverter = new JPADefaultDatabaseProcessor();
    try {
      databaseProcessor = new JPAODataDatabaseProcessorFactory().create(this.jpaoDataGetHandler.ds);
    } catch (SQLException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

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
  public JPAServiceDebugger getDebugger() {
    return debugger;
  }

  @Override
  public DebugSupport getDebugSupport() {
    return debugSupport;
  }

  @Override
  public JPAEdmProvider getEdmProvider() throws ODataException {
    if (jpaEdm == null)
      jpaEdm = new JPAEdmProvider(this.jpaoDataGetHandler.namespace, this.jpaoDataGetHandler.jpaMetamodel,
          postProcessor, packageName);
    return jpaEdm;
  }

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
  public List<EdmxReference> getReferences() {
    return references;
  }

  @Override
  public JPAODataPagingProvider getPagingProvider() {
    return pagingProvider;
  }

  @Override
  public void initDebugger(final String debugFormat) {
    // see org.apache.olingo.server.core.debug.ServerCoreDebugger
    boolean isDebugMode = false;

    if (debugSupport != null) {
      // Should we read the parameter from the servlet here and ignore multiple parameters?
      if (debugFormat != null) {
        debugSupport.init(this.jpaoDataGetHandler.odata);
        isDebugMode = debugSupport.isUserAuthorized();
      }
      if (isDebugMode)
        debugger = new JPACoreDeugger();
      else
        debugger = new JPAEmptyDebugger();
      debugSupport.setDebugger(debugger);
    }
  }

  @Override
  public void setCUDRequestHandler(JPACUDRequestHandler jpaCUDRequestHandler) {
    this.jpaCUDRequestHandler = jpaCUDRequestHandler;
  }

  @Override
  public void setDatabaseProcessor(final JPAODataDatabaseProcessor databaseProcessor) {
    this.databaseProcessor = databaseProcessor;
  }

  @Override
  public void setDebugSupport(final DebugSupport jpaDebugSupport) {
    this.debugSupport = this.jpaoDataGetHandler.new JPADebugSupportWrapper(jpaDebugSupport);
  }

  @Override
  public void setErrorProcessor(ErrorProcessor errorProcessor) {
    this.errorProcessor = errorProcessor;
  }

  @Override
  public void setMetadataPostProcessor(final JPAEdmMetadataPostProcessor postProcessor) throws ODataException {
    if (this.jpaoDataGetHandler.jpaMetamodel != null)
      jpaEdm = new JPAEdmProvider(this.jpaoDataGetHandler.namespace, this.jpaoDataGetHandler.jpaMetamodel,
          postProcessor, packageName);
    else
      this.postProcessor = postProcessor;
  }

  @Override
  public void setOperationConverter(final JPAODataDatabaseOperations jpaOperationConverter) {
    operationConverter = jpaOperationConverter;
  }

  @Override
  public void setReferences(final List<EdmxReference> references) {
    this.references = references;
  }

  @Override
  public void setTypePackage(final String... packageName) {
    this.packageName = packageName;

  }

  @Override
  public void setPagingProvider(final JPAODataPagingProvider provider) {
    this.pagingProvider = provider;
  }
}