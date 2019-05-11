package com.sap.olingo.jpa.processor.core.api;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Metamodel;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHandler;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.debug.DebugInformation;
import org.apache.olingo.server.api.debug.DebugSupport;
import org.apache.olingo.server.api.debug.RuntimeMeasurement;

import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;

public class JPAODataGetHandler {
  final String namespace;
  public final EntityManagerFactory emf;
  private final JPAODataContextImpl context;
  final DataSource ds;
  final OData odata;
  Metamodel jpaMetamodel;

  public JPAODataGetHandler(final String pUnit) throws ODataException {
    this.namespace = pUnit;
    this.ds = null;
    this.emf = null;
    this.context = new JPAODataContextImpl(this);
    this.odata = OData.newInstance();
  }

  public JPAODataGetHandler(final String pUnit, final DataSource ds) throws ODataException {
    super();
    this.namespace = pUnit;
    this.ds = ds;
    this.emf = JPAEntityManagerFactory.getEntityManagerFactory(pUnit, ds);
    this.jpaMetamodel = emf.getMetamodel();
    this.context = new JPAODataContextImpl(this);
    this.odata = OData.newInstance();

  }

  public JPAODataGetHandler(final String pUnit, final DataSource ds, final EntityManagerFactory emf, JPAEdmProvider edmProvider) throws ODataException {
    super();
    this.namespace = pUnit;
    this.ds = ds;
    this.emf = emf;
    this.jpaMetamodel = emf.getMetamodel();
    this.context = new JPAODataContextImpl(this);
    this.context.setEdmProvider(edmProvider);
    this.odata = OData.newInstance();
  }

  public JPAODataGetContext getJPAODataContext() {
    return context;
  }

  public void process(final HttpServletRequest request, final HttpServletResponse response) throws ODataException {
    final EntityManager em = emf.createEntityManager();
    try {
      process(request, response, null, em);
    } finally {
      em.close();
    }
  }

  @SuppressWarnings("unchecked")
  public void process(final HttpServletRequest request, final HttpServletResponse response,
      final JPAODataClaimsProvider claims, final EntityManager em) throws ODataException {

    this.jpaMetamodel = em.getMetamodel();
    final ODataHttpHandler handler = odata.createHandler(odata.createServiceMetadata(context.getEdmProvider(), context
        .getEdmProvider().getReferences()));
    context.getEdmProvider().setRequestLocales(request.getLocales());
    context.initDebugger(request.getParameter(DebugSupport.ODATA_DEBUG_QUERY_PARAMETER));
    handler.register(context.getDebugSupport());
    handler.register(new JPAODataRequestProcessor(context, claims, em));
    handler.register(new JPAODataBatchProcessor(context, em));
    handler.register(context.getEdmProvider().getServiceDocument());
    handler.register(context.getErrorProcessor());
    handler.process(request, response);
  }

  public void process(final HttpServletRequest request, final HttpServletResponse response, final EntityManager em)
      throws ODataException {

    process(request, response, null, em);
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
