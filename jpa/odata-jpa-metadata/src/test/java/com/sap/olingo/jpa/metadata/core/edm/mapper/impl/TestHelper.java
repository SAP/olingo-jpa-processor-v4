package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRights;
import com.sap.olingo.jpa.processor.core.testmodel.UserType;

public class TestHelper {
  private final Metamodel jpaMetamodel;
  public final IntermediateSchema schema;
  final IntermediateAnnotationInformation annotationInfo;

  public TestHelper(final Metamodel metamodel, final String namespace) throws ODataJPAModelException {
    this(metamodel, namespace, new ArrayList<>());
  }

  public TestHelper(final Metamodel metamodel, final String namespace,
      final List<AnnotationProvider> annotationProviderList) throws ODataJPAModelException {

    final Reflections reflections = mock(Reflections.class);
    when(reflections.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(
        ABCClassification.class, AccessRights.class, UserType.class)));
    annotationInfo = new IntermediateAnnotationInformation(annotationProviderList, mock(IntermediateReferences.class));

    this.jpaMetamodel = metamodel;
    this.schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(namespace), jpaMetamodel, reflections,
        annotationInfo);
  }

  public Object findAttribute(final List<? extends JPAAttribute> attributes, final String searchItem) {
    for (final JPAAttribute attribute : attributes) {
      if (attribute.getExternalName().equals(searchItem))
        return attribute;
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

  @SuppressWarnings("unchecked")
  public <T> EmbeddableType<T> getComplexType(final String typeName) {
    for (final EmbeddableType<?> embeddableType : jpaMetamodel.getEmbeddables()) {
      if (embeddableType.getJavaType().getSimpleName().equals(typeName)) {
        return (EmbeddableType<T>) embeddableType;
      }
    }
    return null;
  }

  public <T> EmbeddableType<T> getComplexType(final Class<T> clazz) {
    try {
      return jpaMetamodel.embeddable(clazz);
    } catch (final IllegalArgumentException e) {
      return null;
    }
  }

  public Attribute<?, ?> getDeclaredAttribute(final ManagedType<?> et, final String attributeName) {
    for (final Attribute<?, ?> attribute : et.getDeclaredAttributes()) {
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

  public <T> EmbeddableType<T> getEmbeddableType(final Class<T> clazz) {
    try {
      return jpaMetamodel.embeddable(clazz);
    } catch (final IllegalArgumentException e) {
      return null;
    }
  }

  public <T> EntityType<T> getEntityType(final Class<T> clazz) {
    try {
      return jpaMetamodel.entity(clazz);
    } catch (final IllegalArgumentException e) {
      return null;
    }
  }

  public EdmFunction getStoredProcedure(final EntityType<?> jpaEntityType, final String string) {
    if (jpaEntityType.getJavaType() instanceof AnnotatedElement) {
      final EdmFunctions jpaStoredProcedureList = jpaEntityType.getJavaType()
          .getAnnotation(EdmFunctions.class);
      if (jpaStoredProcedureList != null) {
        for (final EdmFunction jpaStoredProcedure : jpaStoredProcedureList.value()) {
          if (jpaStoredProcedure.name().equals(string)) return jpaStoredProcedure;
        }
      }
    }
    return null;
  }

}