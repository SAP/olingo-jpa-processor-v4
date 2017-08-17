package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssoziation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

abstract class IntermediateStructuredType extends IntermediateModelElement implements JPAStructuredType {
// 
  protected final Map<String, IntermediateProperty> declaredPropertiesList;
  protected final Map<String, IntermediateNavigationProperty> declaredNaviPropertiesList;
  protected final Map<String, JPAPathImpl> resolvedPathMap;
  protected final Map<String, JPAPathImpl> intermediatePathMap;
  protected final Map<String, JPAAssociationPathImpl> resolvedAssociationPathMap;
  protected final ManagedType<?> jpaManagedType;
  protected final IntermediateSchema schema;

  IntermediateStructuredType(final JPAEdmNameBuilder nameBuilder, final ManagedType<?> jpaManagedType,
      final IntermediateSchema schema) throws ODataJPAModelException {

    super(nameBuilder, IntNameBuilder.buildStructuredTypeName(jpaManagedType.getJavaType()));
    this.declaredPropertiesList = new HashMap<String, IntermediateProperty>();
    this.resolvedPathMap = new HashMap<String, JPAPathImpl>();
    this.intermediatePathMap = new HashMap<String, JPAPathImpl>();
    this.declaredNaviPropertiesList = new HashMap<String, IntermediateNavigationProperty>();
    this.resolvedAssociationPathMap = new HashMap<String, JPAAssociationPathImpl>();
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
    final List<JPAAssociationPath> associationList = new ArrayList<JPAAssociationPath>();

    for (final String externalName : resolvedAssociationPathMap.keySet()) {
      associationList.add(resolvedAssociationPathMap.get(externalName));
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
  public List<JPAAttribute> getAttributes() throws ODataJPAModelException {
    lazyBuildEdmItem();
    final List<JPAAttribute> result = new ArrayList<JPAAttribute>();
    for (final String propertyKey : declaredPropertiesList.keySet()) {
      final IntermediateProperty attribute = declaredPropertiesList.get(propertyKey);
      if (!attribute.ignore())
        result.add(attribute);
    }
    if (getBaseType() != null)
      result.addAll(getBaseType().getAttributes());
    return result;
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
    for (final String internalName : declaredNaviPropertiesList.keySet()) {
      if (externalName.equals(declaredNaviPropertiesList.get(internalName).getExternalName()))
        return resolvedAssociationPathMap.get(externalName);
    }
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null)
      return baseType.getDeclaredAssociation(externalName);
    return null;
  }

  @Override
  public JPAPath getPath(final String externalName) throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    JPAPath targetPath = resolvedPathMap.get(externalName);
    if (targetPath == null)
      targetPath = intermediatePathMap.get(externalName);
    if (targetPath == null || targetPath.ignore())
      return null;
    return targetPath;
  }

