package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.COMPLEX_PROPERTY_WRONG_PROTECTION_PATH;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.DB_TYPE_NOT_DETERMINED;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.INVALID_NAVIGATION_PROPERTY;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.PROPERTY_REQUIRED_UNKNOWN;
import static jakarta.persistence.metamodel.Attribute.PersistentAttributeType.BASIC;
import static jakarta.persistence.metamodel.Attribute.PersistentAttributeType.ELEMENT_COLLECTION;
import static jakarta.persistence.metamodel.Attribute.PersistentAttributeType.EMBEDDED;
import static jakarta.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_MANY;
import static jakarta.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_ONE;
import static jakarta.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_MANY;
import static jakarta.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_ONE;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AssociationOverrides;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.MappedSuperclassType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlStructuralType;
import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssociation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataNavigationPath;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPathNotFoundException;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPropertyPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCache;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCacheSupplier;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.ListCacheSupplier;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.MapCache;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.MapCacheFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.MapCacheSupplier;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

abstract class IntermediateStructuredType<T> extends IntermediateModelElement implements JPAStructuredType,
    ODataAnnotatable {
//
  private static final Log LOGGER = LogFactory.getLog(IntermediateStructuredType.class);
  private final MapCache<String, IntermediateProperty> declaredPropertiesMap;
  private final MapCache<String, IntermediateNavigationProperty<?>> declaredNavigationPropertiesMap;
  private final MapCache<String, JPAPath> resolvedPathMap;
  private final MapCache<String, JPAPath> intermediatePathMap;
  private final MapCache<String, JPAAssociationPathImpl> resolvedAssociationPathMap;
  private final InstanceCache<CsdlStructuralType> edmStructuralType;
  protected final ManagedType<T> jpaManagedType;
  protected final Class<T> jpaJavaType;
  protected final List<MappedSuperclassType<? super T>> mappedSuperclass;
  protected final IntermediateSchema schema;
  protected final ListCacheSupplier<JPAProtectionInfo> protectedAttributes;
  private Optional<List<IntermediateSimpleProperty>> streamProperty;
  private int buildState = 0;

  IntermediateStructuredType(final JPAEdmNameBuilder nameBuilder, final ManagedType<T> jpaManagedType,
      final IntermediateSchema schema) {

    super(nameBuilder, InternalNameBuilder.buildStructuredTypeName(jpaManagedType.getJavaType()), schema
        .getAnnotationInformation(), false);
    this.resolvedPathMap = new MapCacheSupplier<>(this::buildCompletePathMap);
    this.intermediatePathMap = new MapCacheSupplier<>(this::buildIntermediatePathMap);
    this.resolvedAssociationPathMap = new MapCacheSupplier<>(this::buildCompleteAssociationPathMap);
    this.protectedAttributes = new ListCacheSupplier<>(this::lazyBuildCompleteProtectionList);
    this.declaredNavigationPropertiesMap = new MapCacheSupplier<>(this::buildNavigationPropertyList);
    this.declaredPropertiesMap = new MapCacheSupplier<>(this::buildCompletePropertyMap);
    this.edmStructuralType = new InstanceCacheSupplier<>(this::buildEdmItem);

    this.jpaManagedType = jpaManagedType;
    this.jpaJavaType = this.jpaManagedType.getJavaType();
    this.schema = schema;
    this.mappedSuperclass = determineMappedSuperclass(jpaManagedType);
    this.streamProperty = Optional.empty();
    determineIgnore();
  }

  IntermediateStructuredType(final IntermediateStructuredType<T> source, final List<String> requesterUserGroups)
      throws ODataJPAModelException {
    super(source.nameBuilder, source.getInternalName(), source.schema.getAnnotationInformation(), true);
    edmAnnotations.addAll(source.edmAnnotations);

    this.jpaManagedType = source.jpaManagedType;
    this.jpaJavaType = this.jpaManagedType.getJavaType();
    this.schema = source.schema;
    this.mappedSuperclass = determineMappedSuperclass(jpaManagedType);
    this.streamProperty = Optional.empty();
    determineIgnore();

    this.protectedAttributes = new ListCacheSupplier<>(this::lazyBuildCompleteProtectionList);
    this.resolvedPathMap = new MapCacheFunction<>(this::restrictedPathMap, source.getResolvedPathMap(),
        requesterUserGroups);
    this.intermediatePathMap = new MapCacheFunction<>(this::restrictedIntermediatePathMap, source
        .getIntermediatePathMap(),
        requesterUserGroups);
    this.resolvedAssociationPathMap = new MapCacheFunction<>(this::extractNavigationPropertyPathElements, source
        .getResolvedAssociationPathMap(), requesterUserGroups);
    this.declaredNavigationPropertiesMap = new MapCacheFunction<>(this::extractNavigationPropertiesElements, source
        .getDeclaredNavigationPropertiesMap(), requesterUserGroups);
    this.declaredPropertiesMap = new MapCacheFunction<>(this::extractPropertiesElements, source
        .getDeclaredPropertiesMap(), requesterUserGroups);
    this.edmStructuralType = new InstanceCacheSupplier<>(this::buildEdmItem);
  }

  @Override
  public JPAAssociationAttribute getAssociation(final String internalName) throws ODataJPAModelException {
    for (final JPAAttribute attribute : this.getAssociations()) {
      if (attribute.getInternalName().equals(internalName))
        return (JPAAssociationAttribute) attribute;
    }
    return null;
  }

  @Override
  public JPAAssociationPath getAssociationPath(final String externalName) throws ODataJPAModelException {
    return getResolvedAssociationPathMap().get(externalName);
  }

  @Override
  public List<JPAAssociationPath> getAssociationPathList() throws ODataJPAModelException {

    final List<JPAAssociationPath> associationList = new ArrayList<>();

    for (final Entry<String, JPAAssociationPathImpl> associationPat : getResolvedAssociationPathMap().entrySet()) {
      associationList.add(associationPat.getValue());
    }
    return associationList;
  }

  @Override
  public Optional<JPAAttribute> getAttribute(final String internalName) throws ODataJPAModelException {
    return getAttribute(internalName, true);
  }

  @Override
  public Optional<JPAAttribute> getAttribute(final String internalName, final boolean respectIgnore)
      throws ODataJPAModelException {
    Optional<JPAAttribute> result = Optional.ofNullable(getDeclaredPropertiesMap().get(internalName));
    final IntermediateStructuredType<?> baseType = getBaseType();
    if (!result.isPresent() && baseType != null)
      result = baseType.getAttribute(internalName);
    else if (result.isPresent() && respectIgnore && ((IntermediateModelElement) result.get()).ignore())
      return Optional.empty();
    return result;
  }

  @Override
  public Optional<JPAAttribute> getAttribute(final UriResourceProperty uriResourceItem) throws ODataJPAModelException {
    final String externalName = uriResourceItem.getProperty().getName();
    for (final Entry<String, IntermediateProperty> property : getDeclaredPropertiesMap().entrySet()) {
      if (property.getValue().getExternalName().equals(externalName))
        return Optional.of(property.getValue());
    }
    final IntermediateStructuredType<?> baseType = getBaseType();
    if (baseType != null)
      return baseType.getAttribute(uriResourceItem);
    return Optional.empty();
  }

  @Override
  public List<JPAAttribute> getAttributes() throws ODataJPAModelException {
    final List<JPAAttribute> result = new ArrayList<>();
    result.addAll(getOwnProperties());
    result.addAll(getBaseTypeAttributes());
    return result;
  }

  abstract List<JPAAttribute> getBaseTypeAttributes() throws ODataJPAModelException;

  private List<JPAAttribute> getOwnProperties() throws ODataJPAModelException {
    final List<JPAAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : getDeclaredPropertiesMap().entrySet()) {
      final IntermediateProperty attribute = property.getValue();
      if (!attribute.ignore())
        result.add(attribute);
    }
    return result;
  }

  @Override
  public List<JPAPath> getCollectionAttributesPath() throws ODataJPAModelException {
    final List<JPAPath> pathList = new ArrayList<>();
    for (final Entry<String, JPAPath> path : getResolvedPathMap().entrySet()) {
      if (!path.getValue().ignore() && path.getValue().getLeaf() instanceof JPACollectionAttribute)
        pathList.add(path.getValue());
    }
    for (final Entry<String, JPAPath> path : getIntermediatePathMap().entrySet()) {
      if (!path.getValue().ignore() && path.getValue().getLeaf() instanceof JPACollectionAttribute)
        pathList.add(path.getValue());
    }
    return pathList;
  }

  @Override
  public List<JPAAssociationAttribute> getDeclaredAssociations() throws ODataJPAModelException {
    final List<JPAAssociationAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateNavigationProperty<?>> naviProperty : getDeclaredNavigationPropertiesMap()
        .entrySet())
      result.add(naviProperty.getValue());
    final IntermediateStructuredType<? super T> baseType = getBaseType();
    if (baseType != null)
      result.addAll(baseType.getDeclaredAssociations());
    return result;
  }

  @Override
  public Optional<JPAAttribute> getDeclaredAttribute(@Nonnull final String internalName) throws ODataJPAModelException {
    Optional<JPAAttribute> result = Optional.ofNullable(getDeclaredPropertiesMap().get(internalName));
    final IntermediateStructuredType<?> baseType = getBaseType();
    if (!result.isPresent() && baseType != null)
      result = baseType.getDeclaredAttribute(internalName);
    return result;
  }

  @Override
  public List<JPAAttribute> getDeclaredAttributes() throws ODataJPAModelException {
    final List<JPAAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : getDeclaredPropertiesMap().entrySet()) {
      result.add(property.getValue());
    }
    final IntermediateStructuredType<? super T> baseType = getBaseType();
    if (baseType != null)
      result.addAll(baseType.getDeclaredAttributes());
    return result;
  }

  @Override
  public List<JPACollectionAttribute> getDeclaredCollectionAttributes() throws ODataJPAModelException {
    final List<JPACollectionAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : getDeclaredPropertiesMap().entrySet()) {
      if (property.getValue().isCollection())
        result.add((JPACollectionAttribute) property.getValue());
    }
    final IntermediateStructuredType<? super T> baseType = getBaseType();
    if (baseType != null)
      result.addAll(baseType.getDeclaredCollectionAttributes());
    return result;
  }

  @Override
  public JPAPath getPath(final String externalName) throws ODataJPAModelException {
    return getPath(externalName, true);
  }

  @Override
  public final JPAPath getPath(final String externalName, final boolean respectIgnore) throws ODataJPAModelException {
    JPAPath targetPath = getResolvedPathMap().get(externalName);
    if (targetPath == null)
      targetPath = getIntermediatePathMap().get(externalName);
    if (targetPath == null || (targetPath.ignore() && respectIgnore))
      return null;
    return targetPath;
  }

  @Override
  public List<JPAPath> getPathList() throws ODataJPAModelException {
    final List<JPAPath> pathList = new ArrayList<>();
    for (final var path : getResolvedPathMap().entrySet()) {
      if (!path.getValue().ignore())
        pathList.add(path.getValue());
    }
    return pathList;
  }

  @Override
  public List<JPAPath> searchChildPath(final JPAPath selectItemPath) throws ODataJPAModelException {
    final List<JPAPath> result = new ArrayList<>();
    for (final JPAPath path : getResolvedPathMap().values()) {
      if (!path.ignore() && path.getAlias().startsWith(selectItemPath.getAlias()))
        result.add(path);
    }
    return result;
  }

  @Override
  public List<JPAProtectionInfo> getProtections() throws ODataJPAModelException {

    return protectedAttributes.get();
  }

  @Override
  public Class<?> getTypeClass() {
    return this.jpaManagedType.getJavaType();
  }

  @Override
  public boolean isAbstract() {
    return false;
  }

  private Map<String, IntermediateProperty> extractPropertiesElements(final Map<String, IntermediateProperty> source,
      final List<String> requesterUserGroups) {
    try {
      final Map<String, IntermediateProperty> result = new HashMap<>();
      for (var property : source.entrySet()) {
        if (property.getValue().isComplex())
          result.put(property.getKey(), property.getValue().asUserGroupRestricted(requesterUserGroups));
        else
          result.put(property.getKey(), property.getValue());
      }
      return result;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private Map<String, IntermediateNavigationProperty<?>> extractNavigationPropertiesElements(
      final Map<String, IntermediateNavigationProperty<?>> source, final List<String> requesterUserGroups) {

    try {
      final Map<String, IntermediateNavigationProperty<?>> result = new HashMap<>(source.size());

      for (var navigation : source.entrySet()) {
        if (navigation.getValue().getTargetEntity() instanceof JPAEntityType et
            && et.isAccessibleFor(requesterUserGroups))
          result.put(navigation.getKey(), navigation.getValue());
      }
      return result;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private Map<String, JPAAssociationPathImpl> extractNavigationPropertyPathElements(
      final Map<String, JPAAssociationPathImpl> source, final List<String> requesterUserGroups) {

    try {
      Map<String, JPAAssociationPathImpl> result = new HashMap<>();
      for (var path : source.entrySet()) {
        if (path.getValue().getTargetType() instanceof JPAEntityType et
            && et.isAccessibleFor(requesterUserGroups))
          result.put(path.getKey(), path.getValue());
      }
      return result;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  <B> IntermediateStructuredType<B> baseTypeRestricted(IntermediateStructuredType<B> type,
      List<String> requesterUserGroups) {
    try {
      return type == null ? null : type.asUserGroupRestricted(requesterUserGroups);
    } catch (ODataJPAModelException e) {
      return null;
    }
  }

  private Map<String, IntermediateNavigationProperty<?>> buildNavigationPropertyList() {

    return buildAllAttributesSet().stream()
        .filter(a -> this.isAssociationProperty(a) && !this.isDescriptionProperty(a))
        .map(this::asNavigationProperty)
        .collect(Collectors.toMap(Pair::key, Pair::value));
  }

  protected Map<String, IntermediateProperty> addDescriptionProperty() {

    return buildAllAttributesSet().stream()
        .filter(a -> this.isAssociationProperty(a) && this.isDescriptionProperty(a))
        .map(this::asDescriptionProperty)
        .collect(Collectors.toMap(Pair::key, Pair::value));
  }

  private boolean isDescriptionProperty(Attribute<? super T, ?> attribute) {
    return attribute.getJavaMember() instanceof AnnotatedElement annotated
        && annotated.getAnnotation(EdmDescriptionAssociation.class) != null;
  }

  private boolean isAssociationProperty(Attribute<? super T, ?> attribute) {
    return attribute.getPersistentAttributeType() == ONE_TO_MANY
        || attribute.getPersistentAttributeType() == ONE_TO_ONE
        || attribute.getPersistentAttributeType() == MANY_TO_MANY
        || attribute.getPersistentAttributeType() == MANY_TO_ONE;
  }

  protected Map<String, IntermediateProperty> buildPropertyList() {

    return buildAllAttributesSet().stream()
        .filter(
            a -> a.getPersistentAttributeType() == BASIC
                || a.getPersistentAttributeType() == EMBEDDED
                || a.getPersistentAttributeType() == ELEMENT_COLLECTION)
        .map(this::convertToProperty)
        .collect(Collectors.toMap(Pair::key, Pair::value));
  }

  /**
   * @throws ODataJPAModelException
   *
   */
  protected void checkPropertyConsistency() throws ODataJPAModelException {

    for (final Entry<String, IntermediateProperty> property : getDeclaredPropertiesMap().entrySet()) {
      if (property.getValue().isTransient()) {
        for (final String internalPath : property.getValue().getRequiredProperties()) {
          validateInternalPath(property.getKey(), internalPath);
        }
      }
    }
  }

  protected FullQualifiedName determineBaseTypeFqn() throws ODataJPAModelException {

    final IntermediateStructuredType<? super T> baseEntity = getBaseType();
    if (baseEntity != null && !baseEntity.isAbstract() && isAbstract())
      // Abstract entity type '%1$s' must not inherit from a non-abstract entity type '%2$s'
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.INHERITANCE_NOT_ALLOWED,
          this.internalName, baseEntity.internalName);
    return baseEntity != null ? buildFQN(baseEntity.getExternalName()) : null;
  }

  protected boolean determineHasStream() {
    return getStreamProperty().isPresent();
  }

  protected void determineIgnore() {
    final EdmIgnore jpaIgnore = this.jpaManagedType.getJavaType().getAnnotation(EdmIgnore.class);
    if (jpaIgnore != null) {
      this.setIgnore(true);
    }
  }

  @Override
  public abstract IntermediateStructuredType<? super T> getBaseType() throws ODataJPAModelException;

  protected Optional<IntermediateSimpleProperty> getStreamProperty() {

    if (streamProperty
        .orElseGet(this::determineStreamProperties)
        .isEmpty())
      return Optional.empty();
    return Optional.ofNullable(streamProperty.get().get(0));
  }

  /**
   * The managed type (e.g. via jpaManagedType.getDeclaredAttributes()) does not provide transient field. Therefore they
   * have to be simulated
   * @return
   * @throws ODataJPAModelException
   */
  Map<String, IntermediateProperty> addTransientProperties() throws ODataJPAModelException {
    final Map<String, IntermediateProperty> result = new HashMap<>();
    result.putAll(addTransientOfManagedType(Arrays.asList(jpaManagedType.getJavaType().getDeclaredFields())));
    result.putAll(addTransientOfManagedType(mappedSuperclass.stream()
        .map(MappedSuperclassType::getJavaType)
        .map(Class::getDeclaredFields)
        .map(Arrays::asList)
        .flatMap(List::stream)
        .toList()));
    return result;
  }

  Map<String, IntermediateProperty> addVirtualProperties(Map<String, IntermediateProperty> explicitProperties)
      throws ODataJPAModelException {
    final Map<String, IntermediateProperty> virtualProperties = new HashMap<>();
    for (final IntermediateNavigationProperty<?> naviProperty : getDeclaredNavigationPropertiesMap().values()) {
      if (!naviProperty.isMapped()) {
        for (final JPAJoinColumn joinColumn : naviProperty.getJoinColumns()) {
          final String dbColumnName = joinColumn.getName();
          final IntermediateModelElement property = this.getPropertyByDBFieldInternal(explicitProperties, dbColumnName);
          if (property == null) {
            final Class<?> dbType = determineTargetDBType(naviProperty, joinColumn);
            final IntermediateProperty virtualProperty = new IntermediateVirtualProperty(nameBuilder,
                new VirtualAttribute<>(jpaManagedType, dbColumnName), schema, dbColumnName, dbType);
            virtualProperties.put(virtualProperty.getInternalName(), virtualProperty);
          }
        }
      }
    }
    return virtualProperties;
  }

  List<JPAAttribute> getAssociations() throws ODataJPAModelException {

    final List<JPAAttribute> jpaAttributes = new ArrayList<>();
    for (final Entry<String, IntermediateNavigationProperty<?>> naviProperty : getDeclaredNavigationPropertiesMap()
        .entrySet()) {
      final IntermediateNavigationProperty<?> property = naviProperty.getValue();
      if (!property.ignore())
        jpaAttributes.add(property);
    }
    final IntermediateStructuredType<? super T> baseType = getBaseType();
    if (baseType != null)
      jpaAttributes.addAll(baseType.getAssociations());
    return jpaAttributes;
  }

  JPAAssociationAttribute getCorrespondingAssociation(final IntermediateStructuredType<?> sourceType,
      final String sourceRelationshipName) throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = findCorrespondingAssociation(sourceType, sourceRelationshipName);
    if (jpaAttribute != null) {
      final JPAAssociationAttribute association = getDeclaredNavigationPropertiesMap().get(jpaAttribute.getName());
      if (association != null)
        return association;
      return new IntermediateNavigationProperty<>(nameBuilder, this, jpaAttribute, schema);
    }
    return null;
  }

  @Override
  CsdlStructuralType getEdmItem() throws ODataJPAModelException {
    return edmStructuralType.get().orElse(null);
  }

  abstract CsdlStructuralType buildEdmItem();

  Map<String, JPAPath> getIntermediatePathMap() throws ODataJPAModelException {
    return intermediatePathMap.get();
  }

  List<IntermediateJoinColumn> getJoinColumns(final String relationshipName) throws ODataJPAModelException {

    final JPAAssociationAttribute association = this.getAssociation(relationshipName);
    if (association != null) {
      return ((IntermediateNavigationProperty<?>) association).getJoinColumns();
    }
    // The association '%2$s' has not been found at '%1$s'
    throw new ODataJPAModelException(INVALID_NAVIGATION_PROPERTY, relationshipName, getInternalName());
  }

  /**
   * Method follows resolved semantic
   *
   * @param dbFieldName
   * @return
   * @throws ODataJPAModelException
   */
  JPAPath getPathByDBField(final String dbFieldName) throws ODataJPAModelException {
    for (final JPAPath path : getResolvedPathMap().values()) {
      if (path.getDBFieldName().equals(dbFieldName)
          && !pathContainsCollection(path))
        return path;
    }

    return null;
  }

  Map<String, JPAAssociationPathImpl> getResolvedAssociationPathMap() throws ODataJPAModelException {
    return resolvedAssociationPathMap.get();
  }

  private boolean pathContainsCollection(final JPAPath path) {

    return path.getPath().stream()
        .map(JPAAttribute.class::cast)
        .anyMatch(JPAAttribute::isCollection);
  }

  /**
   * Returns an property regardless if it should be ignored or not
   * @param internalName
   * @return
   * @throws ODataJPAModelException
   */
  IntermediateProperty getProperty(final String internalName) throws ODataJPAModelException {
    IntermediateProperty result = getDeclaredPropertiesMap().get(internalName);
    final IntermediateStructuredType<?> baseType = getBaseType();
    if (result == null && baseType != null)
      result = baseType.getProperty(internalName);
    return result;
  }

  /**
   * Gets a property by its database field name.
   * <p>
   * The resolution respects embedded types as well as super types
   * @param dbFieldName
   * @return
   * @throws ODataJPAModelException
   */
  IntermediateModelElement getPropertyByDBField(final String dbFieldName) throws ODataJPAModelException {
    return getPropertyByDBFieldInternal(getDeclaredPropertiesMap(), dbFieldName);
  }

  Map<String, JPAPath> getResolvedPathMap() throws ODataJPAModelException {
    return resolvedPathMap.get();
  }

  Map<String, IntermediateNavigationProperty<?>> getDeclaredNavigationPropertiesMap() throws ODataJPAModelException {
    return declaredNavigationPropertiesMap.get();
  }

  boolean hasBuildStepPerformed(final int step) {
    return (buildState & step) != 0;
  }

  private Map<String, IntermediateProperty> addTransientOfManagedType(final List<Field> fields)
      throws ODataJPAModelException {

    final Map<String, IntermediateProperty> result = new HashMap<>();

    for (final Field jpaAttribute : fields) {
      final EdmTransient edmTransient = jpaAttribute.getAnnotation(EdmTransient.class);
      if (edmTransient != null) {
        IntermediateProperty property = null;
        if (Collection.class.isAssignableFrom(jpaAttribute.getType())) {
          property = new IntermediateCollectionProperty<>(nameBuilder,
              new TransientPluralAttribute<>(jpaManagedType, jpaAttribute, schema), schema, this);
        } else {
          property = new IntermediateSimpleProperty(nameBuilder,
              new TransientSingularAttribute<>(jpaManagedType, jpaAttribute), schema);
        }
        result.put(property.internalName, property);
      }
    }
    return result;
  }

  private Set<Attribute<? super T, ?>> buildAllAttributesSet() {
    final Set<Attribute<? super T, ?>> attributes = new HashSet<>();
    attributes.addAll(jpaManagedType.getDeclaredAttributes());
    attributes.addAll(mappedSuperclass.stream()
        .map(ManagedType::getDeclaredAttributes)
        .flatMap(Set::stream)
        .collect(Collectors.toSet()));
    return attributes;
  }

  private String buildPath(final String pathRoot, final String pathElement) {
    return pathRoot + JPAPath.PATH_SEPARATOR + pathElement;
  }

  private Pair<String, IntermediateNavigationProperty<?>> asNavigationProperty(final Attribute<?, ?> jpaAttribute) {
    try {
      if (LOGGER.isTraceEnabled())
        LOGGER.trace(getExternalName() + ": found navigation property, attribute'" + jpaAttribute.getName() + "'");
      final IntermediateNavigationProperty<?> navigationProp = new IntermediateNavigationProperty<>(nameBuilder, this,
          jpaAttribute, schema);
      return new Pair<>(navigationProp.internalName, navigationProp);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private Pair<String, IntermediateProperty> convertToProperty(final Attribute<?, ?> jpaAttribute) {
    final PersistentAttributeType attributeType = jpaAttribute.getPersistentAttributeType();

    try {
      switch (attributeType) {
        case BASIC, EMBEDDED:
          if (jpaAttribute instanceof SingularAttribute<?, ?>
              && ((SingularAttribute<?, ?>) jpaAttribute).isId()
              && attributeType == PersistentAttributeType.EMBEDDED) {
            IntermediateSimpleProperty property = new IntermediateEmbeddedIdProperty(nameBuilder, jpaAttribute,
                schema);
            return new Pair<>(property.internalName, property);
          } else {
            final IntermediateSimpleProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute,
                schema);
            return new Pair<>(property.internalName, property);
          }
        case ELEMENT_COLLECTION:
          final IntermediateCollectionProperty<T> property = new IntermediateCollectionProperty<>(nameBuilder,
              (PluralAttribute<?, ?, ?>) jpaAttribute, schema, this);
          return new Pair<>(property.internalName, property);
        default:
          throw new ODataJPAModelInternalException(
              // Attribute Type '%1$s' as of now not supported
              new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_ATTRIBUTE_TYPE,
                  attributeType.name()));
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private Pair<String, IntermediateProperty> asDescriptionProperty(final Attribute<?, ?> jpaAttribute) {
    try {
      if (LOGGER.isTraceEnabled())
        LOGGER.trace(getExternalName() + ": found description property, attribute '" + jpaAttribute.getName()
            + "'");
      final IntermediateDescriptionProperty descProperty = new IntermediateDescriptionProperty(nameBuilder,
          jpaAttribute, this, schema);
      return new Pair<>(descProperty.internalName, descProperty);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private String determineDBFieldName(final IntermediateProperty property, final JPAPath jpaPath) {
    if (!property.isTransient()) {
      final Attribute<?, ?> jpaAttribute = jpaManagedType.getAttribute(property.getInternalName());
      if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
        final AnnotatedElement a = (AnnotatedElement) jpaAttribute.getJavaMember();
        final AttributeOverrides overwriteList = a.getAnnotation(AttributeOverrides.class);
        if (overwriteList != null) {
          for (final AttributeOverride overwrite : overwriteList.value()) {
            if (overwrite.name().equals(jpaPath.getLeaf().getInternalName()))
              return overwrite.column().name();
          }
        } else {
          final AttributeOverride overwrite = a.getAnnotation(AttributeOverride.class);
          if (overwrite != null && overwrite.name().equals(jpaPath.getLeaf().getInternalName())) {
            return overwrite.column().name();
          }
        }
      }
    }
    return jpaPath.getDBFieldName();
  }

  private List<? extends JPAJoinColumn> determineJoinColumns(final IntermediateModelElement property, // NOSONAR
      final JPAAssociationPath association) {
    final List<IntermediateJoinColumn> result = new ArrayList<>();

    final Attribute<?, ?> jpaAttribute = jpaManagedType.getAttribute(property.getInternalName());
    if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      final AnnotatedElement a = (AnnotatedElement) jpaAttribute.getJavaMember();
      if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
        final AssociationOverrides overwriteList = a.getAnnotation(AssociationOverrides.class);
        if (overwriteList != null) {
          for (final AssociationOverride overwrite : overwriteList.value()) {
            if (overwrite.name().equals(association.getLeaf().getInternalName())) {
              for (final JoinColumn column : overwrite.joinColumns())
                result.add(new IntermediateJoinColumn(column));
            }
          }
        } else {
          final AssociationOverride overwrite = a.getAnnotation(AssociationOverride.class);
          if (overwrite != null && overwrite.name().equals(association.getLeaf().getInternalName())) {
            for (final JoinColumn column : overwrite.joinColumns())
              result.add(new IntermediateJoinColumn(column));
          }
        }
      }
    }
    return result;

  }

  @SuppressWarnings("unchecked")
  private List<MappedSuperclassType<? super T>> determineMappedSuperclass(final ManagedType<T> managedType) {

    if (managedType instanceof IdentifiableType<?> type) {
      final List<MappedSuperclassType<? super T>> result = new ArrayList<>();
      while (type.getSupertype() instanceof MappedSuperclassType<?>) {
        type = type.getSupertype();
        result.add((MappedSuperclassType<? super T>) type);
      }
      return result;
    }
    return Collections.emptyList();
  }

  private List<IntermediateSimpleProperty> determineStreamProperties() {
    final List<IntermediateSimpleProperty> result = new ArrayList<>(1);
    try {
      for (final Entry<String, IntermediateProperty> property : getDeclaredPropertiesMap().entrySet()) {
        // Edm.Stream, or a type definition whose underlying type is Edm.Stream, cannot be used in collections or for
        // non-binding parameters to functions or actions.
        if (property.getValue().isStream()) {
          result.add((IntermediateSimpleProperty) property.getValue());
        }
      }

      final IntermediateStructuredType<?> baseType = getBaseType();
      if (baseType != null) {
        final var superResult = baseType.getStreamProperty();
        if (superResult.isPresent()) {
          result.add(superResult.get());
        }
      }
      if (result.size() > 1)
        // Only one stream property per entity is allowed. For %1$s %2$s have been found
        throw new ODataJPAModelInternalException(
            new ODataJPAModelException(ODataJPAModelException.MessageKeys.TO_MANY_STREAMS, internalName, Integer
                .toString(result.size())));
      streamProperty = Optional.of(result);
      return result;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private Class<?> determineTargetClass(final Attribute<?, ?> jpaAttribute) {
    Class<?> targetClass;
    if (jpaAttribute.isCollection()) {
      targetClass = ((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType().getJavaType();
    } else {
      targetClass = jpaAttribute.getJavaType();
    }
    return targetClass;
  }

  @Nonnull
  private Class<?> determineTargetDBType(final IntermediateNavigationProperty<?> intermediateNavigationProperty,
      final JPAJoinColumn joinColumn) throws ODataJPAModelException {

    final IntermediateStructuredType<?> st = ((IntermediateStructuredType<?>) intermediateNavigationProperty
        .getTargetEntity());
    final IntermediateProperty property = ((IntermediateProperty) st.getPropertyByDBField(joinColumn
        .getReferencedColumnName()));
    if (property != null) {
      final Class<?> dbType = property.getDbType();
      if (dbType != null) {
        return dbType;
      }
    }
    // Type of column could not be determined for association '%1$s' of '%2$s'.
    throw new ODataJPAModelException(DB_TYPE_NOT_DETERMINED, intermediateNavigationProperty.getInternalName(),
        getInternalName());
  }

  private Attribute<?, ?> findCorrespondingAssociation(final IntermediateStructuredType<?> sourceType,
      final String sourceRelationshipName) {

    for (final Attribute<?, ?> jpaAttribute : jpaManagedType.getAttributes()) {
      if (jpaAttribute.getPersistentAttributeType() != null
          && jpaAttribute.getJavaMember() instanceof AnnotatedElement
          && !sourceRelationshipName.equals(InternalNameBuilder.buildAssociationName(jpaAttribute))) {
        final Class<?> targetClass = determineTargetClass(jpaAttribute);
        if (targetClass.equals(sourceType.getTypeClass())) {
          final OneToMany cardinalityOtM = ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(
              OneToMany.class);
          if (cardinalityOtM != null && cardinalityOtM.mappedBy() != null
              && cardinalityOtM.mappedBy().equals(sourceRelationshipName))
            return jpaAttribute;
        }
      }
    }

    return null;
  }

  private IntermediateModelElement getPropertyByDBFieldInternal(final Map<String, IntermediateProperty> properties,
      final String dbFieldName)
      throws ODataJPAModelException {
    for (final IntermediateProperty property : properties.values()) {
      if (property.isComplex()) {
        final IntermediateProperty embeddedProperty =
            (IntermediateProperty) ((IntermediateStructuredType<?>) property
                .getStructuredType()).getPropertyByDBField(dbFieldName);
        if (embeddedProperty != null && embeddedProperty.getDBFieldName().equals(dbFieldName))
          return embeddedProperty;
      } else if (property.getDBFieldName().equals(dbFieldName)) {
        return property;
      }
    }
    final IntermediateStructuredType<?> baseType = getBaseType();
    if (baseType != null)
      return baseType.getPropertyByDBField(dbFieldName);
    return null;
  }

  private synchronized Map<String, JPAAssociationPathImpl> buildCompleteAssociationPathMap() {
    try {
      final Map<String, JPAAssociationPathImpl> result = new HashMap<>();
      JPAAssociationPathImpl associationPath;
      getResolvedPathMap();
      for (final JPAAttribute association : getAssociations()) {
        associationPath = new JPAAssociationPathImpl((IntermediateNavigationProperty<?>) association, this);
        result.put(associationPath.getAlias(), associationPath);
      }

      for (final Entry<String, JPAPath> entity : this.getIntermediatePathMap().entrySet()) {
        final JPAPath attributePath = entity.getValue();
        if (attributePath.getPath().size() == 1) {
          // Only direct attributes
          final IntermediateProperty property = (IntermediateProperty) attributePath.getLeaf();
          final IntermediateStructuredType<?> is = (IntermediateStructuredType<?>) property.getStructuredType();
          for (final JPAAssociationPath association : is.getAssociationPathList()) {
            associationPath = new JPAAssociationPathImpl(association, this, determineJoinColumns(property, association),
                property);
            result.put(associationPath.getAlias(), associationPath);
          }
        }
      }
      return result;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);

    }
  }

  private synchronized Map<String, JPAPath> buildIntermediatePathMap() throws ODataJPAModelInternalException {
    ArrayList<JPAElement> pathList;
    final Map<String, JPAPath> result = new HashMap<>();
    try {
      for (final Entry<String, IntermediateProperty> propertyEntity : getDeclaredPropertiesMap().entrySet()) {
        final IntermediateProperty property = propertyEntity.getValue();
        if (property.isComplex()) {
          result.put(property.getExternalName(),
              new JPAPathImpl(property.getExternalName(), null, property));
          final Map<String, JPAPath> intermediatePath = ((IntermediateStructuredType<?>) property
              .getStructuredType()).getIntermediatePathMap();
          for (final Entry<String, JPAPath> path : intermediatePath.entrySet()) {
            pathList = new ArrayList<>();
            if (path.getValue().getLeaf() instanceof IntermediateCollectionProperty) {
              if (path.getValue().getPath().size() > 1)
                pathList.addAll(path.getValue().getPath().subList(0, path.getValue().getPath().size() - 1));
              pathList.add(new IntermediateCollectionProperty<>((IntermediateCollectionProperty<?>) path.getValue()
                  .getLeaf(), this, property));
            } else {
              pathList.addAll(path.getValue().getPath());
            }
            pathList.add(0, property);
            final JPAPath newPath = new JPAPathImpl(buildPath(property.getExternalName(), path.getKey()), null,
                pathList);
            result.put(newPath.getAlias(), newPath);
          }
        }

      }
      final IntermediateStructuredType<? super T> baseType = getBaseType();
      if (baseType != null) {
        result.putAll(baseType.getIntermediatePathMap());
      }
      return result;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private synchronized Map<String, JPAPath> buildCompletePathMap() throws ODataJPAModelInternalException {
    final Map<String, JPAPath> result = new HashMap<>();
    try {
      result.putAll(buildOwnResolvedPathMap());
      result.putAll(getBaseTypeResolvedPathMap());
      return result;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private final Map<String, JPAPath> buildOwnResolvedPathMap() throws ODataJPAModelException {
    final Map<String, JPAPath> result = new HashMap<>();
    ArrayList<JPAElement> pathList;
    for (final Entry<String, IntermediateProperty> propertyEntity : getDeclaredPropertiesMap().entrySet()) {
      final IntermediateProperty property = propertyEntity.getValue();
      if (property.isComplex()) {
        final var resolvedPath = ((IntermediateStructuredType<?>) property
            .getStructuredType()).getResolvedPathMap();

        for (final var path : resolvedPath.entrySet()) {
          pathList = new ArrayList<>(path.getValue().getPath());
          pathList.add(0, property);
          JPAPathImpl newPath;
          if (property.isKey()) {
            newPath = new JPAPathImpl(path.getKey(), determineDBFieldName(property, resolvedPath.get(path.getKey())),
                pathList);
          } else {

            newPath = new JPAPathImpl(buildPath(property.getExternalName(), path.getKey()),
                determineDBFieldName(property, path.getValue()), rebuildPathList(pathList));
          }
          result.put(newPath.getAlias(), newPath);

        }
      } else {
        result.put(property.getExternalName(), new JPAPathImpl(property.getExternalName(), property
            .getDBFieldName(), property));
      }
    }
    return result;
  }

  protected abstract Map<String, JPAPath> getBaseTypeResolvedPathMap() throws ODataJPAModelException;

  protected abstract Map<String, IntermediateProperty> buildCompletePropertyMap();

  Map<String, IntermediateProperty> getDeclaredPropertiesMap() throws ODataJPAModelException {
    return declaredPropertiesMap.get();
  }

  private Map<String, JPAPath> restrictedPathMap(final Map<String, JPAPath> source,
      final List<String> requesterUserGroups) {
    return source;
  }

  private Map<String, JPAPath> restrictedIntermediatePathMap(final Map<String, JPAPath> source,
      final List<String> requesterUserGroups) {
    return source;
  }

  private List<JPAProtectionInfo> lazyBuildCompleteProtectionList() throws ODataJPAModelInternalException {
    try {
      final List<JPAProtectionInfo> result = new ArrayList<>();
      for (final JPAAttribute attribute : getDeclaredAttributes()) {
        if (attribute.hasProtection()) {
          if (attribute.isComplex()) {
            for (final String claimName : attribute.getProtectionClaimNames()) {
              for (final String pathName : attribute.getProtectionPath(claimName)) {
                final JPAPath path = this.getPath(pathName, false);
                if (path == null) // Annotation EdmProtectedBy found at '%2$s' of '%1$s', but the given 'path' '%3$s'...
                  throw new ODataJPAModelException(COMPLEX_PROPERTY_WRONG_PROTECTION_PATH, attribute.getInternalName(),
                      this.getTypeClass().getSimpleName(), pathName);
                result.add(new ProtectionInfo(path, claimName, attribute));
              }
            }
          } else {
            for (final String claimName : attribute.getProtectionClaimNames()) {
              final var path = this.getPath(attribute.getExternalName(), false);
              if (path == null) // Annotation EdmProtectedBy found at '%2$s' of '%1$s', but the given 'path' '%3$s'...
                throw new ODataJPAModelException(COMPLEX_PROPERTY_WRONG_PROTECTION_PATH, attribute.getInternalName(),
                    this.getTypeClass().getSimpleName());
              result.add(new ProtectionInfo(path, claimName,
                  attribute));
            }
          }
        } else if (attribute.isComplex()) { // Protection at attribute overrides protection within complex
          for (final JPAProtectionInfo info : attribute.getStructuredType().getProtections()) {
            // Copy and extend path
            final String pathName = attribute.getExternalName() + JPAPath.PATH_SEPARATOR + info.getPath().getAlias();
            final JPAPath path = this.getPath(pathName, false);
            result.add(new ProtectionInfo(path, info));
          }
        }
      }
      return result;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private List<JPAElement> rebuildPathList(final List<JPAElement> pathList) throws ODataJPAModelException {

    final StringBuilder path = new StringBuilder();
    for (int i = 0; i < pathList.size() - 1; i++) {
      path.append(pathList.get(i).getExternalName());
      path.append(JPAPath.PATH_SEPARATOR);
    }
    path.deleteCharAt(path.length() - 1);
    final JPAPath parentPath = getIntermediatePathMap().get(path.toString());
    if (parentPath == null)
      return pathList;
    else {
      final List<JPAElement> pathElements = new ArrayList<>(parentPath.getPath());
      JPAElement leaf = pathList.get(pathList.size() - 1);
      if (leaf instanceof IntermediateCollectionProperty
          && pathList.size() > 1
          && ((IntermediateCollectionProperty<?>) leaf).getSourceType() != this)
        leaf = new IntermediateCollectionProperty<>((IntermediateCollectionProperty<?>) leaf, this,
            (IntermediateProperty) pathList.get(0)); // pathList.size() - 2
      pathElements.add(leaf);
      return pathElements;
    }
  }

  private void validateInternalPath(final String propertyName, final String internalPath)
      throws ODataJPAModelException {

    IntermediateStructuredType<?> hop = this;
    for (final String pathPart : internalPath.split(JPAPath.PATH_SEPARATOR)) {
      final JPAAttribute required = hop.getAttribute(pathPart, false).orElseThrow(
          // The transient attribute '%1$s' of class '%2$s' requires '%3$s', which is not known
          () -> new ODataJPAModelException(PROPERTY_REQUIRED_UNKNOWN, propertyName, getInternalName(), internalPath));
      if (required.isComplex())
        hop = (IntermediateStructuredType<?>) required.getStructuredType();
    }
  }

  /**
   * Converts a path given as a string of internal (Java) attribute names into a JPAPath.
   * @param internalPath
   * @return
   * @throws ODataJPAModelException
   */
  @Override
  public ODataPropertyPath convertStringToPath(final String internalPath) throws ODataPathNotFoundException {

    try {
      getResolvedPathMap();
      final String[] pathItems = internalPath.split(JPAPath.PATH_SEPARATOR);
      final StringBuilder targetPath = new StringBuilder();

      IntermediateStructuredType<?> st = this;
      IntermediateProperty nextHop = null;
      for (final String pathItem : pathItems) {
        if (st == null)
          throw new ODataPathNotFoundException(pathItem, internalPath);
        nextHop = st.getAttribute(pathItem)
            .map(IntermediateProperty.class::cast)
            .orElseThrow(() -> new ODataPathNotFoundException(pathItem, internalPath));
        if (!(nextHop.isComplex() && nextHop.isKey()))
          // In case of @EmbeddedId the path gets resolved, as OData does not support keys with a complex type.
          targetPath.append(nextHop.getExternalName()).append(JPAPath.PATH_SEPARATOR);

        st = (IntermediateStructuredType<?>) nextHop.getStructuredType();
      }
      targetPath.deleteCharAt(targetPath.length() - 1);
      return nextHop.isComplex() // NOSONAR
          ? getIntermediatePathMap().get(targetPath.toString())
          : getResolvedPathMap().get(targetPath.toString());
    } catch (final ODataJPAModelException e) {
      throw new ODataPathNotFoundException(internalPath, e);
    }
  }

  @Override
  public ODataNavigationPath convertStringToNavigationPath(final String internalPath)
      throws ODataPathNotFoundException {

    try {
      final String[] pathItems = internalPath.split(JPAPath.PATH_SEPARATOR);
      final StringBuilder targetPath = new StringBuilder();

      JPAStructuredType st = this;

      for (final String pathItem : pathItems) {
        if (st == null)
          throw new ODataPathNotFoundException(pathItem, internalPath);
        JPAAttribute nextHop = st.getAssociation(pathItem);
        if (nextHop == null) {
          nextHop = st.getAttribute(pathItem)
              .orElseThrow(() -> new ODataPathNotFoundException(pathItem, internalPath));
          st = nextHop.getStructuredType();
        } else {
          // Prevent a path that contains two navigations like parent/parent
          st = null;
        }
        targetPath.append(nextHop.getExternalName()).append(JPAPath.PATH_SEPARATOR);
      }
      targetPath.deleteCharAt(targetPath.length() - 1);
      return getResolvedAssociationPathMap().get(targetPath.toString());
    } catch (final ODataJPAModelException e) {
      throw new ODataPathNotFoundException(internalPath, e);
    }
  }

  @Override
  public Annotation javaAnnotation(final String name) {
    for (final Annotation a : this.jpaManagedType.getJavaType().getAnnotations()) {
      if (a.annotationType().getName().equals(name)) {
        return a;
      }
    }
    return null;
  }

  @Override
  public Map<String, Annotation> javaAnnotations(final String packageName) {

    return findJavaAnnotation(packageName, this.jpaJavaType);
  }

  protected abstract static class TransientAttribute<X, Y> implements Attribute<X, Y> {
    protected final ManagedType<X> parent;
    protected final Field attribute;

    TransientAttribute(final ManagedType<X> parent, final Field attribute) {
      super();
      this.parent = parent;
      this.attribute = attribute;
    }

    @Override
    public ManagedType<X> getDeclaringType() {
      return parent;
    }

    @Override
    public Member getJavaMember() {
      return attribute;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Y> getJavaType() {
      return (Class<Y>) attribute.getType();
    }

    @Override
    public String getName() {
      return attribute.getName();
    }

    @Override
    public boolean isAssociation() {
      return false;
    }
  }

  protected static class TransientPluralAttribute<X, Y, Z> extends TransientAttribute<X, Y>
      implements PluralAttribute<X, Y, Z> {

    private final TransientRowType<?> type;

    /**
     * @param parent
     * @param attribute
     */
    TransientPluralAttribute(final ManagedType<X> parent, final Field attribute, final IntermediateSchema schema) {
      super(parent, attribute);
      this.type = new TransientRowType<>(attribute, schema);
    }

    @Override
    public Class<Z> getBindableJavaType() {
      return null;
    }

    @Override
    public BindableType getBindableType() {
      return null;
    }

    @Override
    public CollectionType getCollectionType() {
      if (Map.class.isAssignableFrom(attribute.getType()))
        return CollectionType.MAP;
      if (Set.class.isAssignableFrom(attribute.getType()))
        return CollectionType.SET;
      if (List.class.isAssignableFrom(attribute.getType()))
        return CollectionType.LIST;
      return CollectionType.COLLECTION;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type<Z> getElementType() {
      return (Type<Z>) type;
    }

    @Override
    public PersistentAttributeType getPersistentAttributeType() {
      return PersistentAttributeType.ELEMENT_COLLECTION;
    }

    @Override
    public boolean isCollection() {
      return true;
    }
  }

  protected static class TransientRowType<Z> implements Type<Z> {

    private final Class<?> rowType;
    private final PersistenceType persistenceType;

    /**
     * @param attribute
     */
    public TransientRowType(final Field attribute, final IntermediateSchema schema) {
      if (attribute.getGenericType() instanceof final ParameterizedType type) {
        final java.lang.reflect.Type[] attributes = type.getActualTypeArguments();
        this.rowType = (Class<?>) attributes[attributes.length - 1];
      } else {
        this.rowType = attribute.getType();
      }
      this.persistenceType = determinePersistenceType(schema);

    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Z> getJavaType() {
      return (Class<Z>) rowType;
    }

    @Override
    public PersistenceType getPersistenceType() {
      return persistenceType;
    }

    /**
     * @param schema
     * @return
     */
    private PersistenceType determinePersistenceType(final IntermediateSchema schema) {
      return schema.getStructuredType(rowType) == null ? PersistenceType.BASIC : PersistenceType.EMBEDDABLE;
    }

  }

  protected static class TransientSingularAttribute<X, Y> extends TransientAttribute<X, Y>
      implements SingularAttribute<X, Y> {

    TransientSingularAttribute(final ManagedType<X> parent, final Field attribute) {
      super(parent, attribute);
    }

    @Override
    public Class<Y> getBindableJavaType() {
      return null;
    }

    @Override
    public BindableType getBindableType() {
      return null;
    }

    @Override
    public PersistentAttributeType getPersistentAttributeType() {
      return PersistentAttributeType.BASIC;
    }

    @Override
    public Type<Y> getType() {
      return null;
    }

    @Override
    public boolean isCollection() {
      return false;
    }

    @Override
    public boolean isId() {
      final Id id = this.attribute.getAnnotation(Id.class);
      return id != null;
    }

    @Override
    public boolean isOptional() {
      final Column column = this.attribute.getAnnotation(Column.class);
      return column == null || column.nullable();
    }

    @Override
    public boolean isVersion() {
      final Version version = this.attribute.getAnnotation(Version.class);
      return version != null;
    }
  }

  static class VirtualAttribute<X, Y> implements Attribute<X, Y> {
    private final ManagedType<X> parent;
    private final String dbColumnName;

    VirtualAttribute(final ManagedType<X> parent, final String dbColumnName) {
      super();
      this.parent = parent;
      this.dbColumnName = dbColumnName;
    }

    @Override
    public ManagedType<X> getDeclaringType() {
      return parent;
    }

    @Override
    public Member getJavaMember() {
      return null;
    }

    @Override
    public Class<Y> getJavaType() {
      return null;
    }

    @Override
    public String getName() {
      return dbColumnName.replace("\"", "").toLowerCase(Locale.ENGLISH);
    }

    @Override
    public PersistentAttributeType getPersistentAttributeType() {
      return PersistentAttributeType.BASIC;
    }

    @Override
    public boolean isAssociation() {
      return false;
    }

    @Override
    public boolean isCollection() {
      return false;
    }

  }

  private static class ProtectionInfo implements JPAProtectionInfo {
    private final JPAPath pathToAttribute;
    private final String claimName;
    private final boolean wildcardSupported;

    /**
     * Copy with a new path. This can be used for nested structured types
     * @param path
     * @param info
     */
    public ProtectionInfo(final JPAPath path, final JPAProtectionInfo info) {
      this.pathToAttribute = path;
      this.claimName = info.getClaimName();
      this.wildcardSupported = info.supportsWildcards();
    }

    public ProtectionInfo(final JPAPath path, final String claimName, final JPAAttribute attribute) {
      this.pathToAttribute = path;
      this.claimName = claimName;
      this.wildcardSupported = ((IntermediateProperty) attribute).protectionWithWildcard(claimName, path.getLeaf()
          .getType());

    }

    @Override
    public JPAAttribute getAttribute() {
      return pathToAttribute.getLeaf();
    }

    @Override
    public String getClaimName() {
      return claimName;
    }

    @Override
    public JPAPath getPath() {
      return pathToAttribute;
    }

    @Override
    public boolean supportsWildcards() {
      return wildcardSupported;
    }

    @Override
    public String toString() {
      return "ProtectionInfo [pathToAttribute=" + pathToAttribute.getAlias() + ", claimName=" + claimName
          + ", wildcardSupported=" + wildcardSupported + "]";
    }
  }

  private static record Pair<K, V>(K key, V value) {}
}