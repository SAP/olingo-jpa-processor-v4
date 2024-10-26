package com.sap.olingo.jpa.processor.core.api;

import jakarta.persistence.EntityManagerFactory;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;

public interface JPAODataApiVersionAccess {
  public final String DEFAULT_VERSION = "DEFAULT";

  String getId();

  JPAEdmProvider getEdmProvider();

  EntityManagerFactory getEntityManagerFactory();

  String getMappingPath();
}
