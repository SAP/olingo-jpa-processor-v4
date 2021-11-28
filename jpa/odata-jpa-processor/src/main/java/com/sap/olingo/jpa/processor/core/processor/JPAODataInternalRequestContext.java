package com.sap.olingo.jpa.processor.core.processor;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataDefaultTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.query.ExpressionUtil;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

public final class JPAODataInternalRequestContext implements JPAODataRequestContextAccess,
    ODATARequestContext {

  private Optional<JPAODataClaimProvider> claims;
  private Optional<JPAODataGroupProvider> groups;
  private EntityManager em;
  private UriInfoResource uriInfo;
  private JPASerializer serializer;
  private JPAODataPage page;
  private JPACUDRequestHandler cudRequestHandler;
  private JPAServiceDebugger debugger;
  private JPADebugSupportWrapper debugSupport;
  private String debugFormat;
  private JPAODataTransactionFactory transactionFactory;
  private final JPAHttpHeaderMap header;
  private JPARequestParameterMap customParameter;
  private List<Locale> locales;
  private final JPAHookFactory hookFactory;

  public JPAODataInternalRequestContext() {
    this(null);
  }

  public JPAODataInternalRequestContext(@Nullable final JPAODataRequestContext requestContext) {
    this.header = new JPAHttpHeaderHashMap(Collections.emptyMap());
    copyRequestContext(requestContext);
    this.hookFactory = new JPAHookFactory(em, header, customParameter);
    initDebugger();
  }

  /**
   * Copy constructor only using new uri info
   * @param uriInfo
   * @param context
   */
  public JPAODataInternalRequestContext(final UriInfoResource uriInfo, final JPAODataRequestContextAccess context) {
    this(uriInfo, null, context, context.getHeader());
  }

  /**
   * Copy constructor switching also the header
   * @param uriInfo
   * @param context
   */
  public JPAODataInternalRequestContext(final UriInfoResource uriInfo, final JPAODataRequestContextAccess context,
      final Map<String, List<String>> header) {
    this(uriInfo, null, context, header);
  }

  JPAODataInternalRequestContext(final JPAODataPage page, final JPASerializer serializer,
      final JPAODataRequestContextAccess context, final Map<String, List<String>> header)
      throws ODataJPAIllegalAccessException {

    copyContextValues(context);
    this.serializer = serializer;
    this.cudRequestHandler = new JPADefaultCUDRequestHandler();
    this.header = new JPAHttpHeaderHashMap(header);
    this.customParameter = new JPARequestParameterHashMap(context.getRequestParameter());
    this.hookFactory = new JPAHookFactory(em, this.header, customParameter);
    setJPAODataPage(page);
  }

  JPAODataInternalRequestContext(final UriInfoResource uriInfo, @Nullable final JPASerializer serializer,
      final JPAODataRequestContextAccess context, final Map<String, List<String>> header) {

    copyContextValues(context);
    this.serializer = serializer;
    this.uriInfo = uriInfo;
    this.header = new JPAHttpHeaderHashMap(header);
    this.customParameter = new JPARequestParameterHashMap(context.getRequestParameter());
    this.hookFactory = new JPAHookFactory(em, this.header, customParameter);
  }

  @Override
  public Optional<EdmTransientPropertyCalculator<?>> getCalculator(@Nonnull final JPAAttribute transientProperty)
      throws ODataJPAProcessorException {
    return hookFactory.getTransientPropertyCalculator(transientProperty);
  }

  @Override
  public Optional<EdmQueryExtensionProvider> getQueryEnhancement(final JPAEntityType et)
      throws ODataJPAProcessorException {
    return hookFactory.getQueryExtensionProvider(et);
  }

  @Override
  public Optional<JPAODataClaimProvider> getClaimsProvider() {
    return claims;
  }

  @Override
  public JPACUDRequestHandler getCUDRequestHandler() {
    return cudRequestHandler;
  }

  @Override
  public JPAServiceDebugger getDebugger() {
    if (debugger == null)
      initDebugger();
    return debugger;
  }

  public JPADebugSupportWrapper getDebugSupport() {
    if (debugger == null)
      initDebugger();
    return debugSupport;
  }

  @Override
  public EntityManager getEntityManager() {
    return this.em;
  }

  @Override
  public Optional<JPAODataGroupProvider> getGroupsProvider() {
    return groups;
  }

  @Override
  public JPAHttpHeaderMap getHeader() {
    return header;
  }

  @Override
  public JPARequestParameterMap getRequestParameter() {
    return customParameter;
  }

  @Override
  public JPAODataPage getPage() {
    return page;
  }

  @Override
  public JPASerializer getSerializer() {
    return serializer;
  }

  @Override
  public JPAODataTransactionFactory getTransactionFactory() {
    if (transactionFactory == null)
      createDefaultTransactionFactory();
    return this.transactionFactory;
  }

  @Override
  public UriInfoResource getUriInfo() {
    return this.uriInfo;
  }

  @Override
  public Locale getLocale() {
    if (locales == null || locales.isEmpty())
      return ExpressionUtil.determineFallbackLocale(header);
    return locales.get(0);
  }

  @Override
  public List<Locale> getProvidedLocale() {
    return locales;
  }

  public void setDebugFormat(final String debugFormat) {
    this.debugFormat = debugFormat;
  }

  public void setEntityManager(@Nonnull final EntityManager em) {
    this.em = Objects.requireNonNull(em);
  }

  @Override
  public void setJPAODataPage(@Nonnull final JPAODataPage page) throws ODataJPAIllegalAccessException {
    if (this.uriInfo != null)
      throw new ODataJPAIllegalAccessException();
    this.setUriInfo(page.getUriInfo());
    this.page = Objects.requireNonNull(page);
  }

  @Override
  public void setJPASerializer(@Nonnull final JPASerializer serializer) {
    this.serializer = Objects.requireNonNull(serializer);
  }

  @Override
  public void setUriInfo(@Nonnull final UriInfo uriInfo) throws ODataJPAIllegalAccessException {
    if (this.page != null)
      throw new ODataJPAIllegalAccessException();
    this.uriInfo = Objects.requireNonNull(uriInfo);
  }

  private void copyContextValues(final JPAODataRequestContextAccess context) {
    this.claims = context.getClaimsProvider();
    this.groups = context.getGroupsProvider();
    this.em = context.getEntityManager();
    this.cudRequestHandler = context.getCUDRequestHandler();
    this.debugger = context.getDebugger();
    this.locales = context.getProvidedLocale();
  }

  private void copyRequestContext(final JPAODataRequestContext requestContext) {

    em = requestContext != null ? requestContext.getEntityManager() : null;
    claims = requestContext != null ? requestContext.getClaimsProvider() : Optional.empty();
    groups = requestContext != null ? requestContext.getGroupsProvider() : Optional.empty();
    cudRequestHandler = requestContext != null ? requestContext.getCUDRequestHandler()
        : new JPADefaultCUDRequestHandler();
    debugSupport = requestContext != null && requestContext.getDebuggerSupport() != null ? new JPADebugSupportWrapper(
        debugSupport) : null;
    transactionFactory = requestContext != null ? requestContext.getTransactionFactory() : null;
    locales = requestContext != null ? requestContext.getLocales() : Collections.emptyList();
    customParameter = requestContext != null ? requestContext.getRequestParameter() : new JPARequestParameterHashMap();
  }

  private void createDefaultTransactionFactory() {
    this.transactionFactory = new JPAODataDefaultTransactionFactory(em);
  }

  private void initDebugger() {
    // see org.apache.olingo.server.core.debug.ServerCoreDebugger
    boolean isDebugMode = false;
    debugger = new JPAEmptyDebugger();
    if (debugSupport != null) {
      // Should we read the parameter from the servlet here and ignore multiple parameters?
      if (debugFormat != null) {
        debugSupport.init(OData.newInstance());
        isDebugMode = debugSupport.isUserAuthorized();
      }
      debugger = new JPACoreDebugger(isDebugMode);
      debugSupport.setDebugger(debugger);
    }
  }

  private static class JPADefaultCUDRequestHandler extends JPAAbstractCUDRequestHandler {

  }
}
