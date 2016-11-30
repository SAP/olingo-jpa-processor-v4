package com.sap.olingo.jpa.processor.core.api;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Metamodel;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.debug.DebugInformation;
import org.apache.olingo.server.api.debug.DebugSupport;

import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;

public class JPAODataGetHandler {
  final String namespace;
  public final EntityManagerFactory emf;
  private final JPAODataContextImpl context;
  final DataSource ds;
  final OData odata;
  Metamodel jpaMetamodel;

  public JPAODataGetHandler(final String pUnit, final DataSource ds) throws ODataException {
    super();
    this.namespace = pUnit;
    this.ds = ds;
    this.emf = JPAEntityManagerFactory.getEntityManagerFactory(pUnit, ds);
    this.jpaMetamodel = emf.getMetamodel();
    this.context = new JPAODataContextImpl(this);
    this.odata = OData.newInstance();

  }

  public JPAODataGetHandler(final String pUnit) throws ODataException {
    this.namespace = pUnit;
    this.ds = null;
    this.emf = null;
    this.context = new JPAODataContextImpl(this);
    this.odata = OData.newInstance();
  }

  public JPAODataGetContext getJPAODataContext() {
    return context;
  }

  public void process(final HttpServletRequest request, final HttpServletResponse response) throws ODataException {
    process(request, response, emf.createEntityManager());
  }

  @SuppressWarnings("unchecked")
  public void process(final HttpServletRequest request, final HttpServletResponse response, final EntityManager em)
      throws ODataException {

    this.jpaMetamodel = em.getMetamodel();
    final ODataHttpHandler handler = odata.createHandler(odata.createServiceMetadata(context.getEdmProvider(), context
        .getEdmProvider().getReferences()));
    context.getEdmProvider().setRequestLocales(request.getLocales());
    context.initDebugger(request.getParameter(DebugSupport.ODATA_DEBUG_QUERY_PARAMETER));
    handler.register(context.getDebugSupport());
    handler.register(new JPAODataRequestProcessor(context, em));
    handler.register(new JPAODataBatchProcessor(em));
    handler.process(request, response);
  }

  class JPADebugSupportWrapper implements DebugSupport {

    final private DebugSupport debugSupport;
    private JPAServiceDebugger debugger;

    public JPADebugSupportWrapper(final DebugSupport debugSupport) {
      super();
      this.debugSupport = debugSupport;
    }

    @Override
    public void init(final OData odata) {
      debugSupport.init(odata);
    }

    @Override
    public boolean isUserAuthorized() {
      return debugSupport.isUserAuthorized();
    }

    @Override
    public ODataResponse createDebugResponse(final String debugFormat, final DebugInformation debugInfo) {
      debugInfo.getRuntimeInformation().addAll(debugger.getRuntimeInformation());
      return debugSupport.createDebugResponse(debugFormat, debugInfo);
    }

    void setDebugger(final JPAServiceDebugger debugger) {
      this.debugger = debugger;
    }
  }

}
