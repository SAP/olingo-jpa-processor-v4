package com.sap.olingo.jpa.processor.core.api;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.ex.ODataException;

import com.sap.olingo.jpa.metadata.api.JPAApiVersion;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;

class JPAODataApiVersion implements JPAODataApiVersionAccess {
  private static final Log LOGGER = LogFactory.getLog(JPAODataApiVersion.class);
  private final String id;
  private final JPAEdmProvider edmProvider;
  private final EntityManagerFactory emf;
  private String mappingPath;
  private final Optional<Class<? extends EntityManagerFactory>> wrapperClass;

  JPAODataApiVersion(final JPAApiVersion version, final JPAEdmNameBuilder nameBuilder,
      final List<AnnotationProvider> annotationProviders, final ProcessorSqlPatternProvider sqlPattern)
      throws ODataException {

    this.id = version.getId();
    this.wrapperClass = determineWrapperClass();
    this.edmProvider = createEdmProvider(version, nameBuilder, annotationProviders);
    this.emf = createEmfWrapper(version.getEntityManagerFactory(), edmProvider, sqlPattern);
    this.mappingPath = version.getRequestMappingPath();
  }

  JPAODataApiVersion(final String id, final JPAEdmProvider edmProvider, final EntityManagerFactory emf) {
    this.id = id;
    this.edmProvider = edmProvider;
    this.emf = emf;
    this.wrapperClass = determineWrapperClass();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public JPAEdmProvider getEdmProvider() {
    return edmProvider;
  }

  @Override
  public EntityManagerFactory getEntityManagerFactory() {
    return emf;
  }

  @Override
  public String getMappingPath() {
    return mappingPath;
  }

  private JPAEdmProvider createEdmProvider(final JPAApiVersion version, final JPAEdmNameBuilder nameBuilder,
      final List<AnnotationProvider> annotationProviders) throws ODataException {
    return new JPAEdmProvider(version.getEntityManagerFactory().getMetamodel(), version.getMetadataPostProcessor(),
        version.getPackageNames(), nameBuilder, annotationProviders);
  }

  @SuppressWarnings("unchecked")
  private Optional<Class<? extends EntityManagerFactory>> determineWrapperClass() {
    try {
      return Optional.ofNullable((Class<? extends EntityManagerFactory>) Class
          .forName("com.sap.olingo.jpa.processor.cb.api.EntityManagerFactoryWrapper"));
    } catch (final ClassNotFoundException e) {
      LOGGER.trace("No Criteria Builder Extension found: use provided Entity Manager Factory");
      return Optional.empty();
    }
  }

  private EntityManagerFactory createEmfWrapper(final EntityManagerFactory factory, final JPAEdmProvider jpaEdm,
      final ProcessorSqlPatternProvider sqlPattern) {
    try {
      if (wrapperClass.isPresent()) {
        LOGGER.trace("Criteria Builder Extension found. It will be used");
        return wrapperClass.get().getConstructor(EntityManagerFactory.class,
            JPAServiceDocument.class, ProcessorSqlPatternProvider.class)
            .newInstance(factory, jpaEdm.getServiceDocument(), sqlPattern);
      } else
        return factory;
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      LOGGER.debug("Exception thrown while trying to create instance of emf wrapper", e);
    }
    return factory;
  }

}
