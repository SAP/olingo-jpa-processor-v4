package org.apache.olingo.jpa.processor.core.query;

import java.lang.reflect.AnnotatedElement;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;

public class TestHelper {
  final private Metamodel jpaMetamodel;
  final public ServicDocument sd;

  public TestHelper(Metamodel metamodel, String namespace) throws ODataJPAModelException {
    this.jpaMetamodel = metamodel;
    sd = new ServicDocument(namespace, metamodel, null);
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