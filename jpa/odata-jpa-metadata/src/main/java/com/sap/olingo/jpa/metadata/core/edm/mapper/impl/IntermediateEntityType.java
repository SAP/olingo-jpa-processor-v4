package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nonnull;

import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.MappedSuperclassType;
import jakarta.persistence.metamodel.Type;

import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTopLevelElementRepresentation;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAQueryExtension;
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
  private Optional<Optional<JPAQueryExtension<EdmQueryExtensionProvider>>> extensionQueryProvider;
  private List<JPAAttribute> keyAttributes;
  private final boolean asTopLevelOnly;
  private final boolean asEntitySet;
  private final boolean asSingleton;

  IntermediateEntityType(final JPAEdmNameBuilder nameBuilder, final EntityType<T> et, final IntermediateSchema schema) {
    super(nameBuilder, et, schema);
    this.setExternalName(nameBuilder.buildEntityTypeName(et));
    asTopLevelOnly = determineAsTopLevelOnly();
    asEntitySet = determineAsEntitySet();
    asSingleton = determineAsSingleton();
    etagPath = Optional.empty();
    extensionQueryProvider = Optional.empty();
  }

  @Override
  public void addAnnotations(final List<CsdlAnnotation> annotations) {
    this.edmAnnotations.addAll(annotations);

  }

  @Override
  public CsdlAnnotation getAnnotation(final String alias, final String term) throws ODataJPAModelException {
    buildEdmTypeIfEmpty();
    return filterAnnotation(alias, term);
  }

  @Override
  public Optional<JPAAttribute> getAttribute(final String internalName) throws ODataJPAModelException {
    buildEdmTypeIfEmpty();
    final Optional<JPAAttribute> a = super.getAttribute(internalName);
    if (a.isPresent())
      return a;
    return getKey(internalName);
  }

  @Override
  public Optional<JPAAttribute> getAttribute(final UriResourceProperty uriResourceItem) throws ODataJPAModelException {
    buildEdmTypeIfEmpty();
    final Optional<JPAAttribute> a = super.getAttribute(uriResourceItem);
    if (a.isPresent())
      return a;
    return getKey(uriResourceItem);
  }

  @Override
  public JPACollectionAttribute getCollectionAttribute(final String externalName) throws ODataJPAModelException {
    final JPAPath path = getPath(externalName);
    if (path != null && path.getLeaf() instanceof final JPACollectionAttribute collectionAttribute)
      return collectionAttribute;
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
  public Optional<JPAAttribute> getDeclaredAttribute(@Nonnull final String internalName) throws ODataJPAModelException {
    final Optional<JPAAttribute> a = super.getDeclaredAttribute(internalName);
    if (a.isPresent())
      return a;
    return getKey(internalName);
  }

  @Override
  public JPAPath getEtagPath() throws ODataJPAModelException {
    if (hasEtag() && etagPath.isPresent())
      return etagPath.get();
    return null;
  }

  @Override
  public List<JPAAttribute> getKey() throws ODataJPAModelException {
    if (!hasBuildStepPerformed(PROPERTY_BUILD)) {
      // Properties need to be present otherwise they have to be build
      lazyBuildEdmItem();
    }
    if (keyAttributes == null) {
      final List<JPAAttribute> intermediateKey = new ArrayList<>(); // Cycle break
      addKeyAttribute(intermediateKey, Arrays.asList(this.getTypeClass().getDeclaredFields()));
      addKeyAttribute(intermediateKey, mappedSuperclass.stream()
          .map(ManagedType::getJavaType)
          .map(Class::getDeclaredFields)
          .flatMap(Arrays::stream)
          .toList());

      final IntermediateStructuredType<?> baseType = getBaseType();
      if (baseType != null) {
        intermediateKey.addAll(((IntermediateEntityType<?>) baseType).getKey());
      }
      keyAttributes = Collections.unmodifiableList(intermediateKey);
    }
    return Collections.unmodifiableList(keyAttributes);
  }

  @Override
  public List<JPAPath> getKeyPath() throws ODataJPAModelException {
    buildEdmTypeIfEmpty();
    final List<JPAPath> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : this.declaredPropertiesMap.entrySet()) {
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
  public Optional<JPAQueryExtension<EdmQueryExtensionProvider>> getQueryExtension() throws ODataJPAModelException {
    return extensionQueryProvider.orElse(determineExtensionQueryProvide());
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
    Table table = null;

    if (a != null)
      table = a.getAnnotation(Table.class);
    final IntermediateStructuredType<?> baseType = getBaseType();
    if (table == null && baseType != null)
      return ((IntermediateEntityType<?>) baseType).getTableName();
    return (table == null) ? jpaManagedType.getJavaType().getSimpleName().toUpperCase(Locale.ENGLISH)
        : buildFQTableName(table.schema(), table.name());
  }

  @Override
  public boolean hasCompoundKey() {
    final Type<?> idType = ((IdentifiableType<?>) jpaManagedType).getIdType();
    return jpaManagedType.getJavaType().getAnnotation(IdClass.class) != null
        || idType instanceof EmbeddableType;
  }

  @Override
  public boolean hasEtag() throws ODataJPAModelException {
    buildEdmTypeIfEmpty();
    return etagPath.isPresent();
  }

  @Override
  public boolean hasStream() throws ODataJPAModelException {
    buildEdmTypeIfEmpty();
    return this.determineHasStream();
  }

  @Override
  public boolean ignore() {
    return (asTopLevelOnly || super.ignore());
  }

  @Override
  public boolean isAbstract() {
    return determineAbstract();
  }

  @Override
  public List<JPAPath> searchChildPath(final JPAPath selectItemPath) {
    final List<JPAPath> result = new ArrayList<>();
    for (final JPAPath path : this.resolvedPathMap.values()) {
      if (!path.ignore() && path.getAlias().startsWith(selectItemPath.getAlias()))
        result.add(path);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <I extends CsdlAbstractEdmItem> List<I> extractEdmModelElements(
      final Map<?, ? extends IntermediateModelElement> mappingBuffer)
      throws ODataJPAModelException {

    final List<I> extractionTarget = new ArrayList<>();
    for (final IntermediateModelElement element : mappingBuffer.values()) {
      if (!element.ignore()
          // Skip Streams
          && !(element instanceof final IntermediateSimpleProperty simpleProperty
              && simpleProperty.isStream())) {
        if (element instanceof final IntermediateEmbeddedIdProperty embeddedId) {
          extractionTarget.addAll((Collection<? extends I>) resolveEmbeddedId(embeddedId));
        } else {
          extractionTarget.add((I) element.getEdmItem());
        }
      }
    }
    return returnNullIfEmpty(extractionTarget);
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmStructuralType == null) {
      buildPropertyList();
      buildNavigationPropertyList();
      addTransientProperties();
      addVirtualProperties();
      determineExtensionQueryProvide();
      postProcessor.processEntityType(this);
      retrieveAnnotations(this, Applicability.ENTITY_TYPE);
      edmStructuralType = new CsdlEntityType();
      edmStructuralType.setName(getExternalName());
      edmStructuralType.setProperties(extractEdmModelElements(declaredPropertiesMap));
      edmStructuralType.setNavigationProperties(extractEdmModelElements(
          declaredNavigationPropertiesMap));
      ((CsdlEntityType) edmStructuralType).setKey(extractEdmKeyElements());
      edmStructuralType.setAbstract(determineAbstract());
      edmStructuralType.setBaseType(determineBaseType());
      ((CsdlEntityType) edmStructuralType).setHasStream(determineHasStream());
      edmStructuralType.setAnnotations(determineAnnotations());
      determineHasEtag();
      checkPropertyConsistency(); //
      // TODO determine OpenType
    }
  }

  /**
   * The top level representation of this entity type is entity set.
   * @return
   */
  boolean asEntitySet() {
    return asEntitySet;
  }

  boolean asSingleton() {
    return asSingleton;
  }

  /**
   * This entity type represents only an entity set and not a entity type itself, but an alternative way to access the
   * superordinate entity type. See: {@link EdmAsEntitySet}
   * @return
   */
  boolean asTopLevelOnly() {
    return asTopLevelOnly;
  }

  /**
   *
   * @param dbCatalog
   * @param dbSchema
   * @param dbTableName
   * @return
   */
  boolean dbEquals(final String dbCatalog, final String dbSchema, @Nonnull final String dbTableName) {
    final AnnotatedElement a = jpaManagedType.getJavaType();
    Table table = null;
    if (a != null)
      table = a.getAnnotation(Table.class);
    if (table == null) {
      if (dbSchema != null && !dbSchema.isEmpty()) {
        return getTableName().equals(buildFQTableName(dbSchema, dbTableName));
      } else {
        return (dbCatalog == null || dbCatalog.isEmpty())
            && (dbSchema == null || dbSchema.isEmpty())
            && dbTableName.equals(getTableName());
      }
    } else
      return table.catalog().equals(dbCatalog)
          && table.schema().equals(dbSchema)
          && table.name().equals(dbTableName);
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
  List<CsdlPropertyRef> extractEdmKeyElements() throws ODataJPAModelException {

    return getKey().stream()
        .map(this::asPropertyRef)
        .toList();
  }

  @Override
  CsdlEntityType getEdmItem() throws ODataJPAModelException {
    buildEdmTypeIfEmpty();
    return (CsdlEntityType) edmStructuralType;
  }

  List<MappedSuperclassType<? super T>> getMappedSuperType() {
    return mappedSuperclass;
  }

  private void addKeyAttribute(final List<JPAAttribute> intermediateKey, final List<Field> keyFields)
      throws ODataJPAModelException {
    for (final Field candidate : keyFields) {
      final JPAAttribute attribute = this.declaredPropertiesMap.get(candidate.getName());
      if (attribute != null && attribute.isKey()) {
        if (attribute.isComplex()) {
          intermediateKey.addAll(buildEmbeddedIdKey(attribute));
        } else {
          intermediateKey.add(attribute);
        }
      }
    }
  }

  private CsdlPropertyRef asPropertyRef(final JPAAttribute idAttribute) {
    // TODO setAlias
    final CsdlPropertyRef keyElement = new CsdlPropertyRef();
    keyElement.setName(idAttribute.getExternalName());
    return keyElement;
  }

  private void buildEdmTypeIfEmpty() throws ODataJPAModelException {
    if (edmStructuralType == null) {
      lazyBuildEdmItem();
    }
  }

  private List<JPAAttribute> buildEmbeddedIdKey(final JPAAttribute attribute) throws ODataJPAModelException {

    final JPAStructuredType id = ((IntermediateEmbeddedIdProperty) attribute).getStructuredType();
    final List<JPAAttribute> keyElements = new ArrayList<>(id.getTypeClass().getDeclaredFields().length);
    final Field[] keyFields = id.getTypeClass().getDeclaredFields();
    for (int i = 0; i < keyFields.length; i++) {
      id.getAttribute(keyFields[i].getName()).ifPresent(keyElements::add);
    }
    return keyElements;
  }

  private List<CsdlAnnotation> determineAnnotations() throws ODataJPAModelException {
    getAnnotations(edmAnnotations, this.jpaManagedType.getJavaType(), internalName);
    return edmAnnotations;
  }

  private boolean determineAsEntitySet() {
    final Optional<EdmEntityType> jpaEntityType = getAnnotation(jpaJavaType, EdmEntityType.class);
    return !jpaEntityType.isPresent()
        || jpaEntityType.get().as() == EdmTopLevelElementRepresentation.AS_ENTITY_SET
        || jpaEntityType.get().as() == EdmTopLevelElementRepresentation.AS_ENTITY_SET_ONLY;
  }

  private boolean determineAsSingleton() {
    final EdmEntityType jpaEntityType = this.jpaManagedType.getJavaType().getAnnotation(EdmEntityType.class);
    return jpaEntityType != null && (jpaEntityType.as() == EdmTopLevelElementRepresentation.AS_SINGLETON
        || jpaEntityType.as() == EdmTopLevelElementRepresentation.AS_SINGLETON_ONLY);
  }

  private boolean determineAsTopLevelOnly() {
    final Optional<EdmEntityType> jpaEntityType = getAnnotation(jpaJavaType, EdmEntityType.class);
    return (jpaEntityType.isPresent()
        && (jpaEntityType.get().as() == EdmTopLevelElementRepresentation.AS_ENTITY_SET_ONLY
            || jpaEntityType.get().as() == EdmTopLevelElementRepresentation.AS_SINGLETON_ONLY));
  }

  @SuppressWarnings("unchecked")
  private Optional<JPAQueryExtension<EdmQueryExtensionProvider>> determineExtensionQueryProvide()
      throws ODataJPAModelException {
    extensionQueryProvider = Optional.of(Optional.empty());
    final Optional<EdmEntityType> jpaEntityType = getAnnotation(jpaJavaType, EdmEntityType.class);
    if (jpaEntityType.isPresent()) {
      final Class<EdmQueryExtensionProvider> provider = (Class<EdmQueryExtensionProvider>) jpaEntityType
          .get().extensionProvider();
      final Class<?> defaultProvider = EdmQueryExtensionProvider.class;
      if (provider != null && provider != defaultProvider)
        extensionQueryProvider = Optional.of(Optional.of(new JPAQueryExtensionProvider<>(
            provider)));
    }
    final IntermediateStructuredType<?> baseType = getBaseType();
    if (!extensionQueryProvider.get().isPresent() && baseType != null)
      extensionQueryProvider = Optional.ofNullable(((IntermediateEntityType<?>) baseType).getQueryExtension());
    return extensionQueryProvider.orElseGet(Optional::empty);
  }

  private void determineHasEtag() throws ODataJPAModelException {
    for (final Entry<String, IntermediateProperty> property : this.declaredPropertiesMap.entrySet()) {
      if (property.getValue().isEtag()) {
        etagPath = Optional.of(getPath(property.getValue().getExternalName(), false));
      }
    }
    if (getBaseType() instanceof final IntermediateEntityType<?> baseEntityType)
      etagPath = Optional.ofNullable(baseEntityType.getEtagPath());
  }

  private <A extends Annotation> Optional<A> getAnnotation(final Class<?> annotated, final Class<A> type) {
    return Optional.ofNullable(annotated.getAnnotation(type));
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

  private Optional<JPAAttribute> getKey(final UriResourceProperty uriResourceItem) throws ODataJPAModelException {
    for (final JPAAttribute attribute : getKey()) {
      if (attribute.getExternalName().equals(uriResourceItem.getProperty().getName()))
        return Optional.of(attribute);
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  private List<CsdlProperty> resolveEmbeddedId(final IntermediateEmbeddedIdProperty embeddedId)
      throws ODataJPAModelException {
    return ((IntermediateStructuredType<T>) embeddedId.getStructuredType()).getEdmItem().getProperties();
  }
}
