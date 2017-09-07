package com.sap.olingo.jpa.processor.core.util;

import java.lang.reflect.AnnotatedElement;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

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

  public TestHelper(EntityManagerFactory emf, String namespace) throws ODataException {
    this.jpaMetamodel = emf.getMetamodel();
    edmProvider = new JPAEdmProvider(namespace, emf, null, null);
    sd = edmProvider.getServiceDocument();
    sd.getEdmEntityContainer();
  }

  public EntityType<?> getEntityType(String typeName) {
    for (EntityType<?> entityType : jpaMetamodel.getEntities()) {
      if (entityType.getJavaType().getSimpleName().equals(typeName)) {
        return entityType;
      }
    }
    return null;
  }

  public JPAEntityType getJPAEntityType(String entitySetName) throws ODataJPAModelException {
    return sd.getEntity(entitySetName);
  }

  public JPAAssociationPath getJPAAssociationPath(String entitySetName, String attributeExtName)
      throws ODataJPAModelException {
    JPAEntityType jpaEntity = sd.getEntity(entitySetName);
    return jpaEntity.getAssociationPath(attributeExtName);
  }

  public JPAAttribute getJPAAssociation(String entitySetName, String attributeIntName) throws ODataJPAModelException {
    JPAEntityType jpaEntity = sd.getEntity(entitySetName);
    return jpaEntity.getAssociation(attributeIntName);
  }

  public JPAAttribute getJPAAttribute(String entitySetName, String attributeIntName) throws ODataJPAModelException {
    JPAEntityType jpaEntity = sd.getEntity(entitySetName);
    return jpaEntity.getAttribute(attributeIntName);
  }

  public EdmFunction getStoredProcedure(EntityType<?> jpaEntityType, String string) {
    if (jpaEntityType.getJavaType() instanceof AnnotatedElement) {
      EdmFunctions jpaStoredProcedureList = ((AnnotatedElement) jpaEntityType.getJavaType())
          .getAnnotation(EdmFunctions.class);
      if (jpaStoredProcedureList != null) {
        for (EdmFunction jpaStoredProcedure : jpaStoredProcedureList.value()) {
          if (jpaStoredProcedure.name().equals(string)) return jpaStoredProcedure;
        }
      }
    }
    return null;
  }

  public Attribute<?, ?> getAttribute(ManagedType<?> et, String attributeName) {
    for (SingularAttribute<?, ?> attribute : et.getSingularAttributes()) {
      if (attribute.getName().equals(attributeName))
        return attribute;
    }
    return null;
  }

  public EmbeddableType<?> getEmbeddedableType(String typeName) {
    for (EmbeddableType<?> embeddableType : jpaMetamodel.getEmbeddables()) {
      if (embeddableType.getJavaType().getSimpleName().equals(typeName)) {
        return embeddableType;
      }
    }
    return null;
  }

  public Attribute<?, ?> getDeclaredAttribute(ManagedType<?> et, String attributeName) {
    for (Attribute<?, ?> attribute : et.getDeclaredAttributes()) {
      if (attribute.getName().equals(attributeName))
        return attribute;
    }
    return null;
  }

}