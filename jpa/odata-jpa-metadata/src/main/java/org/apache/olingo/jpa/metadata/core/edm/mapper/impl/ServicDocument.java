package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.metamodel.Metamodel;

import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/*
 * http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/schemas/edmx.xsd
 * A Service Document can contain of multiple schemas, but only of
 * one Entity Container. This container is assigned to one of the
 * schemas.
 * http://services.odata.org/V4/Northwind/Northwind.svc/$metadata
 */
public class ServicDocument {
  final private Metamodel jpaMetamodel;
  final private JPAEdmNameBuilder nameBuilder;
  final private IntermediateEntityContainer container;
  final private HashMap<String, IntermediateSchema> schemaListInternalKey;
  // final private HashMap<String, IntermediateSchema> schemaListExternalKey;

  public ServicDocument(String namespace, Metamodel jpaMetamodel, JPAEdmMetadataPostProcessor postProcessor)
      throws ODataJPAModelException {

    IntermediateModelElement.SetPostProcessor(postProcessor != null ? postProcessor : new DefaultEdmPostProcessor());

    this.nameBuilder = new JPAEdmNameBuilder(namespace);
    this.jpaMetamodel = jpaMetamodel;
    this.schemaListInternalKey = buildIntermediateSchemas();
    this.container = new IntermediateEntityContainer(nameBuilder, schemaListInternalKey);
    setContainer();
  }

  public CsdlEntityContainer getEdmEntityContainer() throws ODataJPAModelException {
    return container.getEdmItem();
  }

  public List<CsdlSchema> getEdmSchemas() throws ODataJPAModelException {
    return extractEdmSchemas();
  }

  public JPAEntityType getEntity(EdmType edmType) throws ODataJPAModelException {
    IntermediateSchema schema = schemaListInternalKey.get(edmType.getNamespace());
    if (schema != null)
      return schema.getEntityType(edmType.getName());
    return null;
  }

  public JPAEntityType getEntity(FullQualifiedName typeName) {
    IntermediateSchema schema = schemaListInternalKey.get(typeName.getNamespace());
    if (schema != null)
      return schema.getEntityType(typeName.getName());
    return null;
  }

  public JPAEntityType getEntity(String edmEntitySetName) throws ODataJPAModelException {
    IntermediateEntitySet entitySet = container.getEntityTypeSet(edmEntitySetName);
    return entitySet != null ? entitySet.getEntityType() : null;
  }

  public JPAFunction getFunction(EdmFunction function) {
    IntermediateSchema schema = schemaListInternalKey.get(function.getNamespace());
    if (schema != null)
      return schema.getFunction(function.getName());
    return null;
  }

  private HashMap<String, IntermediateSchema> buildIntermediateSchemas() throws ODataJPAModelException {
    HashMap<String, IntermediateSchema> schemaList = new HashMap<String, IntermediateSchema>();
    IntermediateSchema schema = new IntermediateSchema(nameBuilder, jpaMetamodel);
    schemaList.put(schema.internalName, schema);
    return schemaList;
  }

  private List<CsdlSchema> extractEdmSchemas() throws ODataJPAModelException {
    List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
    for (String internalName : schemaListInternalKey.keySet()) {
      schemas.add((CsdlSchema) schemaListInternalKey.get(internalName).getEdmItem());
    }
    return schemas;
  }

  private void setContainer() {
    for (String externalName : schemaListInternalKey.keySet()) {
      schemaListInternalKey.get(externalName).setContainer(container);
      return;
    }
  }

}
