package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAsEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntityTypeAccess;

/**
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397974"
 * >OData Version 4.0 Part 3 - 8 Entity Type</a>
 * @author Oliver Grande
 *
 */
final class IntermediateEntityType extends IntermediateStructuredType implements JPAEntityType,
    IntermediateEntityTypeAccess {
  private CsdlEntityType edmEntityType;
  private boolean hasEtag;
  private List<JPAAttribute> keyAttributes;
  private final boolean asEntitySet;

  IntermediateEntityType(final JPAEdmNameBuilder nameBuilder, final EntityType<?> et, final IntermediateSchema schema) {
    super(nameBuilder, et, schema);
    this.setExternalName(nameBuilder.buildEntityTypeName(et));
    asEntitySet = determineAsEntitySet();
  }

  @Override
  public void addAnnotations(List<CsdlAnnotation> annotations) {
    this.edmAnnotations.addAll(annotations);

  }

  @Override
  public String getContentType() throws ODataJPAModelException {
    final IntermediateSimpleProperty stream = getStreamProperty();
    return stream.getContentType();
  }

  @Override
  public JPAPath getContentTypeAttributePath() throws ODataJPAModelException {
    final String propertyInternalName = getStreamProperty().getContentTypeProperty();
    if (propertyInternalName == null || propertyInternalName.isEmpty()) {
      return null;
    }
    // Ensure that Ignore is ignored
    return getPathByDBField(getProperty(propertyInternalName).getDBFieldName());
  }

  @Override
  public List<JPAAttribute> getKey() throws ODataJPAModelException {
    lazyBuildEdmItem();

    if (keyAttributes == null) {
      keyAttributes = new ArrayList<>();
      for (final String internalName : this.declaredPropertiesList.keySet()) {
        final JPAAttribute attribute = this.declaredPropertiesList.get(internalName);
        if (attribute.isKey()) {
          if (attribute.isComplex()) {
            keyAttributes.addAll(((IntermediateEmbeddedIdProperty) attribute).getStructuredType().getAttributes());
          } else
            keyAttributes.add(attribute);
        }
      }
      final IntermediateStructuredType baseType = getBaseType();
      if (baseType != null) {
        keyAttributes.addAll(((IntermediateEntityType) baseType).getKey());
      }
    }
    return keyAttributes;
  }

  @Override
  public List<JPAPath> getKeyPath() throws ODataJPAModelException {
    lazyBuildEdmItem();

    final List<JPAPath> result = new ArrayList<>();
    for (final String internalName : this.declaredPropertiesList.keySet()) {
      final JPAAttribute attribute = this.declaredPropertiesList.get(internalName);
      if (attribute instanceof IntermediateEmbeddedIdProperty) {
        result.add(intermediatePathMap.get(attribute.getExternalName()));
      } else if (attribute.isKey())
        result.add(resolvedPathMap.get(attribute.getExternalName()));
    }
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null) {
      result.addAll(((IntermediateEntityType) baseType).getKeyPath());
    }
    return result;
  }

  @Override
  public Class<?> getKeyType() {
    if (jpaManagedType instanceof IdentifiableType<?>)
      return ((IdentifiableType<?>) jpaManagedType).getIdType().getJavaType();
    else
      return null;
  }

  @Override
  public List<JPAPath> getSearchablePath() throws ODataJPAModelException {
    final List<JPAPath> allPath = getPathList();
    final List<JPAPath> searchablePath = new ArrayList<>();
    for (final JPAPath p : allPath) {
      if (p.getLeaf().isSearchable())
        searchablePath.add(p);
    }
    return searchablePath;
  }

  @Override
  public JPAPath getStreamAttributePath() throws ODataJPAModelException {
    return getPath(getStreamProperty().getExternalName());
  }

  @Override
  public String getTableName() {
    final AnnotatedElement a = jpaManagedType.getJavaType();
    Table t = null;

    if (a != null)
      t = a.getAnnotation(Table.class);

    return (t == null) ? jpaManagedType.getJavaType().getName().toUpperCase(Locale.ENGLISH)
        : t.name();
  }

  @Override
  public boolean hasEtag() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return hasEtag;
  }

  @Override
  public boolean hasStream() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return this.determineHasStream();
  }

  @Override
  public boolean ignore() {
    return (asEntitySet || super.ignore());
  }

  @Override
  public boolean isAbstract() {
    return determineAbstract();
  }

  @Override
  public List<JPAPath> searchChildPath(final JPAPath selectItemPath) {
    final List<JPAPath> result = new ArrayList<>();
    for (final String pathName : this.resolvedPathMap.keySet()) {
      final JPAPath p = resolvedPathMap.get(pathName);
      if (!p.ignore() && p.getAlias().startsWith(selectItemPath.getAlias()))
        result.add(p);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <T> List<?> extractEdmModelElements(final Map<String, ? extends IntermediateModelElement> mappingBuffer)
      throws ODataJPAModelException {

    final List<T> extractionTarget = new ArrayList<>();
    for (final String externalName : mappingBuffer.keySet()) {
      if (!(mappingBuffer.get(externalName)).ignore()
          // Skip Streams
          && !(mappingBuffer.get(externalName) instanceof IntermediateSimpleProperty &&
              ((IntermediateSimpleProperty) mappingBuffer.get(externalName)).isStream())) {
        if (mappingBuffer.get(externalName) instanceof IntermediateEmbeddedIdProperty) {
          extractionTarget.addAll((Collection<? extends T>) resolveEmbeddedId(
              (IntermediateEmbeddedIdProperty) mappingBuffer.get(externalName)));
        } else {
          extractionTarget.add((T) mappingBuffer.get(externalName).getEdmItem());
        }
      }
    }
    return returnNullIfEmpty(extractionTarget);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmEntityType == null) {
      buildPropertyList();
      buildNaviPropertyList();
      postProcessor.processEntityType(this);

      edmEntityType = new CsdlEntityType();
      edmEntityType.setName(getExternalName());
      edmEntityType.setProperties((List<CsdlProperty>) extractEdmModelElements(declaredPropertiesList));
      edmEntityType.setNavigationProperties((List<CsdlNavigationProperty>) extractEdmModelElements(
          declaredNaviPropertiesList));
      edmEntityType.setKey(extractEdmKeyElements(declaredPropertiesList));
      edmEntityType.setAbstract(determineAbstract());
      edmEntityType.setBaseType(determineBaseType());
      edmEntityType.setHasStream(determineHasStream());
      edmEntityType.setAnnotations(determineAnnotations());
      determineHasEtag();
      // TODO determine OpenType
    }
  }

  boolean asEntitySet() {
    return asEntitySet;
  }

  boolean determineAbstract() {
    final int modifiers = jpaManagedType.getJavaType().getModifiers();
    return Modifier.isAbstract(modifiers);
  }

  private void determineHasEtag() throws ODataJPAModelException {
    for (final String internalName : this.declaredPropertiesList.keySet()) {
      if (declaredPropertiesList.get(internalName).isEtag()) {
        hasEtag = true;
        return;
      }
    }
    if (getBaseType() != null && getBaseType() instanceof IntermediateEntityType)
      hasEtag = ((IntermediateEntityType) getBaseType()).hasEtag();
  }

  /**
   * Creates the key of an entity. In case the POJO is declared with an embedded ID the key fields get resolved, so that
   * they occur as separate properties within the metadata document
   * 
   * @param propertyList
   * @return
   * @throws ODataJPAModelException
   */
  List<CsdlPropertyRef> extractEdmKeyElements(final Map<String, IntermediateProperty> propertyList)
      throws ODataJPAModelException {
    // TODO setAlias
    final List<CsdlPropertyRef> keyList = new ArrayList<>();
    for (final String internalName : propertyList.keySet()) {
      if (propertyList.get(internalName).isKey()) {
        if (propertyList.get(internalName).isComplex()) {
          final List<JPAAttribute> idAttributes = ((IntermediateComplexType) propertyList.get(internalName)
              .getStructuredType())
                  .getAttributes();
          for (final JPAAttribute idAttribute : idAttributes) {
            final CsdlPropertyRef key = new CsdlPropertyRef();
            key.setName(idAttribute.getExternalName());
            keyList.add(key);
          }
        } else {
          final CsdlPropertyRef key = new CsdlPropertyRef();
          key.setName(propertyList.get(internalName).getExternalName());
          keyList.add(key);
        }
      }
    }
    return returnNullIfEmpty(keyList);
  }

  @Override
  CsdlEntityType getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmEntityType;
  }

  private List<CsdlAnnotation> determineAnnotations() throws ODataJPAModelException {
    getAnnotations(edmAnnotations, this.jpaManagedType.getJavaType(), internalName);
    return edmAnnotations;
  }

  private boolean determineAsEntitySet() {

    final EdmAsEntitySet jpaAsEntitySet = this.jpaManagedType.getJavaType().getAnnotation(EdmAsEntitySet.class);
    return jpaAsEntitySet != null;
  }

  private List<?> resolveEmbeddedId(final IntermediateEmbeddedIdProperty embeddedId) throws ODataJPAModelException {
    return ((IntermediateComplexType) embeddedId.getStructuredType()).getEdmItem().getProperties();
  }
}