  @Override
  public List<JPAPath> getPathList() throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    final List<JPAPath> pathList = new ArrayList<JPAPath>();
    for (final String externalName : resolvedPathMap.keySet()) {
      if (!resolvedPathMap.get(externalName).ignore())
        pathList.add(resolvedPathMap.get(externalName));
    }
    return pathList;
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
          final IntermediateProperty property = new IntermediateEmbeddedIdProperty(nameBuilder, jpaAttribute, schema);
          declaredPropertiesList.put(property.internalName, property);
        } else {
          final IntermediateProperty property = new IntermediateProperty(nameBuilder, jpaAttribute, schema);
          declaredPropertiesList.put(property.internalName, property);
        }
        break;
      case ONE_TO_MANY:
      case ONE_TO_ONE:
      case MANY_TO_MANY:
      case MANY_TO_ONE:
        break;
      default:
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_ATTRIBUTE_TYPE,
            attributeType.name());
      }
    }
  }

  protected FullQualifiedName determineBaseType() throws ODataJPAModelException {

    final IntermediateStructuredType baseEntity = getBaseType();
    if (baseEntity != null && !baseEntity.isAbstract() && isAbstract())
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.INHERITANCE_NOT_ALLOWED,
          this.internalName, baseEntity.internalName);
    return baseEntity != null ? nameBuilder.buildFQN(baseEntity.getExternalName()) : null;
  }

  protected void determineIgnore() {
    final EdmIgnore jpaIgnore = ((AnnotatedElement) this.jpaManagedType.getJavaType()).getAnnotation(EdmIgnore.class);
    if (jpaIgnore != null) {
      this.setIgnore(true);
    }
  }

  protected boolean determineHasStream() throws ODataJPAModelException {
    return getStreamProperty() == null ? false : true;
  }

  protected IntermediateStructuredType getBaseType() throws ODataJPAModelException {
    final Class<?> baseType = jpaManagedType.getJavaType().getSuperclass();
    if (baseType != null) {
      final IntermediateStructuredType baseEntity = schema.getEntityType(baseType);
      if (baseEntity != null)
        return baseEntity;
    }
    return null;
  }

  protected IntermediateProperty getStreamProperty() throws ODataJPAModelException {
    int count = 0;
    IntermediateProperty result = null;
    for (final String internalName : declaredPropertiesList.keySet()) {
      if (declaredPropertiesList.get(internalName).isStream()) {
        count += 1;
        result = declaredPropertiesList.get(internalName);
      }
    }
    if (this.getBaseType() != null) {
      final IntermediateProperty superResult = getBaseType().getStreamProperty();
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
    final List<JPAAttribute> jpaAttributes = new ArrayList<JPAAttribute>();
    for (final String internalName : declaredNaviPropertiesList.keySet()) {
      final IntermediateNavigationProperty property = declaredNaviPropertiesList.get(internalName);
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

  Map<String, JPAPathImpl> getIntermediatePathMap() throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    return intermediatePathMap;
  }

  List<IntermediateJoinColumn> getJoinColumns(final IntermediateStructuredType sourceType,
      final String relationshipName) {

    final List<IntermediateJoinColumn> result = new ArrayList<IntermediateJoinColumn>();
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
    for (final String internalName : resolvedPathMap.keySet()) {
      final JPAPath property = resolvedPathMap.get(internalName);
      if (property.getDBFieldName().equals(dbFieldName))
        return property;
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
    for (final String internalName : declaredPropertiesList.keySet()) {
      final IntermediateProperty property = declaredPropertiesList.get(internalName);
      if (property.isComplex()) {
        IntermediateProperty embeddedProperty = (IntermediateProperty) ((IntermediateStructuredType) property
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
        if (overwrite != null) {
          if (overwrite.name().equals(jpaPath.getLeaf().getInternalName()))
            return overwrite.column().name();
        }
      }
    }
    return jpaPath.getDBFieldName();
  }

  private List<IntermediateJoinColumn> determineJoinColumns(final IntermediateModelElement property,
      final JPAAssociationPath association) {
    final List<IntermediateJoinColumn> result = new ArrayList<IntermediateJoinColumn>();

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
          if (overwrite != null) {
            if (overwrite.name().equals(association.getLeaf().getInternalName())) {
              for (final JoinColumn column : overwrite.joinColumns())
                result.add(new IntermediateJoinColumn(column));
            }
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

  private void lazyBuildCompleteAssociationPathMap() throws ODataJPAModelException {
    JPAAssociationPathImpl associationPath;
    lazyBuildCompletePathMap();
    // TODO check if ignore has to be handled
    if (resolvedAssociationPathMap.size() == 0) {
      for (final JPAAttribute association : getAssociations()) {
        associationPath = new JPAAssociationPathImpl((IntermediateNavigationProperty) association, this);
        resolvedAssociationPathMap.put(associationPath.getAlias(), associationPath);
      }

      for (final String key : this.intermediatePathMap.keySet()) {
        final JPAPath attributePath = this.intermediatePathMap.get(key);
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
      for (final String internalName : declaredPropertiesList.keySet()) {
        final IntermediateProperty property = declaredPropertiesList.get(internalName);
        // if (!property.ignore()) {
        if (property.isComplex()) {
          intermediatePathMap.put(property.getExternalName(),
              new JPAPathImpl(property.getExternalName(), null, property));
          final Map<String, JPAPathImpl> intermediatePath = ((IntermediateStructuredType) property
              .getStructuredType()).getIntermediatePathMap();
          for (final String externalName : intermediatePath.keySet()) {
            pathList = new ArrayList<JPAElement>(intermediatePath.get(externalName).getPath());
            pathList.add(0, property);
            intermediatePathMap.put(nameBuilder.buildPath(property.getExternalName(), externalName),
                new JPAPathImpl(nameBuilder.buildPath(property.getExternalName(),
                    externalName), null, pathList));
          }

          final Map<String, JPAPathImpl> resolvedPath = ((IntermediateStructuredType) property
              .getStructuredType()).getResolvedPathMap();

          for (final String externalName : resolvedPath.keySet()) {
            pathList = new ArrayList<JPAElement>(resolvedPath.get(externalName).getPath());
            pathList.add(0, property);
            JPAPathImpl newPath;
            if (property.isKey()) {
              newPath = new JPAPathImpl(externalName, determineDBFieldName(property, resolvedPath.get(externalName)),
                  pathList);
            } else {
              newPath = new JPAPathImpl(nameBuilder.buildPath(property.getExternalName(), externalName),
                  determineDBFieldName(property, resolvedPath.get(externalName)), pathList);
            }
            resolvedPathMap.put(newPath.getAlias(), newPath);

          }
        } else {
          resolvedPathMap.put(property.getExternalName(), new JPAPathImpl(property.getExternalName(), property
              .getDBFieldName(), property));
        }
      }
    }
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null) {
      resolvedPathMap.putAll(baseType.getResolvedPathMap());
      intermediatePathMap.putAll(baseType.getIntermediatePathMap());
    }
    // }

  }
}