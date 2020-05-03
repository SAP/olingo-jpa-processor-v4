package com.sap.olingo.jpa.processor.core.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.debug.DebugInformation;
import org.apache.olingo.server.api.debug.DebugSupport;
import org.apache.olingo.server.api.debug.RuntimeMeasurement;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataDefaultTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

public final class JPAODataRequestContextImpl implements JPAODataCRUDRequestContext, JPAODataRequestContextAccess,
    JPARequestContext {

  private Optional<JPAODataClaimProvider> claims = Optional.empty();
  private Optional<JPAODataGroupProvider> groups = Optional.empty();
  private EntityManager em;
  private UriInfoResource uriInfo;
  private JPASerializer serializer;
  private JPAODataPage page;
  private JPACUDRequestHandler jpaCUDRequestHandler;
  private JPAServiceDebugger debugger;
  private JPADebugSupportWrapper debugSupport;
  private String debugFormat;
  private JPAODataTransactionFactory transactionFactory;
  private final Map<JPAAttribute, EdmTransientPropertyCalculator<?>> transientCalculatorCache;
  private Map<String, List<String>> header;

  public JPAODataRequestContextImpl() {
    // Provide all data via setter
    this.transientCalculatorCache = new HashMap<>();
    this.header = Collections.emptyMap();
  }

  /**
   * Copy constructor only using new uri info
   * @param uriInfo
   * @param context
   */
  public JPAODataRequestContextImpl(final UriInfoResource uriInfo, final JPAODataRequestContextAccess context) {
    this(uriInfo, null, context, context.getHeader());
  }

  /**
   * Copy constructor switching also the header
   * @param uriInfo
   * @param context
   */
  public JPAODataRequestContextImpl(final UriInfoResource uriInfo, final JPAODataRequestContextAccess context,
      final Map<String, List<String>> header) {
    this(uriInfo, null, context, header);
  }

  JPAODataRequestContextImpl(final JPAODataPage page, final JPASerializer serializer,
      final JPAODataRequestContextAccess context, final Map<String, List<String>> header)
      throws ODataJPAIllegalAccessException {

    copyContextValues(context);
    this.serializer = serializer;
    this.jpaCUDRequestHandler = new JPADefaultCUDRequestHandler();
    this.transientCalculatorCache = new HashMap<>();
    this.header = header;
    setJPAODataPage(page);
  }

  JPAODataRequestContextImpl(final UriInfoResource uriInfo, @Nullable final JPASerializer serializer,
      final JPAODataRequestContextAccess context, final Map<String, List<String>> header) {
    copyContextValues(context);
    this.serializer = serializer;
    this.uriInfo = uriInfo;
    this.transientCalculatorCache = new HashMap<>();
    this.header = header;
  }

  @Override
  public Optional<JPAODataClaimProvider> getClaimsProvider() {
    return claims;
  }

  @Override
  public JPACUDRequestHandler getCUDRequestHandler() {
    return jpaCUDRequestHandler;
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
  public void setClaimsProvider(final JPAODataClaimProvider provider) {
    claims = Optional.ofNullable(provider);
  }

  @Override
  public void setCUDRequestHandler(@Nonnull final JPACUDRequestHandler jpaCUDRequestHandler) {
    this.jpaCUDRequestHandler = Objects.requireNonNull(jpaCUDRequestHandler);
  }

  @Override
  public void setEntityManager(@Nonnull final EntityManager em) {
    this.em = Objects.requireNonNull(em);
  }

  @Override
  public void setGroupsProvider(final JPAODataGroupProvider provider) {
    groups = Optional.ofNullable(provider);
  }

  @Override
  public void setJPAODataPage(@Nonnull final JPAODataPage page) throws ODataJPAIllegalAccessException {
    if (this.uriInfo != null)
      throw new ODataJPAIllegalAccessException();
    this.setUriInfo(page.getUriInfo());
    this.page = Objects.requireNonNull(page);
  }

  @Override
  public void setDebugSupport(final DebugSupport debugSupport) {
    this.debugSupport = new JPADebugSupportWrapper(debugSupport);

  }

  @Override
  public void setJPASerializer(@Nonnull final JPASerializer serializer) {
    this.serializer = Objects.requireNonNull(serializer);
  }

  @Override
  public void setTransactionFactory(@Nullable final JPAODataTransactionFactory transactionFactory) {
    this.transactionFactory = transactionFactory;
  }

  @Override
  public void setUriInfo(@Nonnull final UriInfo uriInfo) throws ODataJPAIllegalAccessException {
    if (this.page != null)
      throw new ODataJPAIllegalAccessException();
    this.uriInfo = Objects.requireNonNull(uriInfo);
  }

  @Override
  public JPAServiceDebugger getDebugger() {
    if (debugger == null)
      initDebugger();
    return debugger;
  }

  public void setDebugFormat(String debugFormat) {
    this.debugFormat = debugFormat;
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

  private void copyContextValues(final JPAODataRequestContextAccess context) {
    this.claims = context.getClaimsProvider();
    this.groups = context.getGroupsProvider();
    this.em = context.getEntityManager();
    this.jpaCUDRequestHandler = context.getCUDRequestHandler();
    this.debugger = context.getDebugger();
  }

  private void createDefaultTransactionFactory() {
    this.transactionFactory = new JPAODataDefaultTransactionFactory(em);
  }

  private class JPADefaultCUDRequestHandler extends JPAAbstractCUDRequestHandler {

  }

  private class JPADebugSupportWrapper implements DebugSupport {

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

  public JPADebugSupportWrapper getDebugSupport() {
    if (debugger == null)
      initDebugger();
    return debugSupport;
  }

  @Override
  public Optional<EdmTransientPropertyCalculator<?>> getCalculator(@Nonnull final JPAAttribute transientProperty)
      throws ODataJPAProcessorException {
    try {
      if (transientProperty.isTransient()) {
        if (!transientCalculatorCache.containsKey(transientProperty)) {
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
          }
          final EdmTransientPropertyCalculator<?> calculator = c.newInstance(paramValues);
          transientCalculatorCache.put(transientProperty, calculator);
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
  public Map<String, List<String>> getHeader() {
    return header;
  }

}
