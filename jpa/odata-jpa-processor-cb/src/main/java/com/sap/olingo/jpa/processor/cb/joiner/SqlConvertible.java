package com.sap.olingo.jpa.processor.cb.joiner;

import javax.annotation.Nonnull;

public interface SqlConvertible {

  public StringBuilder asSQL(@Nonnull final StringBuilder statement);
}
