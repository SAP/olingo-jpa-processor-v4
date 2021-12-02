package com.sap.olingo.jpa.processor.cb.joiner;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

class AsSqlAnswer implements Answer<StringBuilder> {

  @Override
  public StringBuilder answer(final InvocationOnMock invocation) throws Throwable {
    final StringBuilder statement = invocation.getArgument(0);
    return statement.append("Test");
  }
}
