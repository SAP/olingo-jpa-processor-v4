package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.server.api.etag.CustomETagSupport;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntitySetAccess;

/**
 * 
 * @author Oliver Grande
 *
 */
class IntermediateEntitySet extends IntermediateModelElement implements CustomETagSupport, IntermediateEntitySetAccess {
  private final IntermediateEntityType entityType;
  private CsdlEntitySet edmEntitySet;

  IntermediateEntitySet(final JPAEdmNameBuilder nameBuilder, final IntermediateEntityType et)
      throws ODataJPAModelException {
    super(nameBuilder, IntNameBuilder.buildEntitySetName(nameBuilder, et));
    entityType = et;
    setExternalName(nameBuilder.buildEntitySetName(et.getEdmItem()));
  }

  /**
   * Returns the entity type to be used internally e.g. for the query generation
   * @return
   */
  public JPAEntityType getEntityType() {
    return entityType;
  }

  /**
   * Returns the entity type that shall be used to create the metadata document.
   * This can differ from the internally used one e.g. if multiple entity sets shall
   * point to the same entity type, but base on different tables
   * @return
   * @throws ODataJPAModelException
   */
  public JPAEntityType getODataEntityType() throws ODataJPAModelException {
    if (entityType.asEntitySet())
      return (JPAEntityType) entityType.getBaseType();
    else
      return entityType;
  }

  @Override
  public void addAnnotations(List<CsdlAnnotation> annotations) {
    this.edmAnnotations.addAll(annotations);
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmEntitySet == null) {
      postProcessor.processEntitySet(this);
      edmEntitySet = new CsdlEntitySet();

      final CsdlEntityType edmEt = ((IntermediateEntityType) getODataEntityType()).getEdmItem();
      edmEntitySet.setName(getExternalName());
      edmEntitySet.setType(nameBuilder.buildFQN(edmEt.getName()));

      // Create navigation Property Binding
      // V4: An entity set or a singleton SHOULD contain an edm:NavigationPropertyBinding element for each navigation
      // property of its entity type, including navigation properties defined on complex typed properties.
      // If omitted, clients MUST assume that the target entity set or singleton can vary per related entity.
      edmEntitySet.setNavigationPropertyBindings(returnNullIfEmpty(determinePropertyBinding()));
      edmEntitySet.setAnnotations(edmAnnotations);
    }
  }

  private List<CsdlNavigationPropertyBinding> determinePropertyBinding() throws ODataJPAModelException {
    final List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
    final List<JPAAssociationPath> naviPropertyList = entityType.getAssociationPathList();
    if (naviPropertyList != null && !naviPropertyList.isEmpty()) {
      // http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398035

      for (final JPAAssociationPath naviPropertyPath : naviPropertyList) {
        final CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
        navPropBinding.setPath(naviPropertyPath.getAlias());

        // TODO Check is FQN is better here
        final IntermediateNavigationProperty naviProperty = ((IntermediateNavigationProperty) naviPropertyPath
            .getLeaf());
        navPropBinding.setTarget(nameBuilder.buildEntitySetName(naviProperty.getTargetEntity().getExternalName()));
        navPropBindingList.add(navPropBinding);
      }
    }
    return navPropBindingList;
  }

  @Override
  CsdlEntitySet getEdmItem() throws ODataJPAModelException { // New test EdmItem with ODataEntityType
    lazyBuildEdmItem();
    return edmEntitySet;
  }

  @Override
  public boolean hasETag(final EdmBindingTarget entitySetOrSingleton) {
    try {
      return entityType.hasEtag();
    } catch (ODataJPAModelException e) {
      // TODO logging
      return false;
    }
  }

  @Override
  public boolean hasMediaETag(final EdmBindingTarget entitySetOrSingleton) {
    // TODO implement this
    return false;
  }
}
