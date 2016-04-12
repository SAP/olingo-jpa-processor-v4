package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

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
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssozation;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

abstract class IntermediateStructuredType extends IntermediateModelElement implements JPAStructuredType {
// 
  protected final Map<String, IntermediateProperty> declaredPropertiesList;
  protected final Map<String, IntermediateNavigationProperty> declaredNaviPropertiesList;
  protected final Map<String, JPAPathImpl> resolvedPathMap;
  protected final Map<String, JPAPathImpl> intermediatePathMap;
  protected final Map<String, JPAAssociationPathImpl> resolvedAssociationPathMap;
  protected final ManagedType<?> jpaManagedType;
  protected final IntermediateSchema schema;
  // private IntermediateStructuredType baseType;

  IntermediateStructuredType(final JPAEdmNameBuilder nameBuilder, final ManagedType<?> jpaManagedType,
      final IntermediateSchema schema) throws ODataJPAModelException {

    super(nameBuilder, IntNameBuilder.buildStructuredTypeName(jpaManagedType.getJavaType()));
    this.declaredPropertiesList = new HashMap<String, IntermediateProperty>();
    this.resolvedPathMap = new HashMap<String, JPAPathImpl>();
    this.intermediatePathMap = new HashMap<String, JPAPathImpl>();
    // TODO fill early NavigationPropertyList w/o getting edmtype
    this.declaredNaviPropertiesList = new HashMap<String, IntermediateNavigationProperty>();
    this.resolvedAssociationPathMap = new HashMap<String, JPAAssociationPathImpl>();
    this.jpaManagedType = jpaManagedType;
    this.schema = schema;
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
    else if (result != null && ((IntermediateProperty) result).ignore())
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
  public JPAAssociationPath getDeclaredAssociation(final JPAAssociationPath associationPath)
      throws ODataJPAModelException {
    lazyBuildCompleteAssociationPathMap();

    if (resolvedAssociationPathMap.containsKey(associationPath.getAlias()))
      return resolvedAssociationPathMap.get(associationPath.getAlias());
//    boolean found = false;
//    StringBuffer pathName = new StringBuffer();
//    for (final JPAElement jpaElement : associationPath.getPath()) {
//      if (found) {
//        pathName.append(jpaElement.getExternalName());
//        pathName.append(JPAPath.PATH_SEPERATOR);
//      }
//      if (jpaElement.getExternalFQN().getFullQualifiedNameAsString().equals(getExternalFQN()
//          .getFullQualifiedNameAsString()))
//        found = true;
//    }
//    if (found) {
//      pathName.deleteCharAt(pathName.length() - 1);
//      return resolvedAssociationPathMap.get(pathName.toString());
//    }
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null)
      return baseType.getDeclaredAssociation(associationPath);
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
        if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
          final EdmDescriptionAssozation jpaDescription = ((AnnotatedElement) jpaAttribute.getJavaMember())
              .getAnnotation(
                  EdmDescriptionAssozation.class);
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

  protected FullQualifiedName determineBaseType() throws ODataJPAModelException {

    final IntermediateStructuredType baseEntity = getBaseType();
    return baseEntity != null ? nameBuilder.buildFQN(baseEntity.getExternalName()) : null;
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

  /**
   * Method follows resolved semantic
   * 
   * @param dbFieldName
   * @return
   * @throws ODataJPAModelException
   */
  JPAPath getAttributeByDBField(final String dbFieldName) throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    for (final String internalName : resolvedPathMap.keySet()) {
      final JPAPath property = resolvedPathMap.get(internalName);
      if (property.getDBFieldName().equals(dbFieldName))
        return property;
    }
    return null;
  }

  IntermediateNavigationProperty getCorrespondingAssiciation(final IntermediateStructuredType sourceType,
      final String sourceRelationshipName) {
    final Attribute<?, ?> jpaAttribute = findCorrespondingAssociation(sourceType, sourceRelationshipName);
    return jpaAttribute == null ? null : new IntermediateNavigationProperty(nameBuilder, sourceType, jpaAttribute,
        schema);
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

  Map<String, JPAPathImpl> getResolvedPathMap() throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    return resolvedPathMap;
  }

  private String determineDBFieldName(final IntermediateProperty property, final JPAPath jpaPath) {
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

  private List<IntermediateJoinColumn> determineJoinColumns(final IntermediateProperty property,
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