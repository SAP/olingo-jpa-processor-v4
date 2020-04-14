package com.sap.olingo.jpa.processor.cb.api;

import javax.annotation.Nonnull;

public interface SqlConvertable {

  public StringBuilder asSQL(@Nonnull final StringBuilder statment);
}
