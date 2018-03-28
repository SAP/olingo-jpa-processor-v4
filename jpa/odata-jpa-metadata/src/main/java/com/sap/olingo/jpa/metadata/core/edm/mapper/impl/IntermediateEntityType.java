package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.Type;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAsEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
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
  public JPACollectionAttribute getCollectionAttribute(final String externalName) throws ODataJPAModelException {
    final JPAPath path = getPath(externalName);
    if (path != null && path.getLeaf() instanceof JPACollectionAttribute)
      return (JPACollectionAttribute) path.getLeaf();
    return null;
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
      final List<JPAAttribute> intermediateKey = new ArrayList<>(); // Cycle break
      for (final Entry<String, IntermediateProperty> property : this.declaredPropertiesList.entrySet()) {
        final JPAAttribute attribute = property.getValue();
        if (attribute.isKey()) {
          if (attribute.isComplex()) {
            intermediateKey.addAll(((IntermediateEmbeddedIdProperty) attribute).getStructuredType().getAttributes());
          } else
            intermediateKey.add(attribute);
        }
      }
      final IntermediateStructuredType baseType = getBaseType();
      if (baseType != null) {
        intermediateKey.addAll(((IntermediateEntityType) baseType).getKey());
      }
      keyAttributes = intermediateKey;
    }
    return keyAttributes;
  }

  @Override
  public List<JPAPath> getKeyPath() throws ODataJPAModelException {
    lazyBuildEdmItem();

    final List<JPAPath> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : this.declaredPropertiesList.entrySet()) {
      final JPAAttribute attribute = property.getValue();
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
    if (jpaManagedType instanceof IdentifiableType<?>) {
      Class<?> idClass = null;
      final Type<?> idType = ((IdentifiableType<?>) jpaManagedType).getIdType();
      if (idType == null)
        // Hibernate does not return an IdType in case of compound key that do not use EmbeddableId. So fallback to hand
        // made evaluation
        idClass = jpaManagedType.getJavaType().getAnnotation(IdClass.class).value();
      else
        idClass = idType.getJavaType();
      return idClass;
    } else
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
    for (final Entry<String, JPAPathImpl> path : this.resolvedPathMap.entrySet()) {
      final JPAPath p = path.getValue();
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
    for (final Entry<String, ? extends IntermediateModelElement> element : mappingBuffer.entrySet()) {
      if (!element.getValue().ignore()
          // Skip Streams
          && !(element.getValue() instanceof IntermediateSimpleProperty &&
              ((IntermediateSimpleProperty) element.getValue()).isStream())) {
        if (element.getValue() instanceof IntermediateEmbeddedIdProperty) {
          extractionTarget.addAll((Collection<? extends T>) resolveEmbeddedId(
              (IntermediateEmbeddedIdProperty) element.getValue()));
        } else {
          extractionTarget.add((T) element.getValue().getEdmItem());
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

  boolean dbEquals(final String dbCatalog, final String dbSchema, final String dbTableName) {
    final AnnotatedElement a = jpaManagedType.getJavaType();
    Table t = null;
    if (a != null)
      t = a.getAnnotation(Table.class);
    if (t == null)
      return (dbCatalog == null || dbCatalog.isEmpty())
          && (dbSchema == null || dbSchema.isEmpty())
          && dbTableName.equals(getTableName());
    else
      return dbCatalog.equals(t.catalog())
          && dbSchema.equals(t.schema())
          && dbTableName.equals(t.name());
  }

  boolean determineAbstract() {
    final int modifiers = jpaManagedType.getJavaType().getModifiers();
    return Modifier.isAbstract(modifiers);
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
    for (final Entry<String, IntermediateProperty> property : propertyList.entrySet()) {
      if (property.getValue().isKey()) {
        if (property.getValue().isComplex()) {
          final List<JPAAttribute> idAttributes = ((IntermediateComplexType) property.getValue()
              .getStructuredType()).getAttributes();
          for (final JPAAttribute idAttribute : idAttributes) {
            final CsdlPropertyRef keyElement = new CsdlPropertyRef();
            keyElement.setName(idAttribute.getExternalName());
            keyList.add(keyElement);
          }
        } else {
          final CsdlPropertyRef keyElement = new CsdlPropertyRef();
          keyElement.setName(property.getValue().getExternalName());
          keyList.add(keyElement);
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

  private void determineHasEtag() throws ODataJPAModelException {
    for (final Entry<String, IntermediateProperty> property : this.declaredPropertiesList.entrySet()) {
      if (property.getValue().isEtag()) {
        hasEtag = true;
        return;
      }
    }
    if (getBaseType() != null && getBaseType() instanceof IntermediateEntityType)
      hasEtag = ((IntermediateEntityType) getBaseType()).hasEtag();
  }

  private List<CsdlProperty> resolveEmbeddedId(final IntermediateEmbeddedIdProperty embeddedId)
      throws ODataJPAModelException {
    return ((IntermediateComplexType) embeddedId.getStructuredType()).getEdmItem().getProperties();
  }
}
