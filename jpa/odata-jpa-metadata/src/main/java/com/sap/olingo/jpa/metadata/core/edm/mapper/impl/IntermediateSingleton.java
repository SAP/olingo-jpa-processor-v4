package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPASingleton;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateSingletonAccess;

/**
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398032">OData
 * Version 4.0 Part 3 - 13.3 Element edm:Singleton</a>
 * @author Oliver Grande
 * @since 1.0.3
 */
final class IntermediateSingleton extends IntermediateTopLevelEntity implements IntermediateSingletonAccess,
    JPASingleton {

  private CsdlSingleton edmSingleton;

  IntermediateSingleton(final JPAEdmNameBuilder nameBuilder, final IntermediateEntityType<?> et,
      final IntermediateAnnotationInformation annotationInfo)
      throws ODataJPAModelException {
    super(nameBuilder, et, annotationInfo);
    setExternalName(nameBuilder.buildSingletonName(et.getEdmItem()));
  }

  @Override
  public void addAnnotations(final List<CsdlAnnotation> annotations) {
    this.edmAnnotations.addAll(annotations);
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmSingleton == null) {
      retrieveAnnotations(this, Applicability.SINGLETON);
      postProcessor.processSingleton(this);
      edmSingleton = new CsdlSingleton();

      final var edmEt = ((IntermediateEntityType<?>) getODataEntityType()).getEdmItem();
      edmSingleton.setName(getExternalName());
      edmSingleton.setType(buildFQN(edmEt.getName()));
      edmSingleton.setMapping(null);
      // Create navigation Property Binding
      // V4: An entity set or a singleton SHOULD contain an edm:NavigationPropertyBinding element for each navigation
      // property of its entity type, including navigation properties defined on complex typed properties.
      // If omitted, clients MUST assume that the target entity set or singleton can vary per related entity.
      edmSingleton.setNavigationPropertyBindings(determinePropertyBinding());
      edmSingleton.setAnnotations(edmAnnotations);
    }
  }

  @Override
  CsdlSingleton getEdmItem() throws ODataJPAModelException {
    if (edmSingleton == null) {
      lazyBuildEdmItem();
    }
    return edmSingleton;
  }

  @Override
  public CsdlAnnotation getAnnotation(final String alias, final String term) throws ODataJPAModelException {
    if (edmSingleton == null) {
      lazyBuildEdmItem();
    }
    return filterAnnotation(alias, term);
  }
}
