package com.sap.olingo.jpa.processor.core.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAQueryExtension;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

final class JPAHookFactory {
  private static final Log LOGGER = LogFactory.getLog(JPAHookFactory.class);
  private final Map<JPAAttribute, EdmTransientPropertyCalculator<?>> transientCalculatorCache;
  private final Map<JPAEntityType, EdmQueryExtensionProvider> queryExtensionProviderCache;
  private final JPAHttpHeaderMap header;
  private final EntityManager em;
  private final JPARequestParameterMap requestParameter;

  JPAHookFactory(final EntityManager em, final JPAHttpHeaderMap header, final JPARequestParameterMap parameter) {
    super();
    this.transientCalculatorCache = new HashMap<>();
    this.queryExtensionProviderCache = new HashMap<>();
    this.em = em;
    this.header = header;
    this.requestParameter = parameter;
  }

  public Optional<EdmTransientPropertyCalculator<?>> getTransientPropertyCalculator(
      @Nonnull final JPAAttribute transientProperty) throws ODataJPAProcessorException {
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

  public Optional<EdmQueryExtensionProvider> getQueryExtensionProvider(
      @Nonnull final JPAEntityType et) throws ODataJPAProcessorException {

    if (!queryExtensionProviderCache.containsKey(et)) {
      try {
        queryExtensionProviderCache.put(et, et.getQueryExtention()
            .map(this::createQueryExtensionProvider)
            .orElse(null));
      } catch (final Exception e) {
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    }
    return Optional.ofNullable(queryExtensionProviderCache.get(et));
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
      if (parameter.getType().isAssignableFrom(JPAHttpHeaderMap.class))
        paramValues[i] = header;
      if (parameter.getType().isAssignableFrom(JPARequestParameterMap.class))
        paramValues[i] = requestParameter;
    }
    final EdmTransientPropertyCalculator<?> calculator = c.newInstance(paramValues);
    transientCalculatorCache.put(transientProperty, calculator);
  }

  private EdmQueryExtensionProvider createQueryExtensionProvider(
      final JPAQueryExtension<EdmQueryExtensionProvider> queryExtension) {

    final Constructor<?> c = queryExtension.getConstructor();
    try {
      final Parameter[] parameters = c.getParameters();
      final Object[] paramValues = new Object[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        final Parameter parameter = parameters[i];
        if (parameter.getType().isAssignableFrom(JPAHttpHeaderMap.class))
          paramValues[i] = header;
        if (parameter.getType().isAssignableFrom(JPARequestParameterMap.class))
          paramValues[i] = requestParameter;
      }
      return (EdmQueryExtensionProvider) c.newInstance(paramValues);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      LOGGER.error("Cloud not create Query Extension: " + c.getDeclaringClass().getName(), e);
      return null;
    }
  }
}
