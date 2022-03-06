package com.sap.olingo.jpa.processor.core.api;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;

public class JPAODataRequestHandler {
  private static final String REQUEST_MAPPING_ATTRIBUTE = "requestMapping";
  public final Optional<? extends EntityManagerFactory> emf;
  private final JPAODataServiceContext serviceContext;
  private final JPAODataInternalRequestContext requestContext;
  final OData odata;

  /**
   * Create a handler without special request context. Default implementations are used if present.
   * @param serviceContext
   */
  public JPAODataRequestHandler(final JPAODataSessionContextAccess serviceContext) {
    this(serviceContext, OData.newInstance());
  }

  /**
   *
   * @param serviceContext
   * @param requestContext
   */
  public JPAODataRequestHandler(final JPAODataSessionContextAccess serviceContext,
      final JPAODataRequestContext requestContext) {
    this(serviceContext, requestContext, OData.newInstance());
  }

  /**
   * Give the option to inject the odata helper e.g. for testing
   * @param serviceContext
   * @param odata
   */
  JPAODataRequestHandler(final JPAODataSessionContextAccess serviceContext, final OData odata) {
    this(serviceContext, JPAODataRequestContext.with().build(), odata);
  }

  JPAODataRequestHandler(final JPAODataSessionContextAccess serviceContext, final JPAODataRequestContext requestContext,
      final OData odata) {
    this.emf = serviceContext.getEntityManagerFactory();
    this.serviceContext = (JPAODataServiceContext) serviceContext;
    this.requestContext = new JPAODataInternalRequestContext(requestContext, serviceContext);
    this.odata = odata;
  }

  public void process(final HttpServletRequest request, final HttpServletResponse response) throws ODataException {
    if (emf.isPresent() && this.requestContext.getEntityManager() == null) {
      final EntityManager em = emf.get().createEntityManager();
      try {
        this.requestContext.setEntityManager(em);
        processInternal(request, response);
      } finally {
        em.close();
      }
    } else {
      processInternal(request, response);
    }
  }

  @SuppressWarnings("unchecked")
  private void processInternal(final HttpServletRequest request, final HttpServletResponse response)
      throws ODataException {

    final JPAEdmProvider jpaEdm = requestContext.getEdmProvider();
    final ODataHttpHandler handler = odata.createHandler(odata.createServiceMetadata(jpaEdm, jpaEdm.getReferences()));
    serviceContext.getEdmProvider().setRequestLocales(request.getLocales());
    final HttpServletRequest mappedRequest = prepareRequestMapping(request, serviceContext.getMappingPath());
    handler.register(requestContext.getDebugSupport());
    handler.register(new JPAODataRequestProcessor(serviceContext, requestContext));
    handler.register(serviceContext.getBatchProcessorFactory().getBatchProcessor(serviceContext, requestContext));
    handler.register(serviceContext.getEdmProvider().getServiceDocument());
    handler.register(serviceContext.getErrorProcessor());
    handler.register(new JPAODataServiceDocumentProcessor(serviceContext));
    handler.process(mappedRequest, response);
  }

  private HttpServletRequest prepareRequestMapping(final HttpServletRequest req, final String requestPath) {
    if (requestPath != null && !requestPath.isEmpty()) {
      final HttpServletRequestWrapper request = new HttpServletRequestWrapper(req);
      request.setAttribute(REQUEST_MAPPING_ATTRIBUTE, requestPath);
      return request;
    } else {
      return req;
    }
  }
}
