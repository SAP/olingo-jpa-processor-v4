package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.Metamodel;

import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edmx.EdmxReference;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/schemas/edmx.xsd
 * A Service Document can contain of multiple schemas, but only of
 * one Entity Container. This container is assigned to one of the
 * schemas.
 * http://services.odata.org/V4/Northwind/Northwind.svc/$metadata
 */
public class ServiceDocument {
  private final Metamodel jpaMetamodel;
  private final JPAEdmNameBuilder nameBuilder;
  private final IntermediateEntityContainer container;
  private final Map<String, IntermediateSchema> schemaListInternalKey;
  private final IntermediateReferences references;
  private final JPAEdmMetadataPostProcessor pP;

  public ServiceDocument(final String namespace, final Metamodel jpaMetamodel,
      final JPAEdmMetadataPostProcessor postProcessor) throws ODataJPAModelException {

    this.pP = postProcessor != null ? postProcessor : new DefaultEdmPostProcessor();
    IntermediateModelElement.setPostProcessor(pP);

    this.references = new IntermediateReferences();
    pP.provideReferences(this.references);
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

  public List<CsdlSchema> getAllSchemas() throws ODataJPAModelException {
    List<CsdlSchema> allSchemas = getEdmSchemas();
    allSchemas.addAll(references.getSchemas());
    return allSchemas;
  }

  /**
   * 
   * @param edmType
   * @return
   * @throws ODataJPAModelException
   */
  public JPAEntityType getEntity(final EdmType edmType) throws ODataJPAModelException {
    final IntermediateSchema schema = schemaListInternalKey.get(edmType.getNamespace());
    if (schema != null)
      return schema.getEntityType(edmType.getName());
    return null;
  }

  public JPAEntityType getEntity(final FullQualifiedName typeName) {
    final IntermediateSchema schema = schemaListInternalKey.get(typeName.getNamespace());
    if (schema != null)
      return schema.getEntityType(typeName.getName());
    return null;
  }

  public JPAEntityType getEntity(final String edmEntitySetName) throws ODataJPAModelException {
    final IntermediateEntitySet entitySet = container.getEntitySet(edmEntitySetName);
    return entitySet != null ? entitySet.getEntityType() : null;
  }

  public JPAFunction getFunction(final EdmFunction function) {
    final IntermediateSchema schema = schemaListInternalKey.get(function.getNamespace());
    if (schema != null)
      return schema.getFunction(function.getName());
    return null;
  }

  private Map<String, IntermediateSchema> buildIntermediateSchemas() throws ODataJPAModelException {
    final Map<String, IntermediateSchema> schemaList = new HashMap<String, IntermediateSchema>();
    final IntermediateSchema schema = new IntermediateSchema(nameBuilder, jpaMetamodel);
    schemaList.put(schema.internalName, schema);
    return schemaList;
  }

  private List<CsdlSchema> extractEdmSchemas() throws ODataJPAModelException {
    final List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
    for (final String internalName : schemaListInternalKey.keySet()) {
      schemas.add(schemaListInternalKey.get(internalName).getEdmItem());
    }
    return schemas;
  }

  private void setContainer() {
    for (final String externalName : schemaListInternalKey.keySet()) {
      schemaListInternalKey.get(externalName).setContainer(container);
      return;
    }
  }

  public JPAElement getEntitySet(final JPAEntityType entityType) throws ODataJPAModelException {
    return container.getEntitySet(entityType);
  }

  public List<EdmxReference> getReferences() {
    return references.getEdmReferences();
  }

  public CsdlTerm getTerm(final FullQualifiedName termName) {
    return this.references.getTerm(termName);
  }
}
