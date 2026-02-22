package com.sap.olingo.jpa.processor.core.util.matcher;

import java.net.URI;

import org.mockito.ArgumentMatcher;

public abstract class SerializerOptionsMatcher<T> implements ArgumentMatcher<T> {

  protected String extElement;

  protected SerializerOptionsMatcher(final String pattern) {
    extElement = pattern;
  }

  @Override
  public final boolean matches(final T options) {
    return verify(getService(options));
  }

  protected abstract URI getService(T options);

  private boolean verify(final URI serviceRoot) {
    if (serviceRoot == null && extElement == null)
      return true;
    else if (serviceRoot != null && serviceRoot.toString().equals(extElement))
      return true;
    return false;
  }
}