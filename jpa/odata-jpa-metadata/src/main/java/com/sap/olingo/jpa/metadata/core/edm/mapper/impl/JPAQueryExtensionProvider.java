package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.EXTENSION_PROVIDER_TOO_MANY_CONSTRUCTORS;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.EXTENSION_PROVIDER_WRONG_PARAMETER;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Map;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAQueryExtension;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class JPAQueryExtensionProvider<X extends EdmQueryExtensionProvider> implements JPAQueryExtension<X> {

  private final Constructor<X> constructor;

  @SuppressWarnings("unchecked")
  JPAQueryExtensionProvider(@Nonnull final Class<X> provider)
      throws ODataJPAModelException {

    final Constructor<?>[] constructors = provider.getConstructors();
    if (constructors.length > 1)
      throw new ODataJPAModelException(EXTENSION_PROVIDER_TOO_MANY_CONSTRUCTORS, provider.getCanonicalName());
    final Constructor<?> c = provider.getConstructors()[0];
    for (final Parameter p : c.getParameters()) {
      if (p.getType() != Map.class)
        throw new ODataJPAModelException(EXTENSION_PROVIDER_WRONG_PARAMETER, provider.getCanonicalName());
    }
    this.constructor = (Constructor<X>) provider.getConstructors()[0];
  }

  @Override
  public Constructor<X> getConstructor() {
    return constructor;
  }

}
