package com.sap.olingo.jpa.processor.cb.impl;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

class ClassAnswer implements Answer<Class<?>> {
  private final Class<?> clazz;

  protected ClassAnswer(final Class<?> clazz) {
    this.clazz = clazz;
  }

  @Override
  public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
    return clazz;
  }
}
