package com.sap.olingo.jpa.metadata.api;

import javax.annotation.Nonnull;

import jakarta.persistence.EntityManagerFactory;

import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAApiVersionProvider.Builder;

public interface JPAApiVersionBuilder {

  public Builder setId(@Nonnull final String id);

  /**
   * Set an externally created entity manager factory.<br>
   * This is necessary e.g. in case a spring based service shall run without a <code>persistance.xml</code>.
   * @param emf
   * @return
   */
  public Builder setEntityManagerFactory(@Nonnull final EntityManagerFactory emf);

  /**
   * Name of the top level package to look for
   * <ul>
   * <li>Enumeration Types
   * <li>Java class based Functions
   * </ul>
   * @param packageName
   */
  public Builder setTypePackage(final String... rootPackages);

  public Builder setRequestMappingPath(final String mappingPath);

  public Builder setMetadataPostProcessor(final JPAEdmMetadataPostProcessor metadataPostProcessor);

}
