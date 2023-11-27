package com.sap.olingo.jpa.processor.core.util;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;

import org.apache.olingo.commons.api.ex.ODataException;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class TestHelper {
  final private Metamodel jpaMetamodel;
  final public JPAServiceDocument sd;
  final public JPAEdmProvider edmProvider;

  public TestHelper(final EntityManagerFactory emf, final String namespace) throws ODataException {
    this.jpaMetamodel = emf.getMetamodel();
    edmProvider = new JPAEdmProvider(namespace, emf, null, TestBase.enumPackages);
    sd = edmProvider.getServiceDocument();
    sd.getEdmEntityContainer();
  }

  public EntityType<?> getEntityType(final Class<?> clazz) {
    for (final EntityType<?> entityType : jpaMetamodel.getEntities()) {
      if (entityType.getJavaType() == clazz) {
        return entityType;
      }
    }
    return null;
  }

  public JPAEntityType getJPAEntityType(final String entitySetName) throws ODataJPAModelException {
    return sd.getEntity(entitySetName);
  }

  public JPAEntityType getJPAEntityType(final Class<?> clazz) throws ODataJPAModelException {
    return sd.getEntity(clazz);
  }

  public JPAAssociationPath getJPAAssociationPath(final String entitySetName, final String attributeExternalName)
      throws ODataJPAModelException {
    final JPAEntityType jpaEntity = sd.getEntity(entitySetName);
    return jpaEntity.getAssociationPath(attributeExternalName);
  }

  public JPAAssociationPath getJPAAssociationPath(final Class<?> clazz, final String attributeExternalName)
      throws ODataJPAModelException {
    final JPAEntityType jpaEntity = sd.getEntity(clazz);
    return jpaEntity.getAssociationPath(attributeExternalName);
  }

  public JPAAttribute getJPAAssociation(final String entitySetName, final String attributeInternalName)
      throws ODataJPAModelException {
    final JPAEntityType jpaEntity = sd.getEntity(entitySetName);
    return jpaEntity.getAssociation(attributeInternalName);
  }

  public Optional<JPAAttribute> getJPAAttribute(final String entitySetName, final String attributeInternalName)
      throws ODataJPAModelException {
    final JPAEntityType jpaEntity = sd.getEntity(entitySetName);
    return jpaEntity.getAttribute(attributeInternalName);
  }

  public EdmFunction getStoredProcedure(final EntityType<?> jpaEntityType, final String string) {
    if (jpaEntityType.getJavaType() instanceof AnnotatedElement) {
      final EdmFunctions jpaStoredProcedureList = ((AnnotatedElement) jpaEntityType.getJavaType())
          .getAnnotation(EdmFunctions.class);
      if (jpaStoredProcedureList != null) {
        for (final EdmFunction jpaStoredProcedure : jpaStoredProcedureList.value()) {
          if (jpaStoredProcedure.name().equals(string)) return jpaStoredProcedure;
        }
      }
    }
    return null;
  }

  public Attribute<?, ?> getAttribute(final ManagedType<?> et, final String attributeName) {
    for (final SingularAttribute<?, ?> attribute : et.getSingularAttributes()) {
      if (attribute.getName().equals(attributeName))
        return attribute;
    }
    return null;
  }

  public EmbeddableType<?> getEmbeddableType(final String typeName) {
    for (final EmbeddableType<?> embeddableType : jpaMetamodel.getEmbeddables()) {
      if (embeddableType.getJavaType().getSimpleName().equals(typeName)) {
        return embeddableType;
      }
    }
    return null;
  }

  public Attribute<?, ?> getDeclaredAttribute(final ManagedType<?> et, final String attributeName) {
    for (final Attribute<?, ?> attribute : et.getDeclaredAttributes()) {
      if (attribute.getName().equals(attributeName))
        return attribute;
    }
    return null;
  }

}