package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MappedSuperclassType;
import javax.persistence.metamodel.Type;

import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAsEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;

/**
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397974"
 * >OData Version 4.0 Part 3 - 8 Entity Type</a>
 *
 * @param <T> Java type the entity type base on.
 * @author Oliver Grande
 *
 */
final class IntermediateEntityType<T> extends IntermediateStructuredType<T> implements JPAEntityType,
    IntermediateEntityTypeAccess {
  private Optional<JPAPath> etagPath;
  private List<JPAAttribute> keyAttributes;
  private final boolean asEntitySet;

  IntermediateEntityType(final JPAEdmNameBuilder nameBuilder, final EntityType<T> et, final IntermediateSchema schema) {
    super(nameBuilder, et, schema);
    this.setExternalName(nameBuilder.buildEntityTypeName(et));
    asEntitySet = determineAsEntitySet();
    etagPath = Optional.empty();
  }

  @Override
  public void addAnnotations(final List<CsdlAnnotation> annotations) {
    this.edmAnnotations.addAll(annotations);

  }

  @Override
  public Optional<JPAAttribute> getAttribute(final String internalName) throws ODataJPAModelException {
    if (edmStructuralType == null) {
      lazyBuildEdmItem();
    }
    final Optional<JPAAttribute> a = super.getAttribute(internalName);
    if (a.isPresent())
      return a;
    return getKey(internalName);
  }

  @Override
  public JPACollectionAttribute getCollectionAttribute(final String externalName) throws ODataJPAModelException {
    final JPAPath path = getPath(externalName);
    if (path != null && path.getLeaf() instanceof JPACollectionAttribute)
      return (JPACollectionAttribute) path.getLeaf();
    return null;
  }

  @Override
  public Optional<JPAAttribute> getDeclaredAttribute(@Nonnull final String internalName) throws ODataJPAModelException {
    final Optional<JPAAttribute> a = super.getDeclaredAttribute(internalName);
    if (a.isPresent())
      return a;
    return getKey(internalName);
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
  public JPAPath getEtagPath() throws ODataJPAModelException {
    if (hasEtag() && etagPath.isPresent())
      return etagPath.get();
    return null;
  }

  @Override
  public List<JPAAttribute> getKey() throws ODataJPAModelException {
    if (edmStructuralType == null) {
      lazyBuildEdmItem();
    }
    if (keyAttributes == null) {
      final List<JPAAttribute> intermediateKey = new ArrayList<>(); // Cycle break
      addKeyAttribute(intermediateKey, this.getTypeClass().getDeclaredFields());
      addKeyAttribute(intermediateKey, mappedSuperclass
          .map(ManagedType::getJavaType)
          .map(Class::getDeclaredFields)
          .orElse(new Field[] {}));
      final IntermediateStructuredType<?> baseType = getBaseType();
      if (baseType != null) {
        intermediateKey.addAll(((IntermediateEntityType<?>) baseType).getKey());
      }
      keyAttributes = Collections.unmodifiableList(intermediateKey);
    }
    return keyAttributes;
  }

  private void addKeyAttribute(final List<JPAAttribute> intermediateKey, final Field[] keyFields)
      throws ODataJPAModelException {
    for (int i = keyFields.length - 1; i >= 0; i--) {
      final JPAAttribute attribute = this.declaredPropertiesList.get(keyFields[i].getName());
      if (attribute != null && attribute.isKey()) {
        if (attribute.isComplex()) {
          intermediateKey.addAll(buildEmbeddedIdKey(attribute));
        } else {
          intermediateKey.add(attribute);
        }
      }
    }
  }

  @Override
  public List<JPAPath> getKeyPath() throws ODataJPAModelException {
    if (edmStructuralType == null) {
      lazyBuildEdmItem();
    }
    final List<JPAPath> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : this.declaredPropertiesList.entrySet()) {
      final JPAAttribute attribute = property.getValue();
      if (attribute instanceof IntermediateEmbeddedIdProperty) {
        result.add(intermediatePathMap.get(attribute.getExternalName()));
      } else if (attribute.isKey()) {
        result.add(resolvedPathMap.get(attribute.getExternalName()));
      }
    }
    final IntermediateStructuredType<?> baseType = getBaseType();
    if (baseType != null) {
      result.addAll(((IntermediateEntityType<?>) baseType).getKeyPath());
    }
    return Collections.unmodifiableList(result);
  }

  @Override
  public Class<?> getKeyType() {
    if (jpaManagedType instanceof IdentifiableType<?>) {
      Class<?> idClass = null;
      final Type<?> idType = ((IdentifiableType<?>) jpaManagedType).getIdType();

      if (idType == null)
        // Hibernate does not return an IdType in case of compound key that do not use
        // EmbeddableId. So fallback to hand made evaluation
        idClass = jpaManagedType.getJavaType().getAnnotation(IdClass.class).value();
      else
        idClass = idType.getJavaType();
      return idClass;
    } else {
      return null;
    }
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
    final IntermediateStructuredType<?> baseType = getBaseType();
    if (t == null && baseType != null)
      return ((IntermediateEntityType<?>) baseType).getTableName();
    return (t == null) ? jpaManagedType.getJavaType().getSimpleName().toUpperCase(Locale.ENGLISH)
        : buildFQTableName(t.schema(), t.name());
  }

  @Override
  public boolean hasCompoundKey() {
    final Type<?> idType = ((IdentifiableType<?>) jpaManagedType).getIdType();
    return jpaManagedType.getJavaType().getAnnotation(IdClass.class) != null
        || idType instanceof EmbeddableType;
  }

  @Override
  public boolean hasEtag() throws ODataJPAModelException {
    if (edmStructuralType == null) {
      lazyBuildEdmItem();
    }
    return etagPath.isPresent();
  }

  @Override
  public boolean hasStream() throws ODataJPAModelException {
    if (edmStructuralType == null) {
      lazyBuildEdmItem();
    }
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
  protected <I extends CsdlAbstractEdmItem> List<I> extractEdmModelElements(
      final Map<String, ? extends IntermediateModelElement> mappingBuffer)
      throws ODataJPAModelException {

    final List<I> extractionTarget = new ArrayList<>();
    for (final Entry<String, ? extends IntermediateModelElement> element : mappingBuffer.entrySet()) {
      if (!element.getValue().ignore()
          // Skip Streams
          && !(element.getValue() instanceof IntermediateSimpleProperty &&
              ((IntermediateSimpleProperty) element.getValue()).isStream())) {
        if (element.getValue() instanceof IntermediateEmbeddedIdProperty) {
          extractionTarget.addAll((Collection<? extends I>) resolveEmbeddedId(
              (IntermediateEmbeddedIdProperty) element.getValue()));
        } else {
          extractionTarget.add((I) element.getValue().getEdmItem());
        }
      }
    }
    return returnNullIfEmpty(extractionTarget);
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmStructuralType == null) {
      buildPropertyList();
      buildNaviPropertyList();
      addTransientProperties();
      postProcessor.processEntityType(this);

      edmStructuralType = new CsdlEntityType();
      edmStructuralType.setName(getExternalName());
      edmStructuralType.setProperties(extractEdmModelElements(declaredPropertiesList));
      edmStructuralType.setNavigationProperties(extractEdmModelElements(
          declaredNaviPropertiesList));
      ((CsdlEntityType) edmStructuralType).setKey(extractEdmKeyElements(declaredPropertiesList));
      edmStructuralType.setAbstract(determineAbstract());
      edmStructuralType.setBaseType(determineBaseType());
      ((CsdlEntityType) edmStructuralType).setHasStream(determineHasStream());
      edmStructuralType.setAnnotations(determineAnnotations());
      determineHasEtag();
      checkPropertyConsistancy();
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
          final List<JPAAttribute> idAttributes = ((IntermediateComplexType<?>) property.getValue()
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

  Optional<MappedSuperclassType<? super T>> getMappedSuperType() {
    return mappedSuperclass;
  }

  @Override
  CsdlEntityType getEdmItem() throws ODataJPAModelException {
    if (edmStructuralType == null) {
      lazyBuildEdmItem();
    }
    return (CsdlEntityType) edmStructuralType;
  }

  private List<JPAAttribute> buildEmbeddedIdKey(final JPAAttribute attribute) throws ODataJPAModelException {

    final JPAStructuredType id = ((IntermediateEmbeddedIdProperty) attribute).getStructuredType();
    final List<JPAAttribute> keyElements = new ArrayList<>(id.getTypeClass().getDeclaredFields().length);
    final Field[] keyFields = id.getTypeClass().getDeclaredFields();
    for (int i = keyFields.length - 1; i >= 0; i--) {
      id.getAttribute(keyFields[i].getName()).ifPresent(keyElements::add);
    }
    return keyElements;
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
        etagPath = Optional.of(getPath(property.getValue().getExternalName(), false));
      }
    }
    if (getBaseType() instanceof IntermediateEntityType)
      etagPath = Optional.ofNullable(((IntermediateEntityType<?>) getBaseType()).getEtagPath());
  }

  private Optional<JPAAttribute> getKey(final String internalName) throws ODataJPAModelException {
    if (internalName == null)
      return Optional.empty();
    for (final JPAAttribute attribute : getKey()) {
      if (internalName.equals(attribute.getInternalName()))
        return Optional.of(attribute);
    }
    return Optional.empty();
  }

  private List<CsdlProperty> resolveEmbeddedId(final IntermediateEmbeddedIdProperty embeddedId)
      throws ODataJPAModelException {
    return ((IntermediateComplexType<?>) embeddedId.getStructuredType()).getEdmItem().getProperties();
  }
}
