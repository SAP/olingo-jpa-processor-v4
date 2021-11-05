package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.persistence.metamodel.Metamodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.reflections8.Reflections;
import org.reflections8.scanners.SubTypesScanner;
import org.reflections8.scanners.TypeAnnotationsScanner;
import org.reflections8.util.ConfigurationBuilder;
import org.reflections8.util.FilterBuilder;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEnumerationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/schemas/edmx.xsd
 * A Service Document can contain multiple schemas, but only
 * one Entity Container. This container is assigned to one of the schemas.<p>
 * http://services.odata.org/V4/Northwind/Northwind.svc/$metadata
 */
class IntermediateServiceDocument implements JPAServiceDocument {

  private static final Log LOGGER = LogFactory.getLog(IntermediateServiceDocument.class);
  private final Metamodel jpaMetamodel;
  private final JPAEdmNameBuilder nameBuilder;
  private final IntermediateEntityContainer container;
  private final Map<String, IntermediateSchema> schemaListInternalKey;
  private final IntermediateReferences references;
  private final JPAEdmMetadataPostProcessor pP;
  private final Reflections reflections;
  private Map<String, JPAProtectionInfo> claims;

  IntermediateServiceDocument(final String namespace, final Metamodel jpaMetamodel,
      final JPAEdmMetadataPostProcessor postProcessor, final String[] packageName) throws ODataJPAModelException {

    this(new JPADefaultEdmNameBuilder(namespace), jpaMetamodel, postProcessor, packageName);
  }

  /**
   * @param customJPANameBuilder
   * @param metamodel
   * @param postProcessor
   * @param packageName
   * @throws ODataJPAModelException
   */
  IntermediateServiceDocument(final JPAEdmNameBuilder nameBuilder, final Metamodel jpaMetamodel,
      final JPAEdmMetadataPostProcessor postProcessor, final String[] packageName) throws ODataJPAModelException {

    this.pP = postProcessor != null ? postProcessor : new DefaultEdmPostProcessor();
    IntermediateModelElement.setPostProcessor(pP);

    this.reflections = createReflections(packageName);
    this.references = new IntermediateReferences();
    pP.provideReferences(this.references);
    this.nameBuilder = nameBuilder;
    this.jpaMetamodel = jpaMetamodel;
    this.schemaListInternalKey = new HashMap<>();
    buildIntermediateSchemas();
    this.container = new IntermediateEntityContainer(nameBuilder, schemaListInternalKey);
    setContainer();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getAllSchemas()
   */
  @Override
  public List<CsdlSchema> getAllSchemas() throws ODataJPAModelException {
    final List<CsdlSchema> allSchemas = getEdmSchemas();
    allSchemas.addAll(references.getSchemas());
    return allSchemas;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getEdmEntityContainer()
   */
  @Override
  public CsdlEntityContainer getEdmEntityContainer() throws ODataJPAModelException {
    return container.getEdmItem();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getEdmSchemas()
   */
  @Override
  public List<CsdlSchema> getEdmSchemas() throws ODataJPAModelException {
    return extractEdmSchemas();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getEntity(org.apache.olingo.commons.api.edm.
   * EdmType)
   */
  @Override
  public JPAEntityType getEntity(final EdmType edmType) throws ODataJPAModelException {
    final IntermediateSchema schema = schemaListInternalKey.get(edmType.getNamespace());
    if (schema != null)
      return schema.getEntityType(edmType.getName());
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getEntity(java.lang.Class)
   */
  @Override
  public JPAEntityType getEntity(final Class<?> entityClass) throws ODataJPAModelException {
    for (final Entry<String, IntermediateSchema> schema : schemaListInternalKey.entrySet()) {
      final JPAEntityType et = (JPAEntityType) schema.getValue().getEntityType(entityClass);
      if (et != null)
        return et;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument#getComplexType(org.apache.olingo.commons.api.edm
   * .EdmComplexType)
   */
  @Override
  public JPAStructuredType getComplexType(final EdmComplexType edmType) {
    final IntermediateSchema schema = schemaListInternalKey.get(edmType.getNamespace());
    if (schema != null)
      return schema.getComplexType(edmType.getName());
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getEntity(org.apache.olingo.commons.api.edm.
   * FullQualifiedName)
   */
  @Override
  public JPAEntityType getEntity(final FullQualifiedName typeName) {
    final IntermediateSchema schema = schemaListInternalKey.get(typeName.getNamespace());
    if (schema != null)
      return schema.getEntityType(typeName.getName());
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getEntity(java.lang.String)
   */
  @Override
  public JPAEntityType getEntity(final String edmTargetName) throws ODataJPAModelException {
    final IntermediateTopLevelEntity target = determineTopLevelEntity(edmTargetName);
    return target != null ? target.getEntityType() : null;
  }

  @CheckForNull
  private IntermediateTopLevelEntity determineTopLevelEntity(final String edmTargetName) throws ODataJPAModelException {
    final Optional<IntermediateTopLevelEntity> target = Optional.ofNullable(container.getEntitySet(edmTargetName));
    return target.orElse(container.getSingleton(edmTargetName));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getEntitySet(com.sap.olingo.jpa.metadata.core.
   * edm.mapper.api.JPAEntityType)
   */
  @Override
  public JPAEntitySet getEntitySet(final JPAEntityType entityType) throws ODataJPAModelException {
    return container.getEntitySet(entityType);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getFunction(org.apache.olingo.commons.api.edm.
   * EdmFunction)
   */
  @Override
  public JPAFunction getFunction(final EdmFunction function) {
    final IntermediateSchema schema = schemaListInternalKey.get(function.getNamespace());
    if (schema != null)
      return schema.getFunction(function.getName());
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getAction(org.apache.olingo.commons.api.edm.
   * EdmFunction)
   */
  @Override
  public JPAAction getAction(final EdmAction action) {
    final IntermediateSchema schema = schemaListInternalKey.get(action.getNamespace());
    if (schema != null)
      return schema.getAction(action.getName());
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getReferences()
   */
  @Override
  public List<EdmxReference> getReferences() {
    return references.getEdmReferences();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocument#getTerm(org.apache.olingo.commons.api.edm.
   * FullQualifiedName)
   */
  @Override
  public CsdlTerm getTerm(final FullQualifiedName termName) {
    return this.references.getTerm(termName);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.apache.olingo.server.api.etag.CustomETagSupport#hasETag(org.apache.olingo.commons.api.edm.EdmBindingTarget)
   */
  @Override
  public boolean hasETag(final EdmBindingTarget entitySetOrSingleton) {
    try {
      return getEntity(entitySetOrSingleton.getEntityType().getFullQualifiedName()).hasEtag();
    } catch (final ODataJPAModelException e) {
      LOGGER.debug("Error during binding target determination", e);
      return false;
    }
  }

  /**
   * Currently not supported => method always returns false
   */
  @Override
  public boolean hasMediaETag(final EdmBindingTarget entitySetOrSingleton) {
    // TODO Not Supported yet
    return false;
  }

  @Override
  public JPAEdmNameBuilder getNameBuilder() {
    return nameBuilder;
  }

  private void buildIntermediateSchemas() throws ODataJPAModelException {
    final IntermediateSchema schema = new IntermediateSchema(nameBuilder, jpaMetamodel, reflections);
    schemaListInternalKey.put(schema.internalName, schema);
  }

  private Reflections createReflections(final String... packageName) {
    if (packageName != null && packageName.length > 0) {
      final ConfigurationBuilder configBuilder = new ConfigurationBuilder();
      configBuilder.setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner());
      configBuilder.forPackages(packageName);
      configBuilder.filterInputsBy(new FilterBuilder().includePackage(packageName));
      return new Reflections(configBuilder);
    } else {
      return null;
    }
  }

  private List<CsdlSchema> extractEdmSchemas() throws ODataJPAModelException {
    final List<CsdlSchema> schemas = new ArrayList<>();
    try {
      if (schemaListInternalKey.isEmpty())
        buildIntermediateSchemas();
      for (final IntermediateSchema schema : schemaListInternalKey.values()) {
        schemas.add(schema.getEdmItem());
      }
    } catch (final ODataJPAModelException e) {
      schemaListInternalKey.clear();
      throw e;
    }
    return schemas;
  }

  private void setContainer() {
    for (final IntermediateSchema schema : schemaListInternalKey.values()) {
      schema.setContainer(container);
      // OData allows to combine multiple schemas in one metadata document. The container has to be added to one of
      // those.
      // We pick the first that we can get:
      break; // NOSONAR
    }
  }

  @Override
  public JPAEnumerationAttribute getEnumType(final EdmEnumType type) {
    final IntermediateSchema schema = schemaListInternalKey.get(type.getFullQualifiedName().getNamespace());
    if (schema != null)
      return schema.getEnumerationType(type);
    return null;
  }

  @Override
  public JPAEnumerationAttribute getEnumType(final String fqnAsString) {
    final FullQualifiedName fqn = new FullQualifiedName(fqnAsString);
    final IntermediateSchema schema = schemaListInternalKey.get(fqn.getNamespace());
    if (schema != null)
      return schema.getEnumerationType(fqn.getName());
    return null;
  }

  @Override
  public Map<String, JPAProtectionInfo> getClaims() throws ODataJPAModelException {
    if (claims == null) {
      claims = new HashMap<>();
      for (final IntermediateSchema schema : schemaListInternalKey.values()) {
        for (final IntermediateEntityType<?> et : schema.getEntityTypes()) {
          for (final JPAProtectionInfo protection : et.getProtections()) {
            claims.put(protection.getClaimName(), protection);
          }
        }
      }
    }
    return claims;
  }
}
