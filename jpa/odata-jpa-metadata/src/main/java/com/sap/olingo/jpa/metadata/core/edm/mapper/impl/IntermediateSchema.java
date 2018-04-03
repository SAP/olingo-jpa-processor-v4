package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.reflections.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEnumerationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * <p>For details about Schema metadata see:
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397946"
 * >OData Version 4.0 Part 3 - 5 Schema </a>
 * @author Oliver Grande
 *
 */
final class IntermediateSchema extends IntermediateModelElement {
  private final Metamodel jpaMetamodel;
  private final Map<String, IntermediateComplexType> complexTypeListInternalKey;
  private final Map<String, IntermediateEntityType> entityTypeListInternalKey;
  private final Map<String, IntermediateFunction> functionListInternalKey;
  private final Map<String, IntermediateJavaAction> actionListInternalKey;
  private final Map<String, IntermediateEnumerationType> enumTypeListInternalKey;
  private IntermediateEntityContainer container;
  private final Reflections reflections;
  private CsdlSchema edmSchema;

  IntermediateSchema(final JPAEdmNameBuilder nameBuilder, final Metamodel jpaMetamodel, final Reflections reflections)
      throws ODataJPAModelException {

    super(nameBuilder, nameBuilder.buildNamespace());
    this.reflections = reflections;
    this.jpaMetamodel = jpaMetamodel;
    this.enumTypeListInternalKey = buildEnumerationTypeList();
    this.complexTypeListInternalKey = buildComplexTypeList();
    this.entityTypeListInternalKey = buildEntityTypeList();
    this.functionListInternalKey = buildFunctionList();
    this.actionListInternalKey = buildActionList();
  }

  public IntermediateEnumerationType getEnumerationType(Class<?> enumType) {
    if (enumType.isArray())
      return this.enumTypeListInternalKey.get(enumType.getComponentType().getSimpleName());
    return this.enumTypeListInternalKey.get(enumType.getSimpleName());
  }

  public JPAEnumerationAttribute getEnumerationType(EdmEnumType type) {
    for (final Entry<String, IntermediateEnumerationType> enumeration : this.enumTypeListInternalKey.entrySet()) {
      if (enumeration.getValue().getExternalFQN().equals(type.getFullQualifiedName()))
        return enumeration.getValue();
    }
    return null;
  }

  public IntermediateEnumerationType getEnumerationType(String enumName) {
    return this.enumTypeListInternalKey.get(enumName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    edmSchema = new CsdlSchema();
    edmSchema.setNamespace(nameBuilder.buildNamespace());
    edmSchema.setEnumTypes((List<CsdlEnumType>) extractEdmModelElements(enumTypeListInternalKey));
    edmSchema.setComplexTypes((List<CsdlComplexType>) extractEdmModelElements(complexTypeListInternalKey));
    edmSchema.setEntityTypes((List<CsdlEntityType>) extractEdmModelElements(entityTypeListInternalKey));
    edmSchema.setFunctions((List<CsdlFunction>) extractEdmModelElements(functionListInternalKey));
    edmSchema.setActions((List<CsdlAction>) extractEdmModelElements(actionListInternalKey));
//  edm:Annotations
//  edm:Annotation
//  edm:Term
//  edm:TypeDefinition
    // MUST be the last thing that is done !!!!
    if (container != null)
      edmSchema.setEntityContainer(container.getEdmItem());

  }

  JPAAction getAction(final String externalName) {
    for (final Entry<String, IntermediateJavaAction> action : actionListInternalKey.entrySet()) {
      if (action.getValue().getExternalName().equals(externalName) && !action.getValue().ignore())
        return action.getValue();
    }
    return null;
  }

  List<JPAAction> getActions() {
    final ArrayList<JPAAction> actions = new ArrayList<>();
    for (final Entry<String, IntermediateJavaAction> action : actionListInternalKey.entrySet()) {
      actions.add(action.getValue());
    }
    return actions;
  }

  IntermediateStructuredType getComplexType(final Class<?> targetClass) {
    return complexTypeListInternalKey.get(IntNameBuilder.buildStructuredTypeName(targetClass));
  }

  JPAStructuredType getComplexType(final String externalName) {
    for (final Map.Entry<String, IntermediateComplexType> complexType : complexTypeListInternalKey.entrySet()) {
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

  IntermediateStructuredType getEntityType(final Class<?> targetClass) {
    return entityTypeListInternalKey.get(IntNameBuilder.buildStructuredTypeName(targetClass));
  }

  JPAEntityType getEntityType(final String externalName) {

    for (final Entry<String, IntermediateEntityType> et : entityTypeListInternalKey.entrySet()) {
      if (et.getValue().getExternalName().equals(externalName))
        return et.getValue();
    }
    return null;
  }

  JPAEntityType getEntityType(final String dbCatalog, final String dbSchema, final String dbTableName) {
    for (final Entry<String, IntermediateEntityType> et : entityTypeListInternalKey.entrySet()) {
      if (et.getValue().dbEquals(dbCatalog, dbSchema, dbTableName))
        return et.getValue();
    }
    return null;
  }

  List<IntermediateEntityType> getEntityTypes() {
    final List<IntermediateEntityType> entityTypes = new ArrayList<>();
    for (final Entry<String, IntermediateEntityType> et : entityTypeListInternalKey.entrySet()) {
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

  List<JPAFunction> getFunctions() {
    final ArrayList<JPAFunction> functions = new ArrayList<>();
    for (final Entry<String, IntermediateFunction> func : functionListInternalKey.entrySet()) {
      functions.add(func.getValue());
    }
    return functions;
  }

  IntermediateStructuredType getStructuredType(final PluralAttribute<?, ?, ?> jpaAttribute) {
    IntermediateStructuredType type = complexTypeListInternalKey.get(IntNameBuilder.buildStructuredTypeName(jpaAttribute
        .getElementType().getJavaType()));
    if (type == null)
      type = entityTypeListInternalKey.get(IntNameBuilder.buildStructuredTypeName(jpaAttribute.getElementType()
          .getJavaType()));
    return type;
  }

  IntermediateStructuredType getStructuredType(final Attribute<?, ?> jpaAttribute) {
    IntermediateStructuredType type = complexTypeListInternalKey.get(IntNameBuilder.buildStructuredTypeName(jpaAttribute
        .getJavaType()));
    if (type == null)
      type = entityTypeListInternalKey.get(IntNameBuilder.buildStructuredTypeName(jpaAttribute.getJavaType()));
    return type;
  }

  IntermediateStructuredType getStructuredType(final Class<?> targetClass) {
    IntermediateStructuredType type = entityTypeListInternalKey
        .get(IntNameBuilder.buildStructuredTypeName(targetClass));
    if (type == null)
      type = complexTypeListInternalKey.get(IntNameBuilder.buildStructuredTypeName(targetClass));
    return type;
  }

  void setContainer(final IntermediateEntityContainer container) {
    this.container = container;
  }

  private Map<String, IntermediateJavaAction> buildActionList() throws ODataJPAModelException {
    final HashMap<String, IntermediateJavaAction> actionList = new HashMap<>();
    final IntermediateActionFactory factory = new IntermediateActionFactory();
    actionList.putAll(factory.create(nameBuilder, reflections, this));
    return actionList;
  }

  private Map<String, IntermediateComplexType> buildComplexTypeList() throws ODataJPAModelException {
    final HashMap<String, IntermediateComplexType> ctList = new HashMap<>();

    for (final EmbeddableType<?> embeddable : this.jpaMetamodel.getEmbeddables()) {
      final IntermediateComplexType ct = new IntermediateComplexType(nameBuilder, embeddable, this);
      ctList.put(ct.internalName, ct);
    }
    return ctList;
  }

  private Map<String, IntermediateEntityType> buildEntityTypeList() {
    final HashMap<String, IntermediateEntityType> etList = new HashMap<>();

    for (final EntityType<?> entity : this.jpaMetamodel.getEntities()) {
      final IntermediateEntityType et = new IntermediateEntityType(nameBuilder, entity, this);
      etList.put(et.internalName, et);
    }
    return etList;
  }

  private <T extends Enum<?>> Map<String, IntermediateEnumerationType> buildEnumerationTypeList() {
    final HashMap<String, IntermediateEnumerationType> enumList = new HashMap<>();
    if (reflections != null) {
      for (Class<?> enumeration : reflections.getTypesAnnotatedWith(EdmEnumeration.class)) {
        if (enumeration.isEnum()) {
          @SuppressWarnings("unchecked")
          final IntermediateEnumerationType e = new IntermediateEnumerationType(nameBuilder, (Class<T>) enumeration);
          enumList.put(e.getInternalName(), e);
        }
      }
    }
    return enumList;
  }

  private Map<String, IntermediateFunction> buildFunctionList() throws ODataJPAModelException {
    final HashMap<String, IntermediateFunction> funcList = new HashMap<>();
    // 1. Option: Create Function from Entity Annotations
    final IntermediateFunctionFactory factory = new IntermediateFunctionFactory();
    for (final EntityType<?> entity : this.jpaMetamodel.getEntities()) {

      funcList.putAll(factory.create(nameBuilder, entity, this));
    }
    // 2. Option: Create Function from Java Classes
    funcList.putAll(factory.create(nameBuilder, reflections, this));
    return funcList;
  }
}
