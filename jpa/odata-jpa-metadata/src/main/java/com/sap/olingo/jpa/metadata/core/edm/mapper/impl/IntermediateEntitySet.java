package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntitySetAccess;

/**
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398028">OData
 * Version 4.0 Part 3 - 13.2 Element edm:EntitySet</a>
 * @author Oliver Grande
 *
 */
final class IntermediateEntitySet extends IntermediateTopLevelEntity implements IntermediateEntitySetAccess,
    JPAEntitySet {
  private CsdlEntitySet edmEntitySet;

  IntermediateEntitySet(final JPAEdmNameBuilder nameBuilder, final IntermediateEntityType<?> et)
      throws ODataJPAModelException {
    super(nameBuilder, et);
    setExternalName(nameBuilder.buildEntitySetName(et.getEdmItem()));
  }

  /**
   * Returns the entity type that shall be used to create the metadata document.
   * This can differ from the internally used one e.g. if multiple entity sets shall
   * point to the same entity type, but base on different tables
   * @return
   */
  @Override
  public JPAEntityType getODataEntityType() {
    if (entityType.asTopLevelOnly())
      return (JPAEntityType) entityType.getBaseType();
    else
      return entityType;
  }

  @Override
  public void addAnnotations(final List<CsdlAnnotation> annotations) {
    this.edmAnnotations.addAll(annotations);
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmEntitySet == null) {
      postProcessor.processEntitySet(this);
      edmEntitySet = new CsdlEntitySet();

      final CsdlEntityType edmEt = ((IntermediateEntityType<?>) getODataEntityType()).getEdmItem();
      edmEntitySet.setName(getExternalName());
      edmEntitySet.setType(buildFQN(edmEt.getName()));

      // Create navigation Property Binding
      // V4: An entity set or a singleton SHOULD contain an edm:NavigationPropertyBinding element for each navigation
      // property of its entity type, including navigation properties defined on complex typed properties.
      // If omitted, clients MUST assume that the target entity set or singleton can vary per related entity.
      edmEntitySet.setNavigationPropertyBindings(returnNullIfEmpty(determinePropertyBinding()));
      edmEntitySet.setAnnotations(edmAnnotations);
    }
  }

  @Override
  CsdlEntitySet getEdmItem() throws ODataJPAModelException { // New test EdmItem with ODataEntityType
    if (edmEntitySet == null) {
      lazyBuildEdmItem();
    }
    return edmEntitySet;
  }

}