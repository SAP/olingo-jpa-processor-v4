package com.sap.olingo.jpa.processor.cb.api;

import javax.annotation.Nonnull;

public interface SqlConvertible {

  public StringBuilder asSQL(@Nonnull final StringBuilder statement);
}
