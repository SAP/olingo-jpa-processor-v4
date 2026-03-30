package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.Objects;

import javax.annotation.Nonnull;

import jakarta.persistence.EntityManagerFactory;

import com.sap.olingo.jpa.metadata.api.JPAApiVersion;
import com.sap.olingo.jpa.metadata.api.JPAApiVersionBuilder;
import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class JPAApiVersionProvider implements JPAApiVersion {

  private final String id;
  private final EntityManagerFactory emf;
  private final String[] packageNames;
  private final String requestMappingPath;
  private final JPAEdmMetadataPostProcessor metadataPostProcessor;
  private final Boolean hideRestrictedProperties;

  private JPAApiVersionProvider(final Builder builder) {
    id = builder.id;
    emf = builder.emf;
    packageNames = builder.packageNames;
    requestMappingPath = builder.mappingPath;
    metadataPostProcessor = builder.metadataPostProcessor;
    hideRestrictedProperties = builder.hideRestrictedProperties;
  }

  public static Builder with() {
    return new Builder();
  }

  public static class Builder implements JPAApiVersionBuilder {
    private String id;
    private EntityManagerFactory emf;
    private String[] packageNames;
    private String mappingPath;
    private JPAEdmMetadataPostProcessor metadataPostProcessor;
    private Boolean hideRestrictedProperties;

    /**
     *
     * @param id Used to identify the version. Value is required.
     * @return
     */
    @Override
    public Builder setId(@Nonnull final String id) {
      Objects.requireNonNull(id);
      this.id = id;
      return this;
    }

    /**
     * Set an externally created entity manager factory.<br>
     * This is necessary e.g. in case a spring based service shall run without a <code>persistance.xml</code>.
     * @param emf
     * @return
     */
    @Override
    public Builder setEntityManagerFactory(@Nonnull final EntityManagerFactory emf) {
      Objects.requireNonNull(emf);
      this.emf = emf;
      return this;
    }

    /**
     * Name of the top level package to look for
     * <ul>
     * <li>Enumeration Types
     * <li>Java class based Functions
     * </ul>
     * @param packageName
     */
    @Override
    public Builder setTypePackage(final String... rootPackages) {
      this.packageNames = rootPackages;
      return this;
    }

    @Override
    public Builder setRequestMappingPath(final String mappingPath) {
      this.mappingPath = mappingPath;
      return this;
    }

    @Override
    public Builder setMetadataPostProcessor(final JPAEdmMetadataPostProcessor metadataPostProcessor) {
      this.metadataPostProcessor = metadataPostProcessor;
      return this;
    }

    @Override
    public Builder setHideRestrictedProperties(final boolean hideRestrictedProperties) {
      this.hideRestrictedProperties = hideRestrictedProperties;
      return this;
    }

    public JPAApiVersion build() throws ODataJPAModelException {
      if (id == null || id.isBlank())
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.VERSION_ID_MISSING);
      if (emf == null)
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.VERSION_EMF_MISSING);
      if (packageNames == null)
        packageNames = new String[0];
      if (metadataPostProcessor == null)
        metadataPostProcessor = new DefaultEdmPostProcessor();
      if (hideRestrictedProperties == null)
        hideRestrictedProperties = false;
      return new JPAApiVersionProvider(this);
    }
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public EntityManagerFactory getEntityManagerFactory() {
    return emf;
  }

  @Override
  public String[] getPackageNames() {
    return packageNames;
  }

  @Override
  public String getRequestMappingPath() {
    return requestMappingPath;
  }

  @Override
  public JPAEdmMetadataPostProcessor getMetadataPostProcessor() {
    return metadataPostProcessor;
  }

  @Override
  public boolean hideRestrictedProperties() {
    return hideRestrictedProperties;
  }

}
