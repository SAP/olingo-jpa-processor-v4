package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

abstract class IntermediateTopLevelEntity extends IntermediateModelElement {

  final IntermediateEntityType<?> entityType;

  IntermediateTopLevelEntity(final JPAEdmNameBuilder nameBuilder, final IntermediateEntityType<?> et) {
    super(nameBuilder, IntNameBuilder.buildEntitySetName(nameBuilder, et));
    this.entityType = et;
  }

  protected List<CsdlNavigationPropertyBinding> determinePropertyBinding() throws ODataJPAModelException {
    final List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<>();
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
        final CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();

        navPropBinding.setPath(navigationPropertyPath.getAlias());

        // TODO Check is FQN is better here
        final JPAAssociationAttribute navigationProperty = navigationPropertyPath.getLeaf();
        navPropBinding.setTarget(nameBuilder.buildEntitySetName(navigationProperty.getTargetEntity()
            .getExternalName()));
        navPropBindingList.add(navPropBinding);
      }
    }
    return navPropBindingList;
  }

  /**
   * Returns the entity type that shall be used to create the metadata document.
   * This can differ from the internally used one e.g. if multiple entity sets shall
   * point to the same entity type, but base on different tables
   * @return
   * @throws ODataJPAModelException
   */
  public JPAEntityType getODataEntityType() throws ODataJPAModelException {
    if (entityType.asTopLevelOnly())
      return (JPAEntityType) entityType.getBaseType();
    else
      return entityType;
  }
}