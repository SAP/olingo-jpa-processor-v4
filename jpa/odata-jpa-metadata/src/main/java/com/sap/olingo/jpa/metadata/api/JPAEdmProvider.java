package com.sap.olingo.jpa.metadata.api;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Metamodel;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotations;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAServiceDocumentFactory;

public class JPAEdmProvider extends CsdlAbstractEdmProvider {

  private final JPAEdmNameBuilder nameBuilder;
  private final JPAServiceDocument serviceDocument;

  // http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397930
  public JPAEdmProvider(@Nonnull final String namespace, @Nonnull final EntityManagerFactory emf,
      final JPAEdmMetadataPostProcessor postProcessor, final String[] packageName) throws ODataException {
    this(namespace, Objects.requireNonNull(emf.getMetamodel()), postProcessor, packageName);
  }

  public JPAEdmProvider(@Nonnull final String namespace, final Metamodel jpaMetamodel,
      final JPAEdmMetadataPostProcessor postProcessor, final String[] packageName) throws ODataException {

    this(jpaMetamodel, postProcessor, packageName, new JPADefaultEdmNameBuilder(namespace));
  }

  public JPAEdmProvider(final Metamodel jpaMetamodel, final JPAEdmMetadataPostProcessor postProcessor,
      final String[] packageName, final JPAEdmNameBuilder nameBuilder) throws ODataException {
    super();
    this.nameBuilder = nameBuilder;
    // After this call either a schema exists or an exception has been thrown
    this.serviceDocument = new JPAServiceDocumentFactory(nameBuilder, jpaMetamodel, postProcessor, packageName)
        .getServiceDocument();
  }

  /**
   * This method should return a {@link CsdlComplexType} or <b>null</b> if nothing is found.
   *
   * @param complexTypeName full qualified name of complex type
   * @return for the given name
   * @throws ODataException
   */
  @Override
  public CsdlComplexType getComplexType(final FullQualifiedName complexTypeName) throws ODataException {
    for (final CsdlSchema schema : serviceDocument.getAllSchemas()) {
      if (schema.getNamespace().equals(complexTypeName.getNamespace())
          || (schema.getAlias() != null && schema.getAlias().equals(complexTypeName.getNamespace()))) {
        return schema.getComplexType(complexTypeName.getName());
      }
    }
    return null;
  }

  /**
   * Returns the entity container of this edm
   * @return of this edm
   * @throws ODataException
   */
  @Override
  public CsdlEntityContainer getEntityContainer() throws ODataException {
    return serviceDocument.getEdmEntityContainer();
  }

  /**
   * This method should return an {@link CsdlEntityContainerInfo} or <b>null</b> if nothing is found
   *
   * @param entityContainerName (null for default container)
   * @return for the given name
   * @throws ODataException
   */
  @Override
  public CsdlEntityContainerInfo getEntityContainerInfo(final FullQualifiedName entityContainerName)
      throws ODataException {
    // This method is invoked when displaying the Service Document at e.g.: .../DemoService.svc
    if (entityContainerName == null
        || entityContainerName.equals(buildFQN(nameBuilder.buildContainerName()))) {
      final CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
      entityContainerInfo.setContainerName(buildFQN(nameBuilder.buildContainerName()));
      return entityContainerInfo;
    }
    return null;
  }

  /**
   * This method should return an {@link CsdlEntitySet} or <b>null</b> if nothing is found
   *
   * @param entityContainer this EntitySet is contained in
   * @param entitySetName name of entity set
   * @return for the given container and entity set name
   * @throws ODataException
   */
  @Override
  public CsdlEntitySet getEntitySet(final FullQualifiedName entityContainerFQN, final String entitySetName)
      throws ODataException {
    final CsdlEntityContainer container = serviceDocument.getEdmEntityContainer();
    if (entityContainerFQN.equals(buildFQN(container.getName()))) {
      return container.getEntitySet(entitySetName);
    }
    return null;
  }

  @Override
  public CsdlSingleton getSingleton(final FullQualifiedName entityContainerFQN, final String singletonName)
      throws ODataException {
    final CsdlEntityContainer container = serviceDocument.getEdmEntityContainer();
    if (entityContainerFQN.equals(buildFQN(container.getName()))) {
      return container.getSingleton(singletonName);
    }
    return null;
  }

  /**
   * This method should return an {@link CsdlEntityType} or <b>null</b> if nothing is found
   *
   * @param entityTypeName full qualified name of entity type
   * @return for the given name
   * @throws ODataException
   */
  @Override
  public CsdlEntityType getEntityType(final FullQualifiedName entityTypeName) throws ODataException {

    for (final CsdlSchema schema : serviceDocument.getEdmSchemas()) {
      if (schema.getNamespace().equals(entityTypeName.getNamespace())) {
        return schema.getEntityType(entityTypeName.getName());
      }
    }
    return null;
  }

  /**
   * This method should return a {@link CsdlFunctionImport} or <b>null</b> if nothing is found
   *
   * @param entityContainer this FunctionImport is contained in
   * @param functionImportName name of function import
   * @return for the given container name and function import name
   * @throws ODataException
   */
  @Override
  public CsdlFunctionImport getFunctionImport(final FullQualifiedName entityContainerFQN,
      final String functionImportName) throws ODataException {
    final CsdlEntityContainer container = serviceDocument.getEdmEntityContainer();
    if (entityContainerFQN.equals(buildFQN(container.getName()))) {
      return container.getFunctionImport(functionImportName);
    }
    return null;
  }

  /**
   * This method should return a list of all {@link CsdlFunction} for the FullQualifiedName or <b>null</b> if nothing is
   * found
   *
   * @param functionName full qualified name of function
   * @return List of or null
   * @throws ODataException
   */
  @Override
  public List<CsdlFunction> getFunctions(final FullQualifiedName functionName) throws ODataException {
    for (final CsdlSchema schema : serviceDocument.getEdmSchemas()) {
      if (schema.getNamespace().equals(functionName.getNamespace())) {
        final List<CsdlFunction> functions = schema.getFunctions(functionName.getName());
        return functions.isEmpty() ? null : functions;
      }
    }
    return null; // NOSONAR see documentation
  }

  /**
   * This method should return a list of all {@link CsdlAction} for the FullQualifiedName
   * or <b>null</b> if nothing is found
   *
   * @param actionName full qualified name of action
   * @return List of
   * or null
   * @throws ODataException
   */
  @Override
  public List<CsdlAction> getActions(final FullQualifiedName actionName) throws ODataException {
    for (final CsdlSchema schema : serviceDocument.getEdmSchemas()) {
      if (schema.getNamespace().equals(actionName.getNamespace())) {
        return schema.getActions(actionName.getName());
      }
    }
    return null; // NOSONAR see documentation
  }

  /**
   * This method should return an {@link CsdlActionImport} or <b>null</b> if nothing is found
   *
   * @param entityContainer this ActionImport is contained in
   * @param actionImportName name of action import
   * @return for the given container and ActionImport name
   * @throws ODataException
   */
  @Override
  public CsdlActionImport getActionImport(final FullQualifiedName entityContainerFQN, final String actionImportName)
      throws ODataException {
    final CsdlEntityContainer container = serviceDocument.getEdmEntityContainer();
    if (entityContainerFQN.equals(buildFQN(container.getName()))) {
      return container.getActionImport(actionImportName);
    }
    return null;
  }

  /**
   * This method should return an {@link CsdlEnumType} or <b>null</b> if nothing is found
   *
   * @param enumTypeName full qualified name of enum type
   * @return for given name
   * @throws ODataException
   */
  @Override
  public CsdlEnumType getEnumType(final FullQualifiedName enumTypeNameFQN) throws ODataException {

    for (final CsdlSchema schema : serviceDocument.getEdmSchemas()) {
      if (schema.getNamespace().equals(enumTypeNameFQN.getNamespace())) {
        return schema.getEnumType(enumTypeNameFQN.getName());
      }
    }
    return null;
  }

  /**
   * Gets annotations group.
   *
   * @param targetName full qualified name of target
   * @param qualifier for the given target. Might be null.
   * @return group for the given Target
   * @throws ODataException
   */
  @Override
  public CsdlAnnotations getAnnotationsGroup(final FullQualifiedName targetName, final String qualifier)
      throws ODataException {
    return null;
  }

  /**
   * This method should return a {@link CsdlTerm} for the FullQualifiedName or <b>null</b> if nothing is found.
   * @param termName the name of the Term
   * @return or null
   * @throws ODataException
   */
  @Override
  public CsdlTerm getTerm(final FullQualifiedName termName) throws ODataException {
    return serviceDocument.getTerm(termName);
  }

  /**
   * This method should return an {@link CsdlTypeDefinition} or <b>null</b> if nothing is found
   *
   * @param typeDefinitionName full qualified name of type definition
   * @return for given name
   * @throws ODataException
   */
  @Override
  public CsdlTypeDefinition getTypeDefinition(final FullQualifiedName typeDefinitionName) throws ODataException {
    for (final CsdlSchema schema : serviceDocument.getAllSchemas()) {
      if (schema.getNamespace().equals(typeDefinitionName.getNamespace())) {
        return schema.getTypeDefinition(typeDefinitionName.getName());
      }
    }
    return null;
  }

  /**
   * This method should return a collection of all {@link CsdlSchema}
   *
   * @return List of
   * @throws ODataException
   */
  @Override
  public List<CsdlSchema> getSchemas() throws ODataException {
    return serviceDocument.getEdmSchemas();
  }

  public @Nonnull JPAServiceDocument getServiceDocument() {
    return serviceDocument;
  }

  public void setRequestLocales(final Enumeration<Locale> locales) {
    ODataJPAException.setLocales(locales);
  }

  public List<EdmxReference> getReferences() {
    return serviceDocument.getReferences();
  }

  public JPAEdmNameBuilder getEdmNameBuilder() {
    return nameBuilder;
  }

  protected final FullQualifiedName buildFQN(final String name) {
    return new FullQualifiedName(nameBuilder.getNamespace(), name);
  }
}
