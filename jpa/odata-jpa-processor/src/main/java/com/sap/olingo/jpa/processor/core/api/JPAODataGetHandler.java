package com.sap.olingo.jpa.processor.core.api;

import java.util.Optional;

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
import org.apache.olingo.server.api.debug.DebugSupport;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.processor.core.processor.JPAODataRequestContextImpl;

public class JPAODataGetHandler {
  public final Optional<EntityManagerFactory> emf;
  private final JPAODataServiceContext serviceContext;
  private final JPAODataRequestContextImpl requestContext;
  final OData odata;
  @Deprecated
  final DataSource ds;
  @Deprecated
  final String namespace;
  @Deprecated
  final Metamodel jpaMetamodel;

  /**
   * @deprecated (Will be removed with 1.0.0, use service context builder, <code>JPAODataServiceContext.with()</code>
   * instead
   * @param pUnit
   * @throws ODataException
   */
  @Deprecated
  public JPAODataGetHandler(final String pUnit) throws ODataException {// NOSONAR
    this(pUnit, (DataSource) null);
  }

  /**
   * @deprecated (Will be removed with 1.0.0, use service context builder, <code>JPAODataServiceContext.with()</code>
   * instead
   * @param pUnit
   * @param ds
   * @throws ODataException
   */
  @Deprecated
  public JPAODataGetHandler(final String pUnit, final DataSource ds) throws ODataException {
    super();
    this.namespace = pUnit;
    this.ds = ds;
    this.emf = ds != null ? Optional.ofNullable(JPAEntityManagerFactory.getEntityManagerFactory(pUnit, ds))
        : Optional.empty();
    this.jpaMetamodel = emf.isPresent() ? emf.get().getMetamodel() : null;
    this.serviceContext = new JPAODataServiceContext(this);
    this.requestContext = new JPAODataRequestContextImpl();
    this.odata = OData.newInstance();
  }

  public JPAODataGetHandler(final JPAODataCRUDContextAccess serviceContext) {
    this.namespace = null;
    this.ds = null;
    this.emf = serviceContext.getEntityManagerFactory();
    this.jpaMetamodel = null;
    this.serviceContext = (JPAODataServiceContext) serviceContext;
    this.requestContext = new JPAODataRequestContextImpl();
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
    return serviceContext;
  }

  public JPAODataRequestContext getJPAODataRequestContext() {
    return requestContext;
  }

  public void process(final HttpServletRequest request, final HttpServletResponse response) throws ODataException {
    if (emf.isPresent() && this.requestContext.getEntityManager() == null) {
      final EntityManager em = emf.get().createEntityManager();
      try {
        process(request, response, em);
      } finally {
        em.close();
      }
    } else {
      processInternal(request, response);
    }
  }

  /**
   * @deprecated (Will be removed with 1.0.0, parameter <code>em</code> not longer supported,
   * use Request Context (<code>getJPAODataRequestContext</code>) instead)
   * @param request
   * @param response
   * @param em
   * @throws ODataException
   */
  @Deprecated
  public void process(final HttpServletRequest request, final HttpServletResponse response, final EntityManager em)
      throws ODataException {

    this.requestContext.setEntityManager(em);
    process(request, response);
  }

  /**
   * @deprecated (Will be removed with 1.0.0, parameter <code>claims</code> and <code>em</code> not longer supported,
   * use Request Context (<code>getJPAODataRequestContext</code>) instead)
   * @param request
   * @param response
   * @param claims
   * @param em
   * @throws ODataException
   */
  @Deprecated
  public void process(final HttpServletRequest request, final HttpServletResponse response,
      final JPAODataClaimProvider claims, final EntityManager em) throws ODataException {

    this.requestContext.setClaimsProvider(claims);
    this.requestContext.setEntityManager(em);

    process(request, response);
  }

  @SuppressWarnings("unchecked")
  private void processInternal(final HttpServletRequest request, final HttpServletResponse response)
      throws ODataException {

    final JPAEdmProvider jpaEdm = serviceContext.getEdmProvider() == null
        && serviceContext instanceof JPAODataServiceContext ? serviceContext.getEdmProvider(requestContext
            .getEntityManager())
            : serviceContext.getEdmProvider();

    final ODataHttpHandler handler = odata.createHandler(odata.createServiceMetadata(jpaEdm, jpaEdm.getReferences()));
    serviceContext.getEdmProvider().setRequestLocales(request.getLocales());
    requestContext.setDebugFormat(request.getParameter(DebugSupport.ODATA_DEBUG_QUERY_PARAMETER));
    setCUDHandler();
    handler.register(requestContext.getDebugSupport());
    handler.register(new JPAODataRequestProcessor(serviceContext, requestContext));
    handler.register(new JPAODataBatchProcessor(requestContext));
    handler.register(serviceContext.getEdmProvider().getServiceDocument());
    handler.register(serviceContext.getErrorProcessor());
    handler.process(request, response);
  }

  private void setCUDHandler() {
    if (serviceContext.getCUDRequestHandler() != null && requestContext.getCUDRequestHandler() == null)
      requestContext.setCUDRequestHandler(serviceContext.getCUDRequestHandler());
  }

}
