package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.COMPLEX_PROPERTY_WRONG_PROTECTION_PATH;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlStructuralType;
import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssoziation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

abstract class IntermediateStructuredType extends IntermediateModelElement implements JPAStructuredType {
// 
  protected final Map<String, IntermediateProperty> declaredPropertiesList;
  protected final Map<String, IntermediateNavigationProperty> declaredNaviPropertiesList;
  protected final Map<String, JPAPathImpl> resolvedPathMap;
  protected final Map<String, JPAPath> intermediatePathMap;
  protected final Map<String, JPAAssociationPathImpl> resolvedAssociationPathMap;
  protected final ManagedType<?> jpaManagedType;
  protected final IntermediateSchema schema;
  protected List<JPAProtectionInfo> protectedAttributes;

  IntermediateStructuredType(final JPAEdmNameBuilder nameBuilder, final ManagedType<?> jpaManagedType,
      final IntermediateSchema schema) {

    super(nameBuilder, IntNameBuilder.buildStructuredTypeName(jpaManagedType.getJavaType()));
    this.declaredPropertiesList = new HashMap<>();
    this.resolvedPathMap = new HashMap<>();
    this.intermediatePathMap = new HashMap<>();
    this.declaredNaviPropertiesList = new HashMap<>();
    this.resolvedAssociationPathMap = new HashMap<>();
    this.jpaManagedType = jpaManagedType;
    this.schema = schema;
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
  public JPAAttribute getAttribute(final String internalName) throws ODataJPAModelException {
    lazyBuildEdmItem();
    JPAAttribute result = declaredPropertiesList.get(internalName);
    if (result == null && getBaseType() != null)
      result = getBaseType().getAttribute(internalName);
    else if (result != null && ((IntermediateModelElement) result).ignore())
      return null;
    return result;
  }

  @Override
  public JPAAttribute getAttribute(final UriResourceProperty uriResourceItem) throws ODataJPAModelException {
    lazyBuildEdmItem();
    final String externalName = uriResourceItem.getProperty().getName();
    for (final Entry<String, IntermediateProperty> property : declaredPropertiesList.entrySet()) {
      if (property.getValue().getExternalName().equals(externalName))
        return property.getValue();
    }
    if (getBaseType() != null)
      return getBaseType().getAttribute(uriResourceItem);
    return null;
  }

  @Override
  public List<JPAAttribute> getAttributes() throws ODataJPAModelException {
    lazyBuildEdmItem();
    final List<JPAAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : declaredPropertiesList.entrySet()) {
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
  public JPAAssociationPath getDeclaredAssociation(final JPAAssociationPath associationPath)
      throws ODataJPAModelException {
    lazyBuildCompleteAssociationPathMap();

    if (resolvedAssociationPathMap.containsKey(associationPath.getAlias()))
      return resolvedAssociationPathMap.get(associationPath.getAlias());
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null)
      return baseType.getDeclaredAssociation(associationPath);
    return null;
  }

  @Override
  public JPAAssociationPath getDeclaredAssociation(final String externalName) throws ODataJPAModelException {
    lazyBuildCompleteAssociationPathMap();
    for (final Entry<String, IntermediateNavigationProperty> naviProperty : declaredNaviPropertiesList.entrySet()) {

      if (externalName.equals(naviProperty.getValue().getExternalName()))
        return resolvedAssociationPathMap.get(externalName);
    }
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null)
      return baseType.getDeclaredAssociation(externalName);
    return null;
  }

