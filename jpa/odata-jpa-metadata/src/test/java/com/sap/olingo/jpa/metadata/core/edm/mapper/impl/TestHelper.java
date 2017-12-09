package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.reflections.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassifiaction;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRights;

public class TestHelper {
  final private Metamodel jpaMetamodel;
  final public IntermediateSchema schema;

  public TestHelper(final Metamodel metamodel, final String namespace) throws ODataJPAModelException {
    final Reflections r = mock(Reflections.class);
    when(r.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(new Class<?>[] {
        ABCClassifiaction.class, AccessRights.class })));

    this.jpaMetamodel = metamodel;
    this.schema = new IntermediateSchema(new JPAEdmNameBuilder(namespace), jpaMetamodel, r);
  }

  public EntityType<?> getEntityType(final String typeName) {
    for (final EntityType<?> entityType : jpaMetamodel.getEntities()) {
      if (entityType.getJavaType().getSimpleName().equals(typeName)) {
        return entityType;
      }
    }
    return null;
  }

  public EmbeddableType<?> getComplexType(final String typeName) {
    for (final EmbeddableType<?> embeddableType : jpaMetamodel.getEmbeddables()) {
      if (embeddableType.getJavaType().getSimpleName().equals(typeName)) {
        return embeddableType;
      }
    }
    return null;
  }

  public EdmFunction getStoredProcedure(EntityType<?> jpaEntityType, String string) {
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

  public PluralAttribute<?, ?, ?> getCollectionAttribute(final ManagedType<?> et, final String attributeName) {
    for (final PluralAttribute<?, ?, ?> attribute : et.getPluralAttributes()) {
      if (attribute.getName().equals(attributeName))
        return attribute;
    }
    return null;
  }

  public EmbeddableType<?> getEmbeddedableType(String typeName) {
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

  public Object findAttribute(final List<? extends JPAAttribute> attributes, final String searchItem) {
    for (final JPAAttribute attribute : attributes) {
      if (attribute.getExternalName().equals(searchItem))
        return attribute;
    }
    return null;
  }

}