package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * <p>For details about Schema metadata see:
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397946"
 * >OData Version 4.0 Part 3 - 5 Schema </a>
 * @author Oliver Grande
 *
 */
class IntermediateSchema extends IntermediateModelElement {
  final private Metamodel jpaMetamodel;
  final private HashMap<String, IntermediateComplexType> complexTypeListInternalKey;
  final private HashMap<String, IntermediateEntityType> entityTypeListInternalKey;
  final private HashMap<String, IntermediateFunction> functionListInternalKey;
  private IntermediateEntityContainer container;
  private CsdlSchema edmSchema = null;

  IntermediateSchema(JPAEdmNameBuilder nameBuilder, Metamodel jpaMetamodel) throws ODataJPAModelException {
    super(nameBuilder, nameBuilder.buildNamespace());
    this.jpaMetamodel = jpaMetamodel;
    this.complexTypeListInternalKey = buildComplexTypeList();
    this.entityTypeListInternalKey = buildEntityTypeList();
    this.functionListInternalKey = buildFunctionList();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    edmSchema = new CsdlSchema();
    edmSchema.setNamespace(nameBuilder.buildNamespace());
    edmSchema.setComplexTypes((List<CsdlComplexType>) extractEdmModelElements(complexTypeListInternalKey));
    edmSchema.setEntityTypes((List<CsdlEntityType>) extractEdmModelElements(entityTypeListInternalKey));
    edmSchema.setFunctions((List<CsdlFunction>) extractEdmModelElements(functionListInternalKey));

//  edm:Action
//  edm:Annotations
//  edm:Annotation
//  edm:EnumType --> Annotation @Enummerated
//  edm:Term
//  edm:TypeDefinition
    // MUST be the last thing that is done !!!!
    if (container != null)
      edmSchema.setEntityContainer(container.getEdmItem());

  }

  @Override
      CsdlSchema getEdmItem() throws ODataJPAModelException {
    if (edmSchema == null) lazyBuildEdmItem();
    return edmSchema;
  }

  IntermediateStructuredType getStructuredType(Attribute<?, ?> jpaAttribute) {
    IntermediateStructuredType type = complexTypeListInternalKey.get(intNameBuilder.buildStructuredTypeName(jpaAttribute
        .getJavaType()));
    if (type == null)
      type = entityTypeListInternalKey.get(intNameBuilder.buildStructuredTypeName(jpaAttribute.getJavaType()));
    return type;
  }

  IntermediateStructuredType getEntityType(Class<?> targetClass) {
    return entityTypeListInternalKey.get(intNameBuilder.buildStructuredTypeName(targetClass));
  }

  JPAEntityType getEntityType(String externalName) {
    for (String internalName : entityTypeListInternalKey.keySet()) {
      if (entityTypeListInternalKey.get(internalName).getExternalName().equals(externalName))
        return entityTypeListInternalKey.get(internalName);
    }
    return null;
  }

  List<IntermediateEntityType> getEntityTypes() {
    List<IntermediateEntityType> entityTypes = new ArrayList<IntermediateEntityType>();
    for (String internalName : entityTypeListInternalKey.keySet()) {
      entityTypes.add(entityTypeListInternalKey.get(internalName));
    }
    return entityTypes;
  }

  JPAFunction getFunction(String externalName) {
    for (String internalName : functionListInternalKey.keySet()) {
      if (functionListInternalKey.get(internalName).getExternalName().equals(externalName))
        return functionListInternalKey.get(internalName);
    }
    return null;
  }

  void setContainer(IntermediateEntityContainer container) {
    this.container = container;
  }

  private HashMap<String, IntermediateComplexType> buildComplexTypeList() throws ODataJPAModelException {
    HashMap<String, IntermediateComplexType> ctList = new HashMap<String, IntermediateComplexType>();

    for (EmbeddableType<?> embeddable : this.jpaMetamodel.getEmbeddables()) {
      IntermediateComplexType ct = new IntermediateComplexType(nameBuilder, embeddable, this);
      ctList.put(ct.internalName, ct);
    }
    return ctList;
  }

  private HashMap<String, IntermediateEntityType> buildEntityTypeList() throws ODataJPAModelException {
    HashMap<String, IntermediateEntityType> etList = new HashMap<String, IntermediateEntityType>();

    for (EntityType<?> entity : this.jpaMetamodel.getEntities()) {
      IntermediateEntityType et = new IntermediateEntityType(nameBuilder, entity, this);
      etList.put(et.internalName, et);
    }
    return etList;
  }

  private HashMap<String, IntermediateFunction> buildFunctionList() throws ODataJPAModelException {
    HashMap<String, IntermediateFunction> funcList = new HashMap<String, IntermediateFunction>();
    // 1. Option: Create Function from Entity Annotations
    IntermediateFunctionFactory factory = new IntermediateFunctionFactory();
    for (EntityType<?> entity : this.jpaMetamodel.getEntities()) {

      funcList.putAll(factory.create(nameBuilder, entity, this));
    }
    return funcList;
  }
}
