package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.COMPLEX_PROPERTY_WRONG_PROTECTION_PATH;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.DB_TYPE_NOT_DETERMINED;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.INVALID_NAVIGATION_PROPERTY;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.PROPERTY_REQUIRED_UNKNOWN;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.BASIC;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.ELEMENT_COLLECTION;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.EMBEDDED;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_MANY;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_ONE;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_MANY;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_ONE;

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
import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MappedSuperclassType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

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
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

abstract class IntermediateStructuredType<T> extends IntermediateModelElement implements JPAStructuredType,
    ODataAnnotatable {
//
  static final int PROPERTY_BUILD = 2; // Declared properties have been build, virtual and transient may be missing
  static final int NAVIGATION_BUILD = 4;
  private static final Log LOGGER = LogFactory.getLog(IntermediateStructuredType.class);
  protected final Map<String, IntermediateProperty> declaredPropertiesMap;
  protected final Map<String, IntermediateNavigationProperty<?>> declaredNavigationPropertiesMap;
  protected final Map<String, JPAPathImpl> resolvedPathMap;
  protected final Map<String, JPAPath> intermediatePathMap;
  protected final Map<String, JPAAssociationPathImpl> resolvedAssociationPathMap;
  protected final ManagedType<T> jpaManagedType;
  protected final Class<T> jpaJavaType;
  protected final List<MappedSuperclassType<? super T>> mappedSuperclass;
  protected final IntermediateSchema schema;
  protected List<JPAProtectionInfo> protectedAttributes;
  protected CsdlStructuralType edmStructuralType;
  private Optional<List<IntermediateSimpleProperty>> streamProperty;
  private int buildState = 0;

  IntermediateStructuredType(final JPAEdmNameBuilder nameBuilder, final ManagedType<T> jpaManagedType,
      final IntermediateSchema schema) {

    super(nameBuilder, InternalNameBuilder.buildStructuredTypeName(jpaManagedType.getJavaType()), schema
        .getAnnotationInformation());
    this.declaredPropertiesMap = new HashMap<>();
    this.resolvedPathMap = new HashMap<>();
    this.intermediatePathMap = new HashMap<>();
    this.declaredNavigationPropertiesMap = new HashMap<>();
    this.resolvedAssociationPathMap = new HashMap<>();
    this.jpaManagedType = jpaManagedType;
    this.jpaJavaType = this.jpaManagedType.getJavaType();
    this.schema = schema;
    this.mappedSuperclass = determineMappedSuperclass(jpaManagedType);
    this.streamProperty = Optional.empty();
    determineIgnore();
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
    lazyBuildCompleteAssociationPathMap();
    return resolvedAssociationPathMap.get(externalName);
  }

  @Override
  public List<JPAAssociationPath> getAssociationPathList() throws ODataJPAModelException {
    lazyBuildCompleteAssociationPathMap();
    final List<JPAAssociationPath> associationList = new ArrayList<>();

    for (final Entry<String, JPAAssociationPathImpl> associationPat : resolvedAssociationPathMap.entrySet()) {
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
    if (edmStructuralType == null)
      lazyBuildEdmItem();
    Optional<JPAAttribute> result = Optional.ofNullable(declaredPropertiesMap.get(internalName));
    if (!result.isPresent() && getBaseType() != null)
      result = getBaseType().getAttribute(internalName);
    else if (result.isPresent() && respectIgnore && ((IntermediateModelElement) result.get()).ignore())
      return Optional.empty();
    return result;
  }

  @Override
  public Optional<JPAAttribute> getAttribute(final UriResourceProperty uriResourceItem) throws ODataJPAModelException {
    if (edmStructuralType == null)
      lazyBuildEdmItem();
    final String externalName = uriResourceItem.getProperty().getName();
    for (final Entry<String, IntermediateProperty> property : declaredPropertiesMap.entrySet()) {
      if (property.getValue().getExternalName().equals(externalName))
        return Optional.of(property.getValue());
    }
    if (getBaseType() != null)
      return getBaseType().getAttribute(uriResourceItem);
    return Optional.empty();
  }

  @Override
  public List<JPAAttribute> getAttributes() throws ODataJPAModelException {
    if (edmStructuralType == null)
      lazyBuildEdmItem();
    final List<JPAAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : declaredPropertiesMap.entrySet()) {
      final IntermediateProperty attribute = property.getValue();
      if (!attribute.ignore())
        result.add(attribute);
    }
    if (getBaseType() != null)
      result.addAll(getBaseType().getAttributes());
    return result;
  }

  @Override
  public List<JPAPath> getCollectionAttributesPath() throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    final List<JPAPath> pathList = new ArrayList<>();
    for (final Entry<String, JPAPathImpl> path : resolvedPathMap.entrySet()) {
      if (!path.getValue().ignore() && path.getValue().getLeaf() instanceof JPACollectionAttribute)
        pathList.add(path.getValue());
    }
    for (final Entry<String, JPAPath> path : intermediatePathMap.entrySet()) {
      if (!path.getValue().ignore() && path.getValue().getLeaf() instanceof JPACollectionAttribute)
        pathList.add(path.getValue());
    }
    return pathList;
  }

  @Override
  public List<JPAAssociationAttribute> getDeclaredAssociations() throws ODataJPAModelException {
    lazyBuildCompleteAssociationPathMap();

    final List<JPAAssociationAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateNavigationProperty<?>> naviProperty : declaredNavigationPropertiesMap
        .entrySet())
      result.add(naviProperty.getValue());
    final IntermediateStructuredType<? super T> baseType = getBaseType();
    if (baseType != null)
      result.addAll(baseType.getDeclaredAssociations());
    return result;
  }

  @Override
  public Optional<JPAAttribute> getDeclaredAttribute(@Nonnull final String internalName) throws ODataJPAModelException {
    if (edmStructuralType == null)
      lazyBuildEdmItem();
    Optional<JPAAttribute> result = Optional.ofNullable(declaredPropertiesMap.get(internalName));
    if (!result.isPresent() && getBaseType() != null)
      result = getBaseType().getDeclaredAttribute(internalName);
    return result;
  }

  @Override
  public List<JPAAttribute> getDeclaredAttributes() throws ODataJPAModelException {
    if (edmStructuralType == null)
      lazyBuildEdmItem();
    final List<JPAAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : declaredPropertiesMap.entrySet()) {
      result.add(property.getValue());
    }
    final IntermediateStructuredType<? super T> baseType = getBaseType();
    if (baseType != null)
      result.addAll(baseType.getDeclaredAttributes());
    return result;
  }

  @Override
  public List<JPACollectionAttribute> getDeclaredCollectionAttributes() throws ODataJPAModelException {
    if (edmStructuralType == null)
      lazyBuildEdmItem();
    final List<JPACollectionAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : declaredPropertiesMap.entrySet()) {
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
    lazyBuildCompletePathMap();
    JPAPath targetPath = resolvedPathMap.get(externalName);
    if (targetPath == null)
      targetPath = intermediatePathMap.get(externalName);
    if (targetPath == null || (targetPath.ignore() && respectIgnore))
      return null;
    return targetPath;
  }

  @Override
  public List<JPAPath> getPathList() throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    final List<JPAPath> pathList = new ArrayList<>();
    for (final Entry<String, JPAPathImpl> path : resolvedPathMap.entrySet()) {
      if (!path.getValue().ignore())
        pathList.add(path.getValue());
    }
    return pathList;
  }

  @Override
  public List<JPAProtectionInfo> getProtections() throws ODataJPAModelException {
    lazyBuildCompleteProtectionList();
    return protectedAttributes;
  }

  @Override
  public Class<?> getTypeClass() {
    return this.jpaManagedType.getJavaType();
  }

  @Override
  public boolean isAbstract() {
    return false;
  }

  protected void buildNavigationPropertyList() throws ODataJPAModelException {

    if (!hasBuildStepPerformed(NAVIGATION_BUILD)) {
      final Set<Attribute<?, ?>> attributes = buildAllAttributesSet().stream()
          .filter(
              a -> a.getPersistentAttributeType() == ONE_TO_MANY
                  || a.getPersistentAttributeType() == ONE_TO_ONE
                  || a.getPersistentAttributeType() == MANY_TO_MANY
                  || a.getPersistentAttributeType() == MANY_TO_ONE)
          .collect(Collectors.toSet());

      for (final Attribute<?, ?> jpaAttribute : attributes) {
        convertToNavigationProperty(jpaAttribute);
      }
      buildStepFinished(NAVIGATION_BUILD);
    }
  }

  protected void buildPropertyList() throws ODataJPAModelException {

    if (!hasBuildStepPerformed(PROPERTY_BUILD)) {

      final Set<Attribute<?, ?>> attributes = buildAllAttributesSet().stream()
          .filter(
              a -> a.getPersistentAttributeType() == BASIC
                  || a.getPersistentAttributeType() == EMBEDDED
                  || a.getPersistentAttributeType() == ELEMENT_COLLECTION)
          .collect(Collectors.toSet());

      for (final Attribute<?, ?> jpaAttribute : attributes) {
        convertToProperty(jpaAttribute);
      }
      buildStepFinished(PROPERTY_BUILD);
    }
  }

  /**
   * @throws ODataJPAModelException
   *
   */
  protected void checkPropertyConsistency() throws ODataJPAModelException {

    for (final Entry<String, IntermediateProperty> property : declaredPropertiesMap.entrySet()) {
      if (property.getValue().isTransient()) {
        for (final String internalPath : property.getValue().getRequiredProperties()) {
          validateInternalPath(property.getKey(), internalPath);
        }
      }
    }
  }

  protected FullQualifiedName determineBaseType() throws ODataJPAModelException {

    final IntermediateStructuredType<? super T> baseEntity = getBaseType();
    if (baseEntity != null && !baseEntity.isAbstract() && isAbstract())
      // Abstract entity type '%1$s' must not inherit from a non-abstract entity type '%2$s'
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.INHERITANCE_NOT_ALLOWED,
          this.internalName, baseEntity.internalName);
    return baseEntity != null ? buildFQN(baseEntity.getExternalName()) : null;
  }

  protected boolean determineHasStream() {
    return getStreamProperty() != null;
  }

  protected void determineIgnore() {
    final EdmIgnore jpaIgnore = this.jpaManagedType.getJavaType().getAnnotation(EdmIgnore.class);
    if (jpaIgnore != null) {
      this.setIgnore(true);
    }
  }

  /**
   * Determines if the structured type has a super type, that will be part of OData metadata. That is the method will
   * return null in case the entity has a MappedSuperclass.
   * @return Determined super type or null
   */
  protected IntermediateStructuredType<? super T> getBaseType() { // NOSONAR
    final Class<?> baseType = jpaManagedType.getJavaType().getSuperclass();
    if (baseType != null) {
      @SuppressWarnings("unchecked")
      final IntermediateStructuredType<? super T> baseEntity = (IntermediateStructuredType<? super T>) schema
          .getEntityType(baseType);
      if (baseEntity != null)
        return baseEntity;
    }
    return null;
  }

  protected IntermediateSimpleProperty getStreamProperty() {

    if (streamProperty
        .orElseGet(this::determineStreamProperties)
        .isEmpty())
      return null;
    return streamProperty.get().get(0);
  }

  /**
   * The managed type (e.g. via jpaManagedType.getDeclaredAttributes()) does not provide transient field. Therefore they
   * have to be simulated
   * @throws ODataJPAModelException
   */
  void addTransientProperties() throws ODataJPAModelException {

    addTransientOfManagedType(Arrays.asList(jpaManagedType.getJavaType().getDeclaredFields()));
    addTransientOfManagedType(mappedSuperclass.stream()
        .map(MappedSuperclassType::getJavaType)
        .map(Class::getDeclaredFields)
        .map(Arrays::asList)
        .flatMap(List::stream)
        .collect(Collectors.toList()));
  }

  void addVirtualProperties() throws ODataJPAModelException {
    final Map<String, IntermediateProperty> virtualProperties = new HashMap<>();
    for (final IntermediateNavigationProperty<?> naviProperty : declaredNavigationPropertiesMap.values()) {
      if (!naviProperty.isMapped()) {
        for (final JPAJoinColumn joinColumn : naviProperty.getJoinColumns()) {
          final String dbColumnName = joinColumn.getName();
          final IntermediateModelElement property = this.getPropertyByDBFieldInternal(dbColumnName);
          if (property == null) {
            final Class<?> dbType = determineTargetDBType(naviProperty, joinColumn);
            final IntermediateProperty virtualProperty = new IntermediateVirtualProperty(nameBuilder,
                new VirtualAttribute<>(jpaManagedType, dbColumnName), schema, dbColumnName, dbType);
            virtualProperties.put(virtualProperty.getInternalName(), virtualProperty);
          }
        }
      }
    }
    declaredPropertiesMap.putAll(virtualProperties);
  }

  /*
   * buildState shall enable intermediate steps during lazyBuildEdmItem steps to access results of a earlier step
   * without triggering the build => cycle break
   */
  void buildStepFinished(final int step) {
    buildState = buildState | step;
  }

  List<JPAAttribute> getAssociations() throws ODataJPAModelException {
    if (edmStructuralType == null)
      lazyBuildEdmItem();
    final List<JPAAttribute> jpaAttributes = new ArrayList<>();
    for (final Entry<String, IntermediateNavigationProperty<?>> naviProperty : declaredNavigationPropertiesMap
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
      final JPAAssociationAttribute association = declaredNavigationPropertiesMap.get(jpaAttribute.getName());
      if (association != null)
        return association;
      return new IntermediateNavigationProperty<>(nameBuilder, this, jpaAttribute, schema);
    }
    return null;
  }

  @Override
  abstract CsdlStructuralType getEdmItem() throws ODataJPAModelException;

  Map<String, JPAPath> getIntermediatePathMap() throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    return intermediatePathMap;
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
    lazyBuildCompletePathMap();

    for (final JPAPath path : resolvedPathMap.values()) {
      if (path.getDBFieldName().equals(dbFieldName)
          && !pathContainsCollection(path))
        return path;
    }

    return null;
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
    if (edmStructuralType == null)
      lazyBuildEdmItem();
    IntermediateProperty result = declaredPropertiesMap.get(internalName);
    if (result == null && getBaseType() != null)
      result = getBaseType().getProperty(internalName);
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
    buildPropertyList();
    return getPropertyByDBFieldInternal(dbFieldName);
  }

  Map<String, JPAPathImpl> getResolvedPathMap() throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    return resolvedPathMap;
  }

  boolean hasBuildStepPerformed(final int step) {
    return (buildState & step) != 0;
  }

  private void addTransientOfManagedType(final List<Field> fields) throws ODataJPAModelException {
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
        declaredPropertiesMap.put(property.internalName, property);
      }
    }
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

  private void convertToNavigationProperty(final Attribute<?, ?> jpaAttribute) throws ODataJPAModelException {
    if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      final EdmDescriptionAssociation jpaDescription = ((AnnotatedElement) jpaAttribute.getJavaMember())
          .getAnnotation(EdmDescriptionAssociation.class);
      if (jpaDescription != null) {
        if (LOGGER.isTraceEnabled())
          LOGGER.trace(getExternalName() + ": found description property, attribute '" + jpaAttribute.getName()
              + "'");
        final IntermediateDescriptionProperty descProperty = new IntermediateDescriptionProperty(nameBuilder,
            jpaAttribute, this, schema);
        declaredPropertiesMap.put(descProperty.internalName, descProperty);
        return;
      }
    }
    if (LOGGER.isTraceEnabled())
      LOGGER.trace(getExternalName() + ": found navigation property, attribute'" + jpaAttribute.getName() + "'");
    final IntermediateNavigationProperty<?> navigationProp = new IntermediateNavigationProperty<>(nameBuilder, this,
        jpaAttribute, schema);
    declaredNavigationPropertiesMap.put(navigationProp.internalName, navigationProp);
  }

  private void convertToProperty(final Attribute<?, ?> jpaAttribute) throws ODataJPAModelException {
    final PersistentAttributeType attributeType = jpaAttribute.getPersistentAttributeType();

    switch (attributeType) {
      case BASIC:
      case EMBEDDED:
        if (jpaAttribute instanceof SingularAttribute<?, ?>
            && ((SingularAttribute<?, ?>) jpaAttribute).isId()
            && attributeType == PersistentAttributeType.EMBEDDED) {
          final IntermediateSimpleProperty property = new IntermediateEmbeddedIdProperty(nameBuilder, jpaAttribute,
              schema);
          declaredPropertiesMap.put(property.internalName, property);
        } else {
          final IntermediateSimpleProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute,
              schema);
          declaredPropertiesMap.put(property.internalName, property);
        }
        break;
      case ELEMENT_COLLECTION:
        final IntermediateCollectionProperty<T> property = new IntermediateCollectionProperty<>(nameBuilder,
            (PluralAttribute<?, ?, ?>) jpaAttribute, schema, this);
        declaredPropertiesMap.put(property.internalName, property);
        break;
      default:
        // Attribute Type '%1$s' as of now not supported
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_ATTRIBUTE_TYPE,
            attributeType.name());
    }
  }

  private String determineDBFieldName(final IntermediateModelElement property, final JPAPath jpaPath) {
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

    if (managedType instanceof IdentifiableType<?>) {
      final List<MappedSuperclassType<? super T>> result = new ArrayList<>();
      IdentifiableType<?> type = (IdentifiableType<T>) managedType;
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
    for (final Entry<String, IntermediateProperty> property : declaredPropertiesMap.entrySet()) {
      // Edm.Stream, or a type definition whose underlying type is Edm.Stream, cannot be used in collections or for
      // non-binding parameters to functions or actions.
      if (property.getValue().isStream()) {
        result.add((IntermediateSimpleProperty) property.getValue());
      }
    }
    if (this.getBaseType() != null) {
      final IntermediateSimpleProperty superResult = getBaseType().getStreamProperty();
      if (superResult != null) {
        result.add(superResult);
      }
    }
    if (result.size() > 1)
      // Only one stream property per entity is allowed. For %1$s %2$s have been found
      throw new RuntimeException(
          new ODataJPAModelException(ODataJPAModelException.MessageKeys.TO_MANY_STREAMS, internalName, Integer
              .toString(result.size())));
    streamProperty = Optional.of(result);
    return result;
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

  private IntermediateModelElement getPropertyByDBFieldInternal(final String dbFieldName)
      throws ODataJPAModelException {
    for (final IntermediateProperty property : declaredPropertiesMap.values()) {
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
    if (getBaseType() != null)
      return getBaseType().getPropertyByDBField(dbFieldName);
    return null;
  }

  private synchronized void lazyBuildCompleteAssociationPathMap() throws ODataJPAModelException {
    JPAAssociationPathImpl associationPath;
    lazyBuildCompletePathMap();
    // TODO check if ignore has to be handled
    if (resolvedAssociationPathMap.size() == 0) {
      for (final JPAAttribute association : getAssociations()) {
        associationPath = new JPAAssociationPathImpl((IntermediateNavigationProperty<?>) association, this);
        resolvedAssociationPathMap.put(associationPath.getAlias(), associationPath);
      }

      for (final Entry<String, JPAPath> entity : this.intermediatePathMap.entrySet()) {
        final JPAPath attributePath = entity.getValue();
        if (attributePath.getPath().size() == 1) {
          // Only direct attributes
          final IntermediateProperty property = (IntermediateProperty) attributePath.getLeaf();
          final IntermediateStructuredType<?> is = (IntermediateStructuredType<?>) property.getStructuredType();
          for (final JPAAssociationPath association : is.getAssociationPathList()) {
            associationPath = new JPAAssociationPathImpl(association, this, determineJoinColumns(property, association),
                property);
            resolvedAssociationPathMap.put(associationPath.getAlias(), associationPath);
          }
        }
      }
    }
  }

  private synchronized void lazyBuildCompletePathMap() throws ODataJPAModelException {
    ArrayList<JPAElement> pathList;
    if (edmStructuralType == null)
      lazyBuildEdmItem();
    if (resolvedPathMap.size() == 0) {
      for (final Entry<String, IntermediateProperty> propertyEntity : declaredPropertiesMap.entrySet()) {
        final IntermediateProperty property = propertyEntity.getValue();
        if (property.isComplex()) {
          intermediatePathMap.put(property.getExternalName(),
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
            intermediatePathMap.put(newPath.getAlias(), newPath);
          }

          final Map<String, JPAPathImpl> resolvedPath = ((IntermediateStructuredType<?>) property
              .getStructuredType()).getResolvedPathMap();

          for (final Entry<String, JPAPathImpl> path : resolvedPath.entrySet()) {
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
            resolvedPathMap.put(newPath.getAlias(), newPath);

          }
        } else {
          resolvedPathMap.put(property.getExternalName(), new JPAPathImpl(property.getExternalName(), property
              .getDBFieldName(), property));
        }
      }
      final IntermediateStructuredType<? super T> baseType = getBaseType();
      if (baseType != null) {
        resolvedPathMap.putAll(baseType.getResolvedPathMap());
        intermediatePathMap.putAll(baseType.getIntermediatePathMap());
      }
    }
  }

  private void lazyBuildCompleteProtectionList() throws ODataJPAModelException {
    if (protectedAttributes == null) {
      if (edmStructuralType == null)
        lazyBuildEdmItem();
      this.protectedAttributes = new ArrayList<>();
      for (final JPAAttribute attribute : getDeclaredAttributes()) {
        if (attribute.hasProtection()) {
          if (attribute.isComplex()) {
            for (final String claimName : attribute.getProtectionClaimNames()) {
              for (final String pathName : attribute.getProtectionPath(claimName)) {
                final JPAPath path = this.getPath(pathName, false);
                if (path == null) // Annotation EdmProtectedBy found at '%2$s' of '%1$s', but the given 'path' '%3$s'...
                  throw new ODataJPAModelException(COMPLEX_PROPERTY_WRONG_PROTECTION_PATH, attribute.getInternalName(),
                      this.getTypeClass().getSimpleName(), pathName);
                protectedAttributes.add(new ProtectionInfo(path, claimName, attribute));
              }
            }
          } else {
            for (final String claimName : attribute.getProtectionClaimNames()) {
              protectedAttributes.add(new ProtectionInfo(this.getPath(attribute.getExternalName(), false), claimName,
                  attribute));
            }
          }
        } else if (attribute.isComplex()) { // Protection at attribute overrides protection within complex
          for (final JPAProtectionInfo info : attribute.getStructuredType().getProtections()) {
            // Copy and extend path
            final String pathName = attribute.getExternalName() + JPAPath.PATH_SEPARATOR + info.getPath().getAlias();
            final JPAPath path = this.getPath(pathName, false);
            protectedAttributes.add(new ProtectionInfo(path, info));
          }
        }
      }
    }
  }

  private List<JPAElement> rebuildPathList(final List<JPAElement> pathList) throws ODataJPAModelException {

    final StringBuilder path = new StringBuilder();
    for (int i = 0; i < pathList.size() - 1; i++) {
      path.append(pathList.get(i).getExternalName());
      path.append(JPAPath.PATH_SEPARATOR);
    }
    path.deleteCharAt(path.length() - 1);
    final JPAPath parentPath = intermediatePathMap.get(path.toString());
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
      lazyBuildCompletePathMap();
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
          ? intermediatePathMap.get(targetPath.toString())
          : resolvedPathMap.get(targetPath.toString());
    } catch (final ODataJPAModelException e) {
      throw new ODataPathNotFoundException(internalPath, e);
    }
  }

  @Override
  public ODataNavigationPath convertStringToNavigationPath(final String internalPath)
      throws ODataPathNotFoundException {

    try {
      lazyBuildCompleteAssociationPathMap();
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
      return resolvedAssociationPathMap.get(targetPath.toString());
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
      if (attribute.getGenericType() instanceof ParameterizedType) {
        final java.lang.reflect.Type[] attributes = ((ParameterizedType) attribute.getGenericType())
            .getActualTypeArguments();
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
}