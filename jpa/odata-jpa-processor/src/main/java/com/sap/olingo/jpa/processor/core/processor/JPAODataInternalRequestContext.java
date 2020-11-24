package com.sap.olingo.jpa.processor.core.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataDefaultTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestParameterAccess;
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
  private final Map<JPAAttribute, EdmTransientPropertyCalculator<?>> transientCalculatorCache;
  private final Map<String, List<String>> header;
  private Map<String, Object> customParameter;
  private List<Locale> locales;

  public JPAODataInternalRequestContext() {
    this(null);

  }

  public JPAODataInternalRequestContext(@Nullable final JPAODataRequestContext requestContext) {
    this.transientCalculatorCache = new HashMap<>();
    this.header = Collections.emptyMap();
    copyRequestContext(requestContext);
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
    this.transientCalculatorCache = new HashMap<>();
    this.header = header;
    this.customParameter = new HashMap<>(context.getParameters());
    setJPAODataPage(page);
  }

  JPAODataInternalRequestContext(final UriInfoResource uriInfo, @Nullable final JPASerializer serializer,
      final JPAODataRequestContextAccess context, final Map<String, List<String>> header) {
    copyContextValues(context);
    this.serializer = serializer;
    this.uriInfo = uriInfo;
    this.transientCalculatorCache = new HashMap<>();
    this.header = header;
    this.customParameter = new HashMap<>(context.getParameters());
  }

  @Override
  public Optional<EdmTransientPropertyCalculator<?>> getCalculator(@Nonnull final JPAAttribute transientProperty)
      throws ODataJPAProcessorException {
    try {
      if (transientProperty.isTransient()) {
        if (!transientCalculatorCache.containsKey(transientProperty)) {
          createCalculator(transientProperty);
        }
        return Optional.of(transientCalculatorCache.get(transientProperty));
      }
    } catch (ODataJPAModelException | InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    return Optional.empty();
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
  public Map<String, List<String>> getHeader() {
    return header;
  }

  @Override
  public JPAODataPage getPage() {
    return page;
  }

  @Override
  public Object getParameter(final String parameterName) {
    return customParameter.get(parameterName);
  }

  @Override
  public Map<String, Object> getParameters() {
    return customParameter;
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
      return ExpressionUtil.determineLocale(header);
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
    customParameter = requestContext != null ? requestContext.getParameters() : new HashMap<>();
  }

  private void createCalculator(final JPAAttribute transientProperty) throws ODataJPAModelException,
      InstantiationException, IllegalAccessException, InvocationTargetException {
    final Constructor<? extends EdmTransientPropertyCalculator<?>> c = transientProperty
        .getCalculatorConstructor();
    final Parameter[] parameters = c.getParameters();
    final Object[] paramValues = new Object[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      final Parameter parameter = parameters[i];
      if (parameter.getType().isAssignableFrom(EntityManager.class))
        paramValues[i] = em;
      if (parameter.getType().isAssignableFrom(Map.class))
        paramValues[i] = header;
      if (parameter.getType().isAssignableFrom(JPAODataRequestParameterAccess.class))
        paramValues[i] = this;
    }
    final EdmTransientPropertyCalculator<?> calculator = c.newInstance(paramValues);
    transientCalculatorCache.put(transientProperty, calculator);
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
