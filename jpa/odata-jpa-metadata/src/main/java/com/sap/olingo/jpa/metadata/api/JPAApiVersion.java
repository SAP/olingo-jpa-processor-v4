package com.sap.olingo.jpa.metadata.api;

import javax.annotation.Nonnull;

import jakarta.persistence.EntityManagerFactory;

import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAApiVersionProvider.Builder;

public interface JPAApiVersion {

  public static JPAApiVersionBuilder with() {
    return new Builder();
  }

  public String getId();

  public EntityManagerFactory getEntityManagerFactory();

  public @Nonnull String[] getPackageNames();

  public String getRequestMappingPath();

  public JPAEdmMetadataPostProcessor getMetadataPostProcessor();
}
