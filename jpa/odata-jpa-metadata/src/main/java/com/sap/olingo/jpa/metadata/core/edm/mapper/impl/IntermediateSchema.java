package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEnumerationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * <p>
 * For details about Schema metadata see:
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397946"
 * >OData Version 4.0 Part 3 - 5 Schema </a>
 * @author Oliver Grande
 *
 */
final class IntermediateSchema extends IntermediateModelElement {
  private final Metamodel jpaMetamodel;
  private final Map<String, IntermediateComplexType<?>> complexTypeListInternalKey;
  private final Map<String, IntermediateEntityType<?>> entityTypeListInternalKey;
  private final Map<String, IntermediateFunction> functionListInternalKey;
  private final Map<ODataActionKey, IntermediateJavaAction> actionListByKey;
  private final Map<String, IntermediateEnumerationType> enumTypeListInternalKey;
  private IntermediateEntityContainer container;
  private final Reflections reflections;
  private CsdlSchema edmSchema;

  IntermediateSchema(final JPAEdmNameBuilder nameBuilder, final Metamodel jpaMetamodel, final Reflections reflections,
      final IntermediateAnnotationInformation annotationInfo) throws ODataJPAModelException {

    super(nameBuilder, nameBuilder.getNamespace(), annotationInfo);
    this.jpaMetamodel = jpaMetamodel;
    this.reflections = reflections;
    this.enumTypeListInternalKey = buildEnumerationTypeList();
    this.complexTypeListInternalKey = buildComplexTypeList();
    this.entityTypeListInternalKey = buildEntityTypeList();
    this.functionListInternalKey = buildFunctionList();
    this.actionListByKey = buildActionList();
  }

  public IntermediateEnumerationType getEnumerationType(final Class<?> enumType) {
    if (enumType.isArray())
      return this.enumTypeListInternalKey.get(enumType.getComponentType().getSimpleName());
    return this.enumTypeListInternalKey.get(enumType.getSimpleName());
  }

  public JPAEnumerationAttribute getEnumerationType(final EdmEnumType type) {
    for (final Entry<String, IntermediateEnumerationType> enumeration : this.enumTypeListInternalKey.entrySet()) {
      if (enumeration.getValue().getExternalFQN().equals(type.getFullQualifiedName()))
        return enumeration.getValue();
    }
    return null;
  }

