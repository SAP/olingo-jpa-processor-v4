package com.sap.olingo.jpa.processor.core.api;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

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

  JPAODataApiVersion(final JPAApiVersion version, final JPAEdmNameBuilder nameBuilder,
      final List<AnnotationProvider> annotationProviders, final ProcessorSqlPatternProvider sqlPattern)
      throws ODataException {

    this.id = version.getId();
    this.edmProvider = createEdmProvider(version, nameBuilder, annotationProviders);
    this.emf = createEmfWrapper(version.getEntityManagerFactory(), edmProvider, sqlPattern);
    this.mappingPath = version.getRequestMappingPath();
  }

  JPAODataApiVersion(final String id, final JPAEdmProvider edmProvider, final EntityManagerFactory emf) {
    this.id = id;
    this.edmProvider = edmProvider;
    this.emf = emf;
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
  private EntityManagerFactory createEmfWrapper(final EntityManagerFactory factory, final JPAEdmProvider jpaEdm,
      final ProcessorSqlPatternProvider sqlPattern) {
    try {
      final Class<? extends EntityManagerFactory> wrapperClass = (Class<? extends EntityManagerFactory>) Class
          .forName("com.sap.olingo.jpa.processor.cb.api.EntityManagerFactoryWrapper");
      if (wrapperClass != null) {
        LOGGER.trace("Criteria Builder Extension found. It will be used");
        return wrapperClass.getConstructor(EntityManagerFactory.class,
            JPAServiceDocument.class, ProcessorSqlPatternProvider.class)
            .newInstance(factory, jpaEdm.getServiceDocument(), sqlPattern);
      } else
        return factory;
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      LOGGER.debug("Exception thrown while trying to create instance of emf wrapper", e);
    } catch (final ClassNotFoundException e) {
      // No Criteria Extension: everything is fine
      LOGGER.trace("No Criteria Builder Extension found: use provided Entity Manager Factory");
    }
    return factory;
  }

}
