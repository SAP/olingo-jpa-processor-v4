package org.apache.olingo.jpa.processor.core.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import org.apache.olingo.jpa.metadata.api.JPAEdmProvider;
import org.apache.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import org.apache.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import org.apache.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import org.apache.olingo.jpa.processor.core.database.JPAODataDatabaseProcessorFactory;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.debug.DebugInformation;
import org.apache.olingo.server.api.debug.DebugSupport;
import org.apache.olingo.server.api.debug.DefaultDebugSupport;

public class JPAODataGetHandler {
  private final String namespace;
  public final EntityManagerFactory emf;
  private final JPAODataContext context;
  private final DataSource ds;
  private final OData odata;

  // TODO enable usage of entity manager factory instead of data source
  public JPAODataGetHandler(final String pUnit, final DataSource ds) throws ODataException {
    super();
    this.namespace = pUnit;
    this.ds = ds;
    this.emf = JPAEntityManagerFactory.getEntityManagerFactory(pUnit, ds);
    this.context = new JPAODataContextImpl();
    this.odata = OData.newInstance();

  }

  public JPAODataContext getJPAODataContext() {
    return context;
  }

  @SuppressWarnings("unchecked")
  public void process(final HttpServletRequest request, final HttpServletResponse response) {
    final ODataHttpHandler handler = odata.createHandler(odata.createServiceMetadata(context.getEdmProvider(), context
        .getReferences()));
    context.getEdmProvider().setRequestLocales(request.getLocales());
    context.initDebugger(request.getParameter(DebugSupport.ODATA_DEBUG_QUERY_PARAMETER));
    handler.register(context.getDebugSupport());
    handler.register(new JPAODataRequestProcessor(context, emf.createEntityManager()));
    handler.register(new JPAODataBatchProcessor());
    handler.process(request, response);

  }

  private class JPAODataContextImpl implements JPAODataContext {
    private List<EdmxReference> references = new ArrayList<EdmxReference>();
    private JPADebugSupportWrapper debugSupport = new JPADebugSupportWrapper(new DefaultDebugSupport());
    private JPAODataDatabaseOperations operationConverter;
    private JPAEdmProvider jpaEdm;
    private JPAODataDatabaseProcessor databaseProcessor;
    private JPAServiceDebugger debugger;

    public JPAODataContextImpl() throws ODataException {
      super();

      operationConverter = new JPADefaultDatabaseProcessor();
      jpaEdm = new JPAEdmProvider(namespace, emf, null);
      try {
        databaseProcessor = new JPAODataDatabaseProcessorFactory().create(ds);
      } catch (SQLException e) {
        throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }

    }

    @Override
    public DebugSupport getDebugSupport() {
      return debugSupport;
    }

    @Override
    public JPAODataDatabaseOperations getOperationConverter() {
      return operationConverter;
    }

    @Override
    public List<EdmxReference> getReferences() {
      return references;
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
    public void setMetadataPostProcessor(final JPAEdmMetadataPostProcessor postProcessor) throws ODataException {
      jpaEdm = new JPAEdmProvider(namespace, emf, postProcessor);
    }

    @Override
    public JPAEdmProvider getEdmProvider() {
      return jpaEdm;
    }

    @Override
    public JPAODataDatabaseProcessor getDatabaseProcessor() {
      return databaseProcessor;
    }

    @Override
    public void setDatabaseProcessor(final JPAODataDatabaseProcessor databaseProcessor) {
      this.databaseProcessor = databaseProcessor;
    }

    @Override
    public void setDebugSupport(final DebugSupport jpaDebugSupport) {
      this.debugSupport = new JPADebugSupportWrapper(jpaDebugSupport);
    }

    @Override
    public JPAServiceDebugger getDebugger() {
      return debugger;
    }

    @Override
    public void initDebugger(String debugFormat) {
      // see org.apache.olingo.server.core.debug.ServerCoreDebugger
      boolean isDebugMode = false;

      if (debugSupport != null) {
        // Should we read the parameter from the servlet here and ignore multiple parameters?
        if (debugFormat != null) {
          debugSupport.init(odata);
          isDebugMode = debugSupport.isUserAuthorized();
        }
      }
      if (isDebugMode)
        debugger = new JPACoreDeugger();
      else
        debugger = new JPAEmptyDebugger();
      debugSupport.setDebugger(debugger);
    }
  }

  private class JPADebugSupportWrapper implements DebugSupport {

    final private DebugSupport debugSupport;
    private JPAServiceDebugger debugger;

    public JPADebugSupportWrapper(DebugSupport debugSupport) {
      super();
      this.debugSupport = debugSupport;
    }

    @Override
    public void init(OData odata) {
      debugSupport.init(odata);
    }

    @Override
    public boolean isUserAuthorized() {
      return debugSupport.isUserAuthorized();
    }

    @Override
    public ODataResponse createDebugResponse(String debugFormat, DebugInformation debugInfo) {
      debugInfo.getRuntimeInformation().addAll(debugger.getRuntimeInformation());
      return debugSupport.createDebugResponse(debugFormat, debugInfo);
    }

    void setDebugger(JPAServiceDebugger debugger) {
      this.debugger = debugger;
    }
  }
}
