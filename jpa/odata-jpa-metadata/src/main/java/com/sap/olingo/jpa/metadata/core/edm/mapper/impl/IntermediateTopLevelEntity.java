package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataNavigationPath;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPathNotFoundException;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPropertyPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAQueryExtension;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPATopLevelEntity;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

abstract class IntermediateTopLevelEntity extends IntermediateModelElement implements JPATopLevelEntity,
    ODataAnnotatable {

  final IntermediateEntityType<?> entityType;

  IntermediateTopLevelEntity(final JPAEdmNameBuilder nameBuilder, final IntermediateEntityType<?> et,
      final IntermediateAnnotationInformation annotationInfo) {

    super(nameBuilder, InternalNameBuilder.buildEntitySetName(nameBuilder, et), annotationInfo);
    this.entityType = et;
  }

  protected List<CsdlNavigationPropertyBinding> determinePropertyBinding() throws ODataJPAModelException {
    final List<CsdlNavigationPropertyBinding> navigationPropBindingList = new ArrayList<>();
    final List<JPAAssociationPath> navigationPropertyList = entityType.getAssociationPathList();
    if (navigationPropertyList != null && !navigationPropertyList.isEmpty()) {
      // http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398035

      for (final JPAAssociationPath navigationPropertyPath : navigationPropertyList) {
        final JPAStructuredType targetType = navigationPropertyPath.getTargetType();
        if (targetType instanceof IntermediateEntityType
            && !(((IntermediateEntityType<?>) targetType).asEntitySet() || ((IntermediateEntityType<?>) targetType)
                .asSingleton())) {
          continue;
        }
        final CsdlNavigationPropertyBinding navigationPropBinding = new CsdlNavigationPropertyBinding();

        navigationPropBinding.setPath(navigationPropertyPath.getAlias());

        // TODO Check is FQN is better here
        final JPAAssociationAttribute navigationProperty = navigationPropertyPath.getLeaf();
        navigationPropBinding.setTarget(nameBuilder.buildEntitySetName(navigationProperty.getTargetEntity()
            .getExternalName()));
        navigationPropBindingList.add(navigationPropBinding);
      }
    }
    return navigationPropBindingList;
  }

  /**
   * Returns the entity type that shall be used to create the metadata document.
   * This can differ from the internally used one e.g. if multiple entity sets shall
   * point to the same entity type, but base on different tables
   * @return
   * @throws ODataJPAModelException
   */
  public JPAEntityType getODataEntityType() {
    if (entityType.asTopLevelOnly())
      return (JPAEntityType) entityType.getBaseType();
    else
      return entityType;
  }

  /**
   * Returns the entity type to be used internally e.g. for the query generation
   * @return
   */
  public JPAEntityType getEntityType() {
    return entityType;
  }

  @Override
  public Optional<JPAQueryExtension<EdmQueryExtensionProvider>> getQueryExtension()
      throws ODataJPAModelException {
    return getEntityType().getQueryExtension();
  }

  @Override
  public ODataPropertyPath convertStringToPath(final String internalPath) throws ODataPathNotFoundException {
    return entityType.convertStringToPath(internalPath);
  }

  @Override
  public ODataNavigationPath convertStringToNavigationPath(final String internalPath)
      throws ODataPathNotFoundException {
    return entityType.convertStringToNavigationPath(internalPath);
  }

  @Override
  public Annotation javaAnnotation(final String name) {
    return entityType.javaAnnotation(name);
  }

  @Override
  public Map<String, Annotation> javaAnnotations(final String packageName) {
    return entityType.javaAnnotations(packageName);
  }
}