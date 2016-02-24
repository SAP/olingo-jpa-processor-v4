package org.apache.olingo.jpa.processor.core.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import org.apache.olingo.jpa.metadata.api.JPAEdmProvider;
import org.apache.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import org.apache.olingo.jpa.processor.core.database.JPAODataDatabaseProcessorFactory;
import org.apache.olingo.jpa.processor.core.filter.JPAOperationConverter;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.debug.DebugSupport;
import org.apache.olingo.server.api.edmx.EdmxReference;

public class JPAODataGetHandler {
  private final String namespace;
  public final EntityManagerFactory emf;
  private final JPAODataContext context;
  private final DataSource ds;
  private final OData odata;

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

  public void process(final HttpServletRequest request, final HttpServletResponse response) {
    final ODataHttpHandler handler = odata.createHandler(odata.createServiceMetadata(context.getEdmProvider(), context
        .getReferences()));
    handler.register(context.getDebugSupport());
    handler.register(new JPAODataRequestProcessor(context, emf.createEntityManager()));
    handler.register(new JPAODataBatchProcessor(context, emf));
    handler.process(request, response);

  }

  private class JPAODataContextImpl implements JPAODataContext {
    private List<EdmxReference> references = new ArrayList<EdmxReference>();
    private DebugSupport debugSupport;
    private JPAOperationConverter operationConverter;
    private JPAEdmProvider jpaEdm;
    private JPAODataDatabaseProcessor databaseProcessor;

    public JPAODataContextImpl() throws ODataException {
      super();

      operationConverter = new JPAOperationConverter(null);
      jpaEdm = new JPAEdmProvider(namespace, emf, null);
      try {
        databaseProcessor = new JPAODataDatabaseProcessorFactory().create(ds);
      } catch (SQLException e) {
        // TODO Error Handling
        e.printStackTrace();
      }
    }

    @Override
    public DebugSupport getDebugSupport() {
      return debugSupport;
    }

    @Override
    public JPAOperationConverter getOperationConverter() {
      return operationConverter;
    }

    @Override
    public List<EdmxReference> getReferences() {
      return references;
    }

    @Override
    public void register(final DebugSupport debugSupport) {
      this.debugSupport = debugSupport;
    }

    @Override
    public void setOperationConverter(final JPAOperationConverter jpaOperationConverter) {
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
  }
}
