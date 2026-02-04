package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.IdClass;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumns;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.MappedSuperclassType;
import jakarta.persistence.metamodel.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlDynamicExpression;
import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTopLevelElementRepresentation;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEtagValidator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAInheritanceInformation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAInheritanceType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAQueryExtension;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCache;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCacheFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCacheSupplier;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.ListCacheSupplier;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;
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
  private static final Log LOGGER = LogFactory.getLog(IntermediateEntityType.class);
  private Optional<JPAPath> etagPath;
  private Optional<JPAEtagValidator> etagValidator;
  private final InstanceCache<JPAQueryExtension<EdmQueryExtensionProvider>> extensionQueryProvider;
  private final ListCacheSupplier<JPAAttribute> keyAttributes;
  private final ListCacheSupplier<String> userGroups;
  private final InstanceCache<IntermediateStructuredType<? super T>> baseType;
  private final boolean asTopLevelOnly;
  private final boolean asEntitySet;
  private final boolean asSingleton;
  private final InstanceCache<JPAInheritanceInformation> inheritanceType;

  IntermediateEntityType(final JPAEdmNameBuilder nameBuilder, final EntityType<T> et, final IntermediateSchema schema) {
    super(nameBuilder, et, schema);
    this.setExternalName(nameBuilder.buildEntityTypeName(et));
    asTopLevelOnly = determineAsTopLevelOnly();
    asEntitySet = determineAsEntitySet();
    asSingleton = determineAsSingleton();
    etagPath = Optional.empty();
    extensionQueryProvider = new InstanceCacheSupplier<>(this::determineExtensionQueryProvide);
    baseType = new InstanceCacheSupplier<>(this::determineBaseType);
    inheritanceType = new InstanceCacheSupplier<>(this::determineInheritanceInformation);
    keyAttributes = new ListCacheSupplier<>(this::buildKeyAttributes);
    userGroups = new ListCacheSupplier<>(this::determineUserGroups);
  }

  private IntermediateEntityType(final IntermediateEntityType<T> source, final List<String> requesterUserGroups,
      final boolean hideRestrictedProperties) throws ODataJPAModelException {
    // Navigations in complex properties need to be restricted as well
    super(source, requesterUserGroups, hideRestrictedProperties);
    this.setExternalName(source.getExternalName());
    asTopLevelOnly = source.asTopLevelOnly;
    asEntitySet = source.asEntitySet;
    asSingleton = source.asSingleton;
    etagPath = source.etagPath;
    extensionQueryProvider = source.extensionQueryProvider;
    userGroups = source.userGroups;
    baseType = new InstanceCacheFunction<>(this::baseTypeRestricted, source.getBaseType(),
        requesterUserGroups);
    inheritanceType = source.inheritanceType;
    etagPath = source.etagPath;
    etagValidator = source.etagValidator;
    keyAttributes = new ListCacheSupplier<>(this::buildKeyAttributes);
  }

  @Override
  public void addAnnotations(final List<CsdlAnnotation> annotations) {
    this.edmAnnotations.addAll(annotations);

  }

  @Override
  public CsdlAnnotation getAnnotation(final String alias, final String term) throws ODataJPAModelException {
    getEdmItem();
    return filterAnnotation(alias, term);
  }

  @Override
  public Object getAnnotationValue(final String alias, final String term, final String property)
      throws ODataJPAModelException {

    try {
      return Optional.ofNullable(getAnnotation(alias, term))
          .map(CsdlAnnotation::getExpression)
          .map(expression -> getAnnotationValue(property, expression))
          .orElse(null);
    } catch (final ODataJPAModelInternalException e) {
      throw (ODataJPAModelException) e.getCause();
    }
  }

  @Override
  protected Object getAnnotationDynamicValue(final String property, final CsdlDynamicExpression expression)
      throws ODataJPAModelInternalException {
    try {
      if (expression.isRecord()) {
        // This may create a problem if the property in question is a record itself. Currently non is supported in
        // standard
        final var propertyValue = findAnnotationPropertyValue(property, expression);
        if (propertyValue.isPresent()) {
          return getAnnotationValue(property, propertyValue.get());
        }
      } else if (expression.isCollection()) {
        return getAnnotationCollectionValue(expression);
      } else if (expression.isPropertyPath()) {
        return getPath(expression.asPropertyPath().getValue());
      } else if (expression.isNavigationPropertyPath()) {
        return getAssociationPath(expression.asNavigationPropertyPath().getValue());
      }
      return null;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  @Override
  public Optional<JPAAttribute> getAttribute(final String internalName) throws ODataJPAModelException {
    final var a = super.getAttribute(internalName);
    if (a.isPresent())
      return a;
    return getKey(internalName);
  }

  @Override
  public Optional<JPAAttribute> getAttribute(final UriResourceProperty uriResourceItem) throws ODataJPAModelException {
    final var a = super.getAttribute(uriResourceItem);
    if (a.isPresent())
      return a;
    return getKey(uriResourceItem);
  }

  @Override
  public JPACollectionAttribute getCollectionAttribute(final String externalName) throws ODataJPAModelException {
    final var path = getPath(externalName);
    if (path != null && path.getLeaf() instanceof final JPACollectionAttribute collectionAttribute)
      return collectionAttribute;
    return null;
  }

  @Override
  public String getContentType() throws ODataJPAModelException {
    return getStreamProperty()
        .map(IntermediateSimpleProperty::getContentType)
        .orElse("");
  }

  @Override
  public JPAPath getContentTypeAttributePath() throws ODataJPAModelException {

    final var propertyInternalName = getStreamProperty()
        .map(IntermediateSimpleProperty::getContentTypeProperty)
        .orElse("");
    if (propertyInternalName == null || propertyInternalName.isEmpty()) {
      return null;
    }
    // Ensure that Ignore is ignored
    return getPathByDBField(getProperty(propertyInternalName).getDBFieldName());
  }

  @Override
  public Optional<JPAAttribute> getDeclaredAttribute(@Nonnull final String internalName) throws ODataJPAModelException {
    final var a = super.getDeclaredAttribute(internalName);
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
  public JPAEtagValidator getEtagValidator() throws ODataJPAModelException {
    if (hasEtag())
      return etagValidator.orElse(null);
    return null;
  }

  @Override
  public List<JPAAttribute> getKey() throws ODataJPAModelException {
    return keyAttributes.get();
  }

  private List<JPAAttribute> buildKeyAttributes() {
    final List<JPAAttribute> intermediateKey = new ArrayList<>(); // Cycle break
    try {
      final var properties = buildPropertyList();
      addKeyAttribute(intermediateKey, Arrays.asList(this.getTypeClass().getDeclaredFields()), properties);

      addKeyAttribute(intermediateKey, mappedSuperclass.stream()
          .map(ManagedType::getJavaType)
          .map(Class::getDeclaredFields)
          .flatMap(Arrays::stream)
          .toList(), properties);

      final IntermediateStructuredType<? super T> type = getBaseType();
      if (type != null) {
        intermediateKey.addAll(((IntermediateEntityType<?>) type).getKey());
      }
      return Collections.unmodifiableList(updateAttributeListDbFieldName(intermediateKey));
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  @Override
  List<JPAAttribute> getBaseTypeAttributes() throws ODataJPAModelException {
    final IntermediateStructuredType<? super T> superType = getBaseType();
    if (superType != null) {
      final Map<String, PrimaryKeyJoinColumn> mappings = getPrimaryKeyJoinColumns();
      final List<JPAAttribute> result = new ArrayList<>();
      for (final var attribute : superType.getAttributes()) {
        result.add(updateAttributeDbFieldName(mappings, attribute));
      }
      return result;
    }
    return List.of();
  }

  private final JPAAttribute updateAttributeDbFieldName(final Map<String, PrimaryKeyJoinColumn> mappings,
      final JPAAttribute attribute) throws ODataJPAModelException {

    if (attribute instanceof final IntermediateSimpleProperty property
        && mappings.containsKey(property.getDBFieldName())) {
      LOGGER.debug("Key mapping (PrimaryKeyJoinColumn) found for " + jpaJavaType.getSimpleName() + "#" + property
          .getInternalName());
      final var targetDbName = mappings.get(property.getDBFieldName()).name();
      if (targetDbName != null && !targetDbName.isBlank())
        return new IntermediateSimpleProperty(property,
            mappings.get(property.getDBFieldName()).name(),
            hideRestrictedProperties);
      else
        LOGGER.warn("Missing 'name' at annotation PrimaryKeyJoinColumn mapping found for " + jpaJavaType
            .getSimpleName() + "#" + property.getInternalName());
    }
    return attribute;
  }

  private final List<JPAAttribute> updateAttributeListDbFieldName(final List<JPAAttribute> intermediateKey)
      throws ODataJPAModelException {

    final Map<String, PrimaryKeyJoinColumn> mappings = getPrimaryKeyJoinColumns();
    for (int i = 0; i < intermediateKey.size(); i++) {
      intermediateKey.set(i, updateAttributeDbFieldName(mappings, intermediateKey.get(i)));
    }
    return intermediateKey;
  }

  private final Map<String, PrimaryKeyJoinColumn> getPrimaryKeyJoinColumns() throws ODataJPAModelException {
    final Map<String, PrimaryKeyJoinColumn> mappings;
    if (this.jpaJavaType.getAnnotation(PrimaryKeyJoinColumn.class) != null) {
      final var mapping = this.jpaJavaType.getAnnotation(PrimaryKeyJoinColumn.class);
      final var referenceColumn = !mapping.referencedColumnName().isEmpty()
          ? mapping.referencedColumnName()
          : ((JPAEntityType) getBaseType()).getKeyPath().get(0).getDBFieldName();
      mappings = Map.of(referenceColumn, this.jpaJavaType.getAnnotation(PrimaryKeyJoinColumn.class));
    } else if (jpaJavaType.getAnnotation(PrimaryKeyJoinColumns.class) != null)
      mappings = Arrays.asList(this.jpaJavaType.getAnnotation(PrimaryKeyJoinColumns.class).value())
          .stream()
          .collect(toMap(PrimaryKeyJoinColumn::referencedColumnName, Function.identity()));
    else
      mappings = Map.of();
    return mappings;
  }

  @Override
  public List<JPAPath> getKeyPath() throws ODataJPAModelException {
    final List<JPAPath> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : this.getDeclaredPropertiesMap().entrySet()) {
      final JPAAttribute attribute = property.getValue();
      if (attribute instanceof IntermediateEmbeddedIdProperty) {
        result.add(getIntermediatePathMap().get(attribute.getExternalName()));
      } else if (attribute.isKey()) {
        result.add(getResolvedPathMap().get(attribute.getExternalName()));
      }
    }
    final IntermediateStructuredType<?> type = getBaseType();
    if (type != null) {
      result.addAll(updatePathListDbFieldName(((IntermediateEntityType<?>) type).getKeyPath()));
    }
    return Collections.unmodifiableList(result);
  }

  private List<JPAPath> updatePathListDbFieldName(final List<JPAPath> keyPath)
      throws ODataJPAModelException {

    final Map<String, PrimaryKeyJoinColumn> mappings = getPrimaryKeyJoinColumns();
    final List<JPAPath> result = new ArrayList<>();
    for (int i = 0; i < keyPath.size(); i++) {
      final var path = keyPath.get(i);
      result.add(updatePathDbFieldName(mappings, path));
    }
    return result;
  }

  private final JPAPath updatePathDbFieldName(final Map<String, PrimaryKeyJoinColumn> mappings, final JPAPath path)
      throws ODataJPAModelException {

    if (mappings.containsKey(path.getDBFieldName())) {
      LOGGER.debug("Key mapping (PrimaryKeyJoinColumn) found for " + jpaJavaType.getSimpleName() + "#" + path
          .getLeaf().getInternalName());
      final var targetDbName = mappings.get(path.getDBFieldName()).name();
      if (targetDbName != null && !targetDbName.isBlank()) {
        return new JPAPathImpl(path.getAlias(), mappings.get(path.getDBFieldName()).name(), path.getPath());
      } else
        LOGGER.warn("Missing 'name' at annotation PrimaryKeyJoinColumn mapping found for " + jpaJavaType
            .getSimpleName() + "#" + path.getLeaf().getInternalName());
    }
    return path;
  }

  @Override
  protected Map<String, JPAPath> getBaseTypeResolvedPathMap() throws ODataJPAModelException {
    final IntermediateStructuredType<? super T> superType = getBaseType();
    if (superType != null) {
      final Map<String, PrimaryKeyJoinColumn> mappings = getPrimaryKeyJoinColumns();
      final Map<String, JPAPath> result = new HashMap<>();
      for (final var path : superType.getResolvedPathMap().entrySet()) {
        result.put(path.getKey(), updatePathDbFieldName(mappings, path.getValue()));
      }
      return result;
    }
    return Map.of();
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
    return extensionQueryProvider.get();
  }

  @Override
  public List<JPAPath> getSearchablePath() throws ODataJPAModelException {
    final var allPath = getPathList();
    final List<JPAPath> searchablePath = new ArrayList<>();
    for (final JPAPath p : allPath) {
      if (p.getLeaf().isSearchable())
        searchablePath.add(p);
    }
    return searchablePath;
  }

  @Override
  public JPAPath getStreamAttributePath() throws ODataJPAModelException {
    final var externalName = getStreamProperty()
        .map(IntermediateSimpleProperty::getExternalName)
        .orElse(null);
    return externalName == null ? null : getPath(externalName);
  }

  @Override
  public String getTableName() {

    try {
      final AnnotatedElement a = jpaManagedType.getJavaType();
      Table table = null;

      if (a != null)
        table = a.getAnnotation(Table.class);
      final IntermediateStructuredType<?> type = getBaseType();
      if (table == null && type != null)
        return ((IntermediateEntityType<?>) type).getTableName();
      return (table == null) ? jpaManagedType.getJavaType().getSimpleName().toUpperCase(Locale.ENGLISH)
          : buildFQTableName(table.schema(), table.name());
    } catch (final ODataJPAModelException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public boolean hasCompoundKey() {
    final Type<?> idType = ((IdentifiableType<?>) jpaManagedType).getIdType();
    return jpaManagedType.getJavaType().getAnnotation(IdClass.class) != null
        || idType instanceof EmbeddableType;
  }

  @Override
  public boolean hasEmbeddedKey() {
    return ((IdentifiableType<?>) jpaManagedType).hasSingleIdAttribute()
        && hasCompoundKey();
  }

  @Override
  public boolean hasEtag() throws ODataJPAModelException {
    getEdmItem();
    return etagPath.isPresent();
  }

  @Override
  public boolean hasStream() throws ODataJPAModelException {
    getEdmItem();
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
  public List<String> getUserGroups() throws ODataJPAModelException {
    return userGroups.get();
  }

  @Override
  public IntermediateStructuredType<? super T> getBaseType() throws ODataJPAModelException { // NOSONAR
    return baseType.get().orElse(null);
  }

  @Override
  public JPAInheritanceInformation getInheritanceInformation() throws ODataJPAModelException {
    return inheritanceType.get().orElse(new NoInheritance());
  }

  /**
   * Determines if the structured type has a super type that will be part of OData metadata. That is, the method will
   * return null in case the entity has a MappedSuperclass.
   * @return Determined super type or null
   */

  private IntermediateStructuredType<? super T> determineBaseType() { // NOSONAR
    final Class<?> superType = jpaManagedType.getJavaType().getSuperclass();
    if (superType != null) {
      @SuppressWarnings("unchecked")
      final IntermediateStructuredType<? super T> baseEntity = (IntermediateStructuredType<? super T>) schema
          .getEntityType(superType);
      if (baseEntity != null)
        return baseEntity;
    }
    return null;
  }

  private JPAInheritanceInformation determineInheritanceInformation() {
    final Class<?> superType = jpaManagedType.getJavaType().getSuperclass();
    final var mappedSuperClass = superType.getAnnotation(MappedSuperclass.class);
    if (superType != Object.class && mappedSuperClass == null) {
      @SuppressWarnings("unchecked")
      final IntermediateEntityType<? super T> baseEntity = (IntermediateEntityType<? super T>) schema
          .getEntityType(superType);
      Objects.requireNonNull(baseEntity);
      try {
        return copyInheritanceInfo(baseEntity);
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAModelInternalException(e);
      }
    } else {
      return determineInheritanceType(jpaManagedType.getJavaType());
    }
  }

  private JPAInheritanceInformation determineInheritanceType(final Class<?> clazz) {
    final var inheritance = clazz.getAnnotation(Inheritance.class);
    if (inheritance != null && inheritance.strategy() == InheritanceType.JOINED) {
      return new IntermediateInheritanceInformationJoinTable(List.of());
    }

    final var discriminator = clazz.getAnnotation(DiscriminatorColumn.class);
    if (discriminator != null)
      return new IntermediateInheritanceInformationSingleTable();
    return new NoInheritance();
  }

  private JPAInheritanceInformation copyInheritanceInfo(final JPAEntityType baseEntity) throws ODataJPAModelException {
    final var inheritanceInfo = baseEntity.getInheritanceInformation();
    return inheritanceInfo.getInheritanceType() == JPAInheritanceType.JOIN_TABLE
        ? new IntermediateInheritanceInformationJoinTable(buildInheritanceJoinColumns(baseEntity))
        : inheritanceInfo;
  }

  private List<JPAOnConditionItem> buildInheritanceJoinColumns(final JPAEntityType baseEntity)
      throws ODataJPAModelException {
    final Map<String, PrimaryKeyJoinColumn> mappings = getPrimaryKeyJoinColumns();
    final Map<String, JPAPath> subKeyPath = getKeyPath().stream().collect(toMap(JPAPath::getDBFieldName, Function
        .identity()));
    final List<JPAOnConditionItem> result = new ArrayList<>();
    for (final var superPath : baseEntity.getKeyPath()) {
      if (mappings.containsKey(superPath.getDBFieldName())) {
        final var subDbFieldName = mappings.get(superPath.getDBFieldName()).name();
        result.add(new JPAOnConditionItemImpl(subKeyPath.get(subDbFieldName), superPath));
      } else {
        result.add(new JPAOnConditionItemImpl(subKeyPath.get(superPath.getDBFieldName()), superPath));
      }
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

  @SuppressWarnings("unchecked")
  @Override
  protected <X extends IntermediateModelElement> X asUserGroupRestricted(final List<String> userGroups,
      final boolean hideRestrictedProperties) throws ODataJPAModelException {
    return (X) new IntermediateEntityType<>(this, userGroups, hideRestrictedProperties);
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    getEdmItem();
  }

  @Override
  synchronized CsdlEntityType buildEdmItem() {
    try {
      final var edmEntityType = createEdmItem();
      postProcessingBuildEdmItem();
      checkPropertyConsistency();
      return edmEntityType;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }

  }

  @Override
  protected synchronized Map<String, IntermediateProperty> buildCompletePropertyMap() {
    try {
      final Map<String, IntermediateProperty> result = new HashMap<>();
      result.putAll(buildPropertyList());
      result.putAll(addDescriptionProperty());
      result.putAll(addTransientProperties());
      result.putAll(addVirtualProperties(result));
      return result;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private CsdlEntityType createEdmItem() throws ODataJPAModelException {
    final var edmEntityType = new CsdlEntityType();
    determineHasEtag();
    edmEntityType.setName(getExternalName());
    edmEntityType.setProperties(extractEdmModelElements(getDeclaredPropertiesMap()));
    edmEntityType.setNavigationProperties(extractEdmModelElements(getDeclaredNavigationPropertiesMap()));
    edmEntityType.setKey(extractEdmKeyElements());
    edmEntityType.setAbstract(determineAbstract());
    edmEntityType.setBaseType(determineBaseTypeFqn());
    edmEntityType.setHasStream(determineHasStream());
    edmEntityType.setAnnotations(determineAnnotations());
    return edmEntityType;
  }

  private void postProcessingBuildEdmItem() {

    determineExtensionQueryProvide();
    postProcessor.processEntityType(this);
    retrieveAnnotations(this, Applicability.ENTITY_TYPE);
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
   * @throws ODataJPAModelException
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
    final var modifiers = jpaManagedType.getJavaType().getModifiers();
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
    return (CsdlEntityType) super.getEdmItem();
  }

  List<MappedSuperclassType<? super T>> getMappedSuperType() {
    return mappedSuperclass;
  }

  private void addKeyAttribute(final List<JPAAttribute> intermediateKey, final List<Field> keyFields,
      final Map<String, IntermediateProperty> properties)
      throws ODataJPAModelException {
    for (final Field candidate : keyFields) {
      final JPAAttribute attribute = properties.get(candidate.getName());
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
    final var keyElement = new CsdlPropertyRef();
    keyElement.setName(idAttribute.getExternalName());
    return keyElement;
  }

  private List<JPAAttribute> buildEmbeddedIdKey(final JPAAttribute attribute) throws ODataJPAModelException {

    final var id = ((IntermediateEmbeddedIdProperty) attribute).getStructuredType();
    final List<JPAAttribute> keyElements = new ArrayList<>(id.getTypeClass().getDeclaredFields().length);
    final var keyFields = id.getTypeClass().getDeclaredFields();
    for (var i = 0; i < keyFields.length; i++) {
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
    final var jpaEntityType = this.jpaManagedType.getJavaType().getAnnotation(EdmEntityType.class);
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
  private JPAQueryExtension<EdmQueryExtensionProvider> determineExtensionQueryProvide() {
    try {
      JPAQueryExtension<EdmQueryExtensionProvider> provider = null;
      final Optional<EdmEntityType> jpaEntityType = getAnnotation(jpaJavaType, EdmEntityType.class);
      if (jpaEntityType.isPresent()) {
        final var providerClass = (Class<EdmQueryExtensionProvider>) jpaEntityType
            .get().extensionProvider();
        final Class<?> defaultProvider = EdmQueryExtensionProvider.class;
        if (providerClass != null && providerClass != defaultProvider)
          provider = new JPAQueryExtensionProvider<>(providerClass);
      }
      final IntermediateStructuredType<?> type = getBaseType();
      if (provider == null && type != null)
        provider = ((IntermediateEntityType<?>) type).getQueryExtension().orElse(null);
      return provider;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private void determineHasEtag() throws ODataJPAModelException {
    for (final Entry<String, IntermediateProperty> property : this.getDeclaredPropertiesMap().entrySet()) {
      if (property.getValue().isEtag()) {
        etagPath = Optional.of(getPath(property.getValue().getExternalName(), false));
        etagValidator = Optional.of(Number.class.isAssignableFrom(property.getValue().getJavaType())
            ? JPAEtagValidator.STRONG : JPAEtagValidator.WEAK);
      }
    }
    if (getBaseType() instanceof final IntermediateEntityType<?> baseEntityType) {
      etagPath = Optional.ofNullable(baseEntityType.getEtagPath());
      etagValidator = Optional.ofNullable(baseEntityType.getEtagValidator());
    }
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

  private List<String> determineUserGroups() {
    final var edmEntityType = this.jpaJavaType.getAnnotation(EdmEntityType.class);
    if (edmEntityType != null && edmEntityType.visibleFor() != null)
      return Arrays.stream(edmEntityType.visibleFor().value()).toList();
    else
      return List.of();
  }

  private static class NoInheritance implements JPAInheritanceInformation {

    @Override
    public List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException {
      return List.of();
    }

    @Override
    public List<JPAOnConditionItem> getReversedJoinColumnsList() throws ODataJPAModelException {
      return List.of();
    }

  }
}