  @Override
  public List<JPAAssociationAttribute> getDeclaredAssociations() throws ODataJPAModelException {
    lazyBuildCompleteAssociationPathMap();

    List<JPAAssociationAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateNavigationProperty> naviProperty : declaredNaviPropertiesList.entrySet())
      result.add(naviProperty.getValue());
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null)
      result.addAll(baseType.getDeclaredAssociations());
    return result;
  }

  @Override
  public List<JPAAttribute> getDeclaredAttributes() throws ODataJPAModelException {
    lazyBuildEdmItem();
    List<JPAAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : declaredPropertiesList.entrySet()) {
      result.add(property.getValue());
    }
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null)
      result.addAll(baseType.getDeclaredAttributes());
    return result;
  }

  @Override
  public List<JPACollectionAttribute> getDeclaredCollectionAttributes() throws ODataJPAModelException {
    lazyBuildEdmItem();
    List<JPACollectionAttribute> result = new ArrayList<>();
    for (final Entry<String, IntermediateProperty> property : declaredPropertiesList.entrySet()) {
      if (property.getValue().isCollection())
        result.add((JPACollectionAttribute) property.getValue());
    }
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null)
      result.addAll(baseType.getDeclaredCollectionAttributes());
    return result;
  }

  @Override
  public JPAPath getPath(final String externalName) throws ODataJPAModelException {
    return getPath(externalName, true);
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

  protected void buildNaviPropertyList() throws ODataJPAModelException {

    for (final Attribute<?, ?> jpaAttribute : jpaManagedType.getDeclaredAttributes()) {
      final PersistentAttributeType attributeType = jpaAttribute.getPersistentAttributeType();

      switch (attributeType) {
      case BASIC:
      case EMBEDDED:
      case ELEMENT_COLLECTION:
        break;
      case ONE_TO_MANY:
      case ONE_TO_ONE:
      case MANY_TO_MANY:
      case MANY_TO_ONE:
        if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
          final EdmDescriptionAssoziation jpaDescription = ((AnnotatedElement) jpaAttribute.getJavaMember())
              .getAnnotation(
                  EdmDescriptionAssoziation.class);
          if (jpaDescription != null) {
            final IntermediateDescriptionProperty descProperty = new IntermediateDescriptionProperty(nameBuilder,
                jpaAttribute, schema);
            declaredPropertiesList.put(descProperty.internalName, descProperty);
            break;
          }
        }
        final IntermediateNavigationProperty navProp = new IntermediateNavigationProperty(nameBuilder, this,
            jpaAttribute,
            schema);
        declaredNaviPropertiesList.put(navProp.internalName, navProp);
        break;
      default:
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_ATTRIBUTE_TYPE,
            attributeType.name());
      }
    }
  }

  protected void buildPropertyList() throws ODataJPAModelException {

    for (final Attribute<?, ?> jpaAttribute : jpaManagedType.getDeclaredAttributes()) {
      final PersistentAttributeType attributeType = jpaAttribute.getPersistentAttributeType();

      switch (attributeType) {
      case BASIC:
      case EMBEDDED:
        if (jpaAttribute instanceof SingularAttribute<?, ?>
            && ((SingularAttribute<?, ?>) jpaAttribute).isId()
            && attributeType == PersistentAttributeType.EMBEDDED) {
          final IntermediateSimpleProperty property = new IntermediateEmbeddedIdProperty(nameBuilder, jpaAttribute,
              schema);
          declaredPropertiesList.put(property.internalName, property);
        } else {
          final IntermediateSimpleProperty property = new IntermediateSimpleProperty(nameBuilder, jpaAttribute, schema);
          declaredPropertiesList.put(property.internalName, property);
        }
        break;
      case ELEMENT_COLLECTION:
        final IntermediateCollectionProperty property = new IntermediateCollectionProperty(nameBuilder,
            (PluralAttribute<?, ?, ?>) jpaAttribute, schema, this);
        declaredPropertiesList.put(property.internalName, property);
        break;
      case ONE_TO_MANY:
      case ONE_TO_ONE:
      case MANY_TO_MANY:
      case MANY_TO_ONE:
        break;
      default:
        // Attribute Type '%1$s' as of now not supported
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_ATTRIBUTE_TYPE,
            attributeType.name());
      }
    }
  }

  protected FullQualifiedName determineBaseType() throws ODataJPAModelException {

    final IntermediateStructuredType baseEntity = getBaseType();
    if (baseEntity != null && !baseEntity.isAbstract() && isAbstract())
      // Abstract entity type '%1$s' must not inherit from a non-abstract entity type '%2$s'
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.INHERITANCE_NOT_ALLOWED,
          this.internalName, baseEntity.internalName);
    return baseEntity != null ? nameBuilder.buildFQN(baseEntity.getExternalName()) : null;
  }

  protected boolean determineHasStream() throws ODataJPAModelException {
    return getStreamProperty() == null ? false : true;
  }

  protected void determineIgnore() {
    final EdmIgnore jpaIgnore = this.jpaManagedType.getJavaType().getAnnotation(EdmIgnore.class);
    if (jpaIgnore != null) {
      this.setIgnore(true);
    }
  }

  protected IntermediateStructuredType getBaseType() {
    final Class<?> baseType = jpaManagedType.getJavaType().getSuperclass();
    if (baseType != null) {
      final IntermediateStructuredType baseEntity = schema.getEntityType(baseType);
      if (baseEntity != null)
        return baseEntity;
    }
    return null;
  }

  protected IntermediateSimpleProperty getStreamProperty() throws ODataJPAModelException {
    int count = 0;
    IntermediateSimpleProperty result = null;
    for (final Entry<String, IntermediateProperty> property : declaredPropertiesList.entrySet()) {
      // Edm.Stream, or a type definition whose underlying type is Edm.Stream, cannot be used in collections or for
      // non-binding parameters to functions or actions.
      if (property.getValue().isStream()) {
        count += 1;
        result = (IntermediateSimpleProperty) property.getValue();
      }
    }
    if (this.getBaseType() != null) {
      final IntermediateSimpleProperty superResult = getBaseType().getStreamProperty();
      if (superResult != null) {
        count += 1;
        result = superResult;
      }
    }
    if (count > 1)
      // Only one stream property per entity is allowed. For %1$s %2$s have been found
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.TO_MANY_STREAMS, internalName, Integer
          .toString(count));
    return result;
  }

  List<JPAAttribute> getAssociations() throws ODataJPAModelException {
    lazyBuildEdmItem();
    final List<JPAAttribute> jpaAttributes = new ArrayList<>();
    for (final Entry<String, IntermediateNavigationProperty> naviProperty : declaredNaviPropertiesList.entrySet()) {
      final IntermediateNavigationProperty property = naviProperty.getValue();
      if (!property.ignore())
        jpaAttributes.add(property);
    }
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null)
      jpaAttributes.addAll(baseType.getAssociations());
    return jpaAttributes;
  }

  JPAAssociationAttribute getCorrespondingAssiciation(final IntermediateStructuredType sourceType,
      final String sourceRelationshipName) throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = findCorrespondingAssociation(sourceType, sourceRelationshipName);
    return jpaAttribute == null ? null : new IntermediateNavigationProperty(nameBuilder, sourceType, jpaAttribute,
        schema);
  }

  @Override
  abstract CsdlStructuralType getEdmItem() throws ODataJPAModelException;

  Map<String, JPAPath> getIntermediatePathMap() throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    return intermediatePathMap;
  }

  List<IntermediateJoinColumn> getJoinColumns(final String relationshipName) {

    final List<IntermediateJoinColumn> result = new ArrayList<>();
    final Attribute<?, ?> jpaAttribute = jpaManagedType.getAttribute(relationshipName);

    if (jpaAttribute != null) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      final JoinColumns columns = annotatedElement.getAnnotation(JoinColumns.class);
      if (columns != null) {
        for (final JoinColumn column : columns.value()) {
          result.add(new IntermediateJoinColumn(column));
        }
      } else {
        final JoinColumn column = annotatedElement.getAnnotation(JoinColumn.class);
        if (column != null) {
          result.add(new IntermediateJoinColumn(column));
        }
      }
    }
    return result;
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

    for (final Entry<String, JPAPathImpl> path : resolvedPathMap.entrySet()) {
      if (path.getValue().getDBFieldName().equals(dbFieldName))
        return path.getValue();
    }
    return null;
  }

  /**
   * Returns an property regardless if it should be ignored or not
   * @param internalName
   * @return
   * @throws ODataJPAModelException
   */
  IntermediateProperty getProperty(final String internalName) throws ODataJPAModelException {
    lazyBuildEdmItem();
    IntermediateProperty result = declaredPropertiesList.get(internalName);
    if (result == null && getBaseType() != null)
      result = getBaseType().getProperty(internalName);
    return result;
  }

  /**
   * Gets a property by its database field name.<p>
   * The resolution respects embedded types as well as super types
   * @param dbFieldName
   * @return
   * @throws ODataJPAModelException
   */
  IntermediateModelElement getPropertyByDBField(final String dbFieldName) throws ODataJPAModelException {
    buildPropertyList();
    for (final Entry<String, IntermediateProperty> declaredProperty : declaredPropertiesList.entrySet()) {
      final IntermediateProperty property = declaredProperty.getValue();
      if (property.isComplex()) {
        IntermediateProperty embeddedProperty =
            (IntermediateProperty) ((IntermediateStructuredType) property
                .getStructuredType()).getPropertyByDBField(dbFieldName);
        if (embeddedProperty != null && embeddedProperty.getDBFieldName().equals(dbFieldName))
          return embeddedProperty;
      } else if (property.getDBFieldName().equals(dbFieldName))
        return property;
    }
    if (getBaseType() != null)
      return getBaseType().getPropertyByDBField(dbFieldName);
    return null;
  }

  Map<String, JPAPathImpl> getResolvedPathMap() throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    return resolvedPathMap;
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

  private List<IntermediateJoinColumn> determineJoinColumns(final IntermediateModelElement property,
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

  private Attribute<?, ?> findCorrespondingAssociation(final IntermediateStructuredType sourceType,
      final String sourceRelationshipName) {
    Class<?> targetClass = null;

    for (final Attribute<?, ?> jpaAttribute : jpaManagedType.getAttributes()) {
      if (jpaAttribute.getPersistentAttributeType() != null
          && jpaAttribute.getJavaMember() instanceof AnnotatedElement
          && !sourceRelationshipName.equals(IntNameBuilder.buildAssociationName(jpaAttribute))) {
        if (jpaAttribute.isCollection()) {
          targetClass = ((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType().getJavaType();
        } else {
          targetClass = jpaAttribute.getJavaType();
        }
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

  private JPAPath getPath(final String externalName, final boolean respectIgnore) throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    JPAPath targetPath = resolvedPathMap.get(externalName);
    if (targetPath == null)
      targetPath = intermediatePathMap.get(externalName);
    if (targetPath == null || targetPath.ignore() && respectIgnore)
      return null;
    return targetPath;
  }

  private void lazyBuildCompleteAssociationPathMap() throws ODataJPAModelException {
    JPAAssociationPathImpl associationPath;
    lazyBuildCompletePathMap();
    // TODO check if ignore has to be handled
    if (resolvedAssociationPathMap.size() == 0) {
      for (final JPAAttribute association : getAssociations()) {
        associationPath = new JPAAssociationPathImpl((IntermediateNavigationProperty) association, this);
        resolvedAssociationPathMap.put(associationPath.getAlias(), associationPath);
      }

      for (final Entry<String, JPAPath> entity : this.intermediatePathMap.entrySet()) {
        final JPAPath attributePath = entity.getValue();
        if (attributePath.getPath().size() == 1) {
          // Only direct attributes
          final IntermediateProperty property = (IntermediateProperty) attributePath.getLeaf();
          final IntermediateStructuredType is = (IntermediateStructuredType) property.getStructuredType();

          for (final JPAAssociationPath association : is.getAssociationPathList()) {
            associationPath = new JPAAssociationPathImpl(nameBuilder, association,
                this, determineJoinColumns(property, association), property);
            resolvedAssociationPathMap.put(associationPath.getAlias(), associationPath);
          }
        }
      }
    }
  }

  private void lazyBuildCompletePathMap() throws ODataJPAModelException {
    ArrayList<JPAElement> pathList;

    lazyBuildEdmItem();
    if (resolvedPathMap.size() == 0) {
      for (final Entry<String, IntermediateProperty> propertyEntity : declaredPropertiesList.entrySet()) {
        final IntermediateProperty property = propertyEntity.getValue();
        if (property.isComplex()) {
          intermediatePathMap.put(property.getExternalName(),
              new JPAPathImpl(property.getExternalName(), null, property));
          final Map<String, JPAPath> intermediatePath = ((IntermediateStructuredType) property
              .getStructuredType()).getIntermediatePathMap();
          for (final Entry<String, JPAPath> path : intermediatePath.entrySet()) {
            pathList = new ArrayList<>();
            if (path.getValue().getLeaf() instanceof IntermediateCollectionProperty) {
              if (path.getValue().getPath().size() > 1)
                pathList.addAll(path.getValue().getPath().subList(0, path.getValue().getPath().size() - 1));
              pathList.add(new IntermediateCollectionProperty((IntermediateCollectionProperty) path.getValue()
                  .getLeaf(), this, property));
            } else
              pathList.addAll(path.getValue().getPath());
            pathList.add(0, property);
            intermediatePathMap.put(nameBuilder.buildPath(property.getExternalName(), path.getKey()),
                new JPAPathImpl(nameBuilder.buildPath(property.getExternalName(),
                    path.getKey()), null, pathList));
          }

          final Map<String, JPAPathImpl> resolvedPath = ((IntermediateStructuredType) property
              .getStructuredType()).getResolvedPathMap();

          for (final Entry<String, JPAPathImpl> path : resolvedPath.entrySet()) {
            pathList = new ArrayList<>(path.getValue().getPath());
            pathList.add(0, property);
            JPAPathImpl newPath;
            if (property.isKey()) {
              newPath = new JPAPathImpl(path.getKey(), determineDBFieldName(property, resolvedPath.get(path.getKey())),
                  pathList);
            } else {

              newPath = new JPAPathImpl(nameBuilder.buildPath(property.getExternalName(), path.getKey()),
                  determineDBFieldName(property, path.getValue()), rebuildPathList(pathList));
            }
            resolvedPathMap.put(newPath.getAlias(), newPath);

          }
        } else {
          resolvedPathMap.put(property.getExternalName(), new JPAPathImpl(property.getExternalName(), property
              .getDBFieldName(), property));
        }
      }
      final IntermediateStructuredType baseType = getBaseType();
      if (baseType != null) {
        resolvedPathMap.putAll(baseType.getResolvedPathMap());
        intermediatePathMap.putAll(baseType.getIntermediatePathMap());
      }
    }
  }

  private void lazyBuildCompleteProtectionList() throws ODataJPAModelException {
    if (protectedAttributes == null) {
      lazyBuildEdmItem();
      this.protectedAttributes = new ArrayList<>();
      for (JPAAttribute attribute : getDeclaredAttributes()) {
        if (attribute.hasProtection()) {
          if (attribute.isComplex()) {
            for (final String claimName : attribute.getProtectionClaimNames()) {
              for (final String pathName : attribute.getProtectionPath(claimName)) {
                final JPAPath path = this.getPath(pathName, false);
                if (path == null) // Annotation EdmProtctedBy found at '%2$s' of '%1$s', but the given 'path' '%3$s'...
                  throw new ODataJPAModelException(COMPLEX_PROPERTY_WRONG_PROTECTION_PATH, attribute.getInternalName(),
                      this.getTypeClass().getSimpleName(), pathName);
                protectedAttributes.add(new ProtectionInfo(path, claimName));
              }
            }
          } else
            for (final String claimName : attribute.getProtectionClaimNames()) {
              protectedAttributes.add(new ProtectionInfo(this.getPath(attribute.getExternalName(), false), claimName));
            }
        } else if (attribute.isComplex()) { // Protection at attribute overrides protection within complex
          for (final JPAProtectionInfo info : attribute.getStructuredType().getProtections()) {
            // Copy and extend path
            final String pathName = attribute.getExternalName() + JPAPath.PATH_SEPERATOR + info.getPath().getAlias();
            protectedAttributes.add(new ProtectionInfo(this.getPath(pathName, false), info.getClaimName()));
          }
        }
      }
    }
  }

  private List<JPAElement> rebuildPathList(final List<JPAElement> pathList) throws ODataJPAModelException {

    final StringBuilder path = new StringBuilder();
    for (int i = 0; i < pathList.size() - 1; i++) {
      path.append(pathList.get(i).getExternalName());
      path.append(JPAPath.PATH_SEPERATOR);
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
          && ((IntermediateCollectionProperty) leaf).getSourceType() != this)
        leaf = new IntermediateCollectionProperty((IntermediateCollectionProperty) leaf, this,
            (IntermediateProperty) pathList.get(0)); // pathList.size() - 2
      pathElements.add(leaf);
      return pathElements;
    }
  }

  private static class ProtectionInfo implements JPAProtectionInfo {
    private final JPAPath pathToAttribute;
    private final String claimName;

    public ProtectionInfo(final JPAPath path, final String claimName) {
      this.pathToAttribute = path;
      this.claimName = claimName;
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
    public String toString() {
      return "ProtectionInfo [pathToAttribute=" + pathToAttribute.getAlias() + ", claimName=" + claimName + "]";
    }

  }
}