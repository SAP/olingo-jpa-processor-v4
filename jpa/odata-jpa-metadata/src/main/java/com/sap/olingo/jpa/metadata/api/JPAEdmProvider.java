package com.sap.olingo.jpa.metadata.api;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

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
  public JPAEdmProvider(final String namespace, final EntityManagerFactory emf,
      final JPAEdmMetadataPostProcessor postProcessor, final String[] packageName) throws ODataException {
    this(namespace, emf.getMetamodel(), postProcessor, packageName);
  }

  public JPAEdmProvider(final String namespace, final Metamodel jpaMetamodel,
      final JPAEdmMetadataPostProcessor postProcessor, final String[] packageName) throws ODataException {
    this(jpaMetamodel, postProcessor, packageName, new JPADefaultEdmNameBuilder(namespace));
  }

  public JPAEdmProvider(final EntityManagerFactory emf,
      final JPAEdmMetadataPostProcessor postProcessor, final String[] packageName, final JPAEdmNameBuilder nameBuilder)
      throws ODataException {
    this(emf.getMetamodel(), postProcessor, packageName, nameBuilder);
  }

  public JPAEdmProvider(final Metamodel jpaMetamodel, final JPAEdmMetadataPostProcessor postProcessor,
      final String[] packageName, final JPAEdmNameBuilder nameBuilder) throws ODataException {
    super();
    this.nameBuilder = nameBuilder;
    this.serviceDocument = new JPAServiceDocumentFactory(nameBuilder, jpaMetamodel, postProcessor, packageName).getServiceDocument();
  }

  @Override
  public CsdlComplexType getComplexType(final FullQualifiedName complexTypeName) throws ODataException {
    for (final CsdlSchema schema : serviceDocument.getAllSchemas()) {
      if (schema.getNamespace().equals(complexTypeName.getNamespace())
          || schema.getAlias() != null && schema.getAlias().equals(complexTypeName.getNamespace())) {
        return schema.getComplexType(complexTypeName.getName());
      }
    }
    return null;
  }

  @Override
  public CsdlEntityContainer getEntityContainer() throws ODataException {
    return serviceDocument.getEdmEntityContainer();
  }

  @Override
  public CsdlEntityContainerInfo getEntityContainerInfo(final FullQualifiedName entityContainerName)
      throws ODataException {
    // This method is invoked when displaying the Service Document at e.g.
    // .../DemoService.svc
    if (entityContainerName == null
        || entityContainerName.equals(buildFQN(nameBuilder.buildContainerName()))) {
      final CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
      entityContainerInfo.setContainerName(buildFQN(nameBuilder.buildContainerName()));
      return entityContainerInfo;
    }
    return null;
  }

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
  public CsdlEntityType getEntityType(final FullQualifiedName entityTypeName) throws ODataException {

    for (final CsdlSchema schema : serviceDocument.getEdmSchemas()) {
      if (schema.getNamespace().equals(entityTypeName.getNamespace())) {
        return schema.getEntityType(entityTypeName.getName());
      }
    }
    return null;
  }

  @Override
  public CsdlFunctionImport getFunctionImport(final FullQualifiedName entityContainerFQN,
      final String functionImportName) throws ODataException {
    final CsdlEntityContainer container = serviceDocument.getEdmEntityContainer();
    if (entityContainerFQN.equals(buildFQN(container.getName()))) {
      return container.getFunctionImport(functionImportName);
    }
    return null;
  }

  @Override
  public List<CsdlFunction> getFunctions(final FullQualifiedName functionName) throws ODataException {
    for (final CsdlSchema schema : serviceDocument.getEdmSchemas()) {
      if (schema.getNamespace().equals(functionName.getNamespace())) {
        return schema.getFunctions(functionName.getName());
      }
    }
    return null; // NOSONAR see documentation
  }

  @Override
  public List<CsdlAction> getActions(final FullQualifiedName actionName) throws ODataException {
    for (final CsdlSchema schema : serviceDocument.getEdmSchemas()) {
      if (schema.getNamespace().equals(actionName.getNamespace())) {
        return schema.getActions(actionName.getName());
      }
    }
    return null; // NOSONAR see documentation
  }

  @Override
  public CsdlActionImport getActionImport(final FullQualifiedName entityContainerFQN, final String actionImportName)
      throws ODataException {
    final CsdlEntityContainer container = serviceDocument.getEdmEntityContainer();
    if (entityContainerFQN.equals(buildFQN(container.getName()))) {
      return container.getActionImport(actionImportName);
    }
    return null;
  }

  @Override
  public CsdlEnumType getEnumType(final FullQualifiedName enumTypeNameFQN) throws ODataException {

    for (final CsdlSchema schema : serviceDocument.getEdmSchemas()) {
      if (schema.getNamespace().equals(enumTypeNameFQN.getNamespace())) {
        return schema.getEnumType(enumTypeNameFQN.getName());
      }
    }
    return null;
  }

  @Override
  public CsdlAnnotations getAnnotationsGroup(final FullQualifiedName targetName, String qualifier)
      throws ODataException {
    return null;
  }

  @Override
  public CsdlTerm getTerm(final FullQualifiedName termName) throws ODataException {
    return serviceDocument.getTerm(termName);
  }

  @Override
  public CsdlTypeDefinition getTypeDefinition(final FullQualifiedName typeDefinitionName) throws ODataException {
    for (final CsdlSchema schema : serviceDocument.getAllSchemas()) {
      if (schema.getNamespace().equals(typeDefinitionName.getNamespace())) {
        return schema.getTypeDefinition(typeDefinitionName.getName());
      }
    }
    return null;
  }

  @Override
  public List<CsdlSchema> getSchemas() throws ODataException {
    return serviceDocument.getEdmSchemas();
  }

  public JPAServiceDocument getServiceDocument() {
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