  public IntermediateEnumerationType getEnumerationType(final String externalName) {
    for (final Entry<String, IntermediateEnumerationType> enumeration : this.enumTypeListInternalKey.entrySet()) {
      if (enumeration.getValue().getExternalName().equals(externalName))
        return enumeration.getValue();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    edmSchema = new CsdlSchema();
    edmSchema.setNamespace(nameBuilder.getNamespace());
    edmSchema.setEnumTypes(extractEdmModelElements(enumTypeListInternalKey));
    edmSchema.setComplexTypes(extractEdmModelElements(complexTypeListInternalKey));
    edmSchema.setEntityTypes(extractEdmModelElements(entityTypeListInternalKey));
    edmSchema.setFunctions(extractEdmModelElements(functionListInternalKey));
    edmSchema.setActions(extractEdmModelElements(actionListByKey));
//  edm:Annotations
//  edm:Annotation
//  edm:Term
//  edm:TypeDefinition
    // MUST be the last thing that is done !!!!
    if (container != null)
      edmSchema.setEntityContainer(container.getEdmItem());

  }

  @CheckForNull
  JPAAction getAction(final String externalName, final FullQualifiedName actionFqn) {
    return actionListByKey.get(new ODataActionKey(externalName, actionFqn));
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  <S extends JPAAction> List<S> getActions() {
    return Collections.unmodifiableList(new ArrayList<>((Collection<S>) actionListByKey.values()));
  }

  @SuppressWarnings("unchecked")
  <T> IntermediateStructuredType<T> getComplexType(final Class<T> targetClass) {
    return (IntermediateStructuredType<T>) complexTypeListInternalKey.get(InternalNameBuilder.buildStructuredTypeName(
        targetClass));
  }

  JPAStructuredType getComplexType(final String externalName) {
    for (final Map.Entry<String, IntermediateComplexType<?>> complexType : complexTypeListInternalKey.entrySet()) {
      if (complexType.getValue().getExternalName().equals(externalName))
        return complexType.getValue();
    }
    return null;
  }

  @Override
  CsdlSchema getEdmItem() throws ODataJPAModelException {
    if (edmSchema == null)
      lazyBuildEdmItem();
    return edmSchema;
  }

  @SuppressWarnings("unchecked")
  @CheckForNull
  <T> IntermediateStructuredType<T> getEntityType(final Class<T> targetClass) {
    return (IntermediateStructuredType<T>) entityTypeListInternalKey.get(InternalNameBuilder.buildStructuredTypeName(
        targetClass));
  }

  JPAEntityType getEntityType(final String externalName) {

    for (final Entry<String, IntermediateEntityType<?>> et : entityTypeListInternalKey.entrySet()) {
      if (et.getValue().getExternalName().equals(externalName))
        return et.getValue();
    }
    return null;
  }

  JPAEntityType getEntityType(final String dbCatalog, final String dbSchema, final String dbTableName) {
    for (final Entry<String, IntermediateEntityType<?>> et : entityTypeListInternalKey.entrySet()) {
      if (et.getValue().dbEquals(dbCatalog, dbSchema, dbTableName))
        return et.getValue();
    }
    return null;
  }

  List<IntermediateEntityType<?>> getEntityTypes() {
    final List<IntermediateEntityType<?>> entityTypes = new ArrayList<>();
    for (final Entry<String, IntermediateEntityType<?>> et : entityTypeListInternalKey.entrySet()) {
      entityTypes.add(et.getValue());
    }
    return entityTypes;
  }

  JPAFunction getFunction(final String externalName) {
    for (final Entry<String, IntermediateFunction> func : functionListInternalKey.entrySet()) {
      if (func.getValue().getExternalName().equals(externalName)
          && !func.getValue().ignore())
        return func.getValue();
    }
    return null;

  }

  @Nonnull
  List<JPAFunction> getFunctions() {
    final ArrayList<JPAFunction> functions = new ArrayList<>();
    for (final Entry<String, IntermediateFunction> func : functionListInternalKey.entrySet()) {
      functions.add(func.getValue());
    }
    return functions;
  }

  @SuppressWarnings("unchecked")
  <T> IntermediateStructuredType<T> getStructuredType(final PluralAttribute<?, ?, ?> jpaAttribute) {
    IntermediateStructuredType<?> type = complexTypeListInternalKey.get(InternalNameBuilder.buildStructuredTypeName(
        jpaAttribute.getElementType().getJavaType()));
    if (type == null)
      type = entityTypeListInternalKey.get(InternalNameBuilder.buildStructuredTypeName(jpaAttribute.getElementType()
          .getJavaType()));
    return (IntermediateStructuredType<T>) type;
  }

  @SuppressWarnings("unchecked")
  <T> IntermediateStructuredType<T> getStructuredType(final Attribute<?, ?> jpaAttribute) {
    IntermediateStructuredType<?> type = complexTypeListInternalKey.get(InternalNameBuilder.buildStructuredTypeName(
        jpaAttribute.getJavaType()));
    if (type == null)
      type = entityTypeListInternalKey.get(InternalNameBuilder.buildStructuredTypeName(jpaAttribute.getJavaType()));
    return (IntermediateStructuredType<T>) type;
  }

  @SuppressWarnings("unchecked")
  <T> IntermediateStructuredType<T> getStructuredType(final Class<?> targetClass) {
    IntermediateStructuredType<?> type = entityTypeListInternalKey
        .get(InternalNameBuilder.buildStructuredTypeName(targetClass));
    if (type == null)
      type = complexTypeListInternalKey.get(InternalNameBuilder.buildStructuredTypeName(targetClass));
    return (IntermediateStructuredType<T>) type;
  }

  void setContainer(final IntermediateEntityContainer container) {
    this.container = container;
  }

  private Map<ODataActionKey, IntermediateJavaAction> buildActionList() throws ODataJPAModelException {
    final HashMap<ODataActionKey, IntermediateJavaAction> actionList = new HashMap<>();
    final IntermediateActionFactory factory = new IntermediateActionFactory();
    actionList.putAll(factory.create(nameBuilder, reflections, this));
    return actionList;
  }

  private Map<String, IntermediateComplexType<?>> buildComplexTypeList() {
    final HashMap<String, IntermediateComplexType<?>> ctList = new HashMap<>();

    for (final EmbeddableType<?> embeddable : this.jpaMetamodel.getEmbeddables()) {
      final IntermediateComplexType<?> ct = new IntermediateComplexType<>(nameBuilder, embeddable, this);
      ctList.put(ct.internalName, ct);
    }
    return ctList;
  }

  private Map<String, IntermediateEntityType<?>> buildEntityTypeList() {
    final HashMap<String, IntermediateEntityType<?>> etList = new HashMap<>();

    for (final EntityType<?> entity : this.jpaMetamodel.getEntities()) {
      final IntermediateEntityType<?> et = new IntermediateEntityType<>(nameBuilder, entity, this);
      etList.put(et.internalName, et);
    }
    return etList;
  }

  private <T extends Enum<?>> Map<String, IntermediateEnumerationType> buildEnumerationTypeList() {
    final HashMap<String, IntermediateEnumerationType> enumList = new HashMap<>();
    if (reflections != null) {
      for (final Class<?> enumeration : reflections.getTypesAnnotatedWith(EdmEnumeration.class)) {
        if (enumeration.isEnum()) {
          @SuppressWarnings("unchecked")
          final IntermediateEnumerationType type = new IntermediateEnumerationType(nameBuilder, (Class<T>) enumeration,
              getAnnotationInformation());
          enumList.put(type.getInternalName(), type);
        }
      }
    }
    return enumList;
  }

  private Map<String, IntermediateFunction> buildFunctionList() throws ODataJPAModelException {
    final HashMap<String, IntermediateFunction> functionList = new HashMap<>();
    // 1. Option: Create Function from Entity Annotations
    final IntermediateFunctionFactory<?> factory = new IntermediateFunctionFactory<>();
    for (final EntityType<?> entity : this.jpaMetamodel.getEntities()) {

      functionList.putAll(factory.create(nameBuilder, entity, this));
    }
    // 2. Option: Create Function from Java Classes
    functionList.putAll(factory.create(nameBuilder, reflections, this));
    return functionList;
  }
}
