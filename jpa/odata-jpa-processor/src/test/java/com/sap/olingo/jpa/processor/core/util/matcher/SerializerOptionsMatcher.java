package com.sap.olingo.jpa.processor.core.util.matcher;

import java.net.URI;

import org.mockito.ArgumentMatcher;

public abstract class SerializerOptionsMatcher<T> implements ArgumentMatcher<T> {

  protected String externalElement;

  protected SerializerOptionsMatcher(final String pattern) {
    externalElement = pattern;
  }

  @Override
  public final boolean matches(final T options) {
    return verify(getService(options));
  }

  protected abstract URI getService(T options);

  private boolean verify(final URI serviceRoot) {
    if (serviceRoot == null && externalElement == null
        || serviceRoot != null && serviceRoot.toString().equals(externalElement))
      return true;
    return false;
  }
}