package com.sap.olingo.jpa.processor.core.processor;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.NO_METADATA_PROVIDER;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataDefaultTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataServiceContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
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
  private JPAODataTransactionFactory transactionFactory;
  private final JPAHttpHeaderMap header;
  private JPARequestParameterMap customParameter;
  private List<Locale> locales;
  private final JPAHookFactory hookFactory;
  private JPAODataDatabaseProcessor dbProcessor;
  private Optional<JPAEdmProvider> edmProvider;
  private JPAODataDatabaseOperations operationConverter;

  public JPAODataInternalRequestContext(@Nonnull final JPAODataRequestContext requestContext,
      @Nonnull final JPAODataSessionContextAccess sessionContext) {
    this.header = new JPAHttpHeaderHashMap(Collections.emptyMap());
    copyRequestContext(requestContext, sessionContext);
    this.hookFactory = new JPAHookFactory(em, header, customParameter);
    initDebugger();
  }

  /**
   * Copy constructor only using new uri info
   * @param uriInfo
   * @param context
   * @throws ODataJPAProcessorException
   */
  public JPAODataInternalRequestContext(final UriInfoResource uriInfo, final JPAODataRequestContextAccess context)
      throws ODataJPAProcessorException {
    this(uriInfo, null, context, context.getHeader());
  }

  /**
   * Copy constructor switching also the header
   * @param uriInfo
   * @param context
   * @throws ODataJPAProcessorException
   */
  public JPAODataInternalRequestContext(final UriInfoResource uriInfo, final JPAODataRequestContextAccess context,
      final Map<String, List<String>> header) throws ODataJPAProcessorException {
    this(uriInfo, null, context, header);
  }

  JPAODataInternalRequestContext(final JPAODataPage page, final JPASerializer serializer,
      final JPAODataRequestContextAccess context, final Map<String, List<String>> header)
      throws ODataJPAIllegalAccessException, ODataJPAProcessorException {

    copyContextValues(context);
    this.serializer = serializer;
    this.cudRequestHandler = this.cudRequestHandler == null ? new JPADefaultCUDRequestHandler()
        : this.cudRequestHandler;
    this.header = new JPAHttpHeaderHashMap(header);
    this.customParameter = new JPARequestParameterHashMap(context.getRequestParameter());
    this.hookFactory = new JPAHookFactory(em, this.header, customParameter);
    setJPAODataPage(page);
  }

  JPAODataInternalRequestContext(final UriInfoResource uriInfo, @Nullable final JPASerializer serializer,
      final JPAODataRequestContextAccess context, final Map<String, List<String>> header)
      throws ODataJPAProcessorException {

    copyContextValues(context);
    this.serializer = serializer;
    this.cudRequestHandler = this.cudRequestHandler == null ? new JPADefaultCUDRequestHandler()
        : this.cudRequestHandler;
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
    if (transactionFactory == null && em != null)
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

  @Override
  public JPAODataDatabaseProcessor getDatabaseProcessor() {
    return dbProcessor;
  }

  @Override
  public JPAEdmProvider getEdmProvider() throws ODataJPAProcessorException {
    return edmProvider.orElseThrow(
        () -> new ODataJPAProcessorException(NO_METADATA_PROVIDER, INTERNAL_SERVER_ERROR));
  }

  @Override
  public JPAODataDatabaseOperations getOperationConverter() {
    return operationConverter;
  }

  private void copyContextValues(final JPAODataRequestContextAccess context)
      throws ODataJPAProcessorException {
    this.em = context.getEntityManager();
    this.claims = context.getClaimsProvider();
    this.groups = context.getGroupsProvider();
    this.cudRequestHandler = context.getCUDRequestHandler();
    this.transactionFactory = context.getTransactionFactory();
    this.locales = context.getProvidedLocale();
    this.debugSupport = context instanceof JPAODataInternalRequestContext ? ((JPAODataInternalRequestContext) context)
        .getDebugSupport() : null;
    this.dbProcessor = context.getDatabaseProcessor();
    this.edmProvider = Optional.ofNullable(context.getEdmProvider());
    this.operationConverter = context.getOperationConverter();
  }

  private void copyRequestContext(@Nonnull final JPAODataRequestContext requestContext,
      @Nonnull final JPAODataSessionContextAccess sessionContext) {

    em = requestContext.getEntityManager();
    claims = requestContext.getClaimsProvider();
    groups = requestContext.getGroupsProvider();
    cudRequestHandler = requestContext.getCUDRequestHandler();
    debugSupport = requestContext.getDebuggerSupport() != null ? new JPADebugSupportWrapper(requestContext
        .getDebuggerSupport()) : null;
    transactionFactory = requestContext.getTransactionFactory();
    locales = requestContext.getLocales();
    customParameter = requestContext.getRequestParameter() != null ? requestContext.getRequestParameter()
        : new JPARequestParameterHashMap();
    dbProcessor = sessionContext.getDatabaseProcessor();
    operationConverter = sessionContext.getOperationConverter();
    edmProvider = determineEdmProvider(sessionContext, em);
  }

  private Optional<JPAEdmProvider> determineEdmProvider(final JPAODataSessionContextAccess sessionContext,
      final EntityManager em) {
    try {
      return sessionContext.getEdmProvider() == null
          && sessionContext instanceof JPAODataServiceContext
              ? Optional.ofNullable(((JPAODataServiceContext) sessionContext).getEdmProvider(em))
              : Optional.ofNullable(sessionContext.getEdmProvider());
    } catch (final ODataException e) {
      debugger.debug(this, Arrays.toString(e.getStackTrace()));
      return Optional.empty();
    }
  }

  private void createDefaultTransactionFactory() {
    this.transactionFactory = new JPAODataDefaultTransactionFactory(em);
  }

  private void initDebugger() {
    // see org.apache.olingo.server.core.debug.ServerCoreDebugger
    debugger = new JPAEmptyDebugger();
    if (debugSupport != null) {
      debugger = new JPACoreDebugger(debugSupport.isUserAuthorized());
      debugSupport.addDebugger(debugger);
    }
  }

  private static class JPADefaultCUDRequestHandler extends JPAAbstractCUDRequestHandler {

  }
}
