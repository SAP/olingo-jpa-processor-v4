package com.sap.olingo.jpa.processor.core.api;

import java.util.Map;

import javax.annotation.Nonnull;

public interface JPAODataRequestParameterAccess {

  Object getParameter(@Nonnull String parameterName);

  Map<String, Object> getParameters();

}