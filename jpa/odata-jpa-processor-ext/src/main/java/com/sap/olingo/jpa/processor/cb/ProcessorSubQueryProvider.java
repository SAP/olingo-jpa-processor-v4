package com.sap.olingo.jpa.processor.cb;

public interface ProcessorSubQueryProvider {
  public <U> ProcessorSubquery<U> subquery(Class<U> type);
}
