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
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;

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
  protected final HashMap<String, IntermediateProperty> declaredPropertiesList;
  protected final HashMap<String, IntermediateNavigationProperty> declaredNaviPropertiesList;
  protected final HashMap<String, JPAPathImpl> resolvedPathMap;
  protected final HashMap<String, JPAPathImpl> intermediatePathMap;
  protected final HashMap<String, JPAAssociationPathImpl> resolvedAssociationPathMap;
  protected final ManagedType<?> jpaManagedType;
  protected final IntermediateSchema schema;
  // private IntermediateStructuredType baseType;

  IntermediateStructuredType(JPAEdmNameBuilder nameBuilder, ManagedType<?> jpaManagedType,
      IntermediateSchema schema) throws ODataJPAModelException {

    super(nameBuilder, intNameBuilder.buildStructuredTypeName(jpaManagedType.getJavaType()));
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
  public JPAAssociationAttribute getAssociation(String internalName) throws ODataJPAModelException {
    for (JPAAttribute attribute : this.getAssociations()) {
      if (attribute.getInternalName().equals(internalName))
        return (JPAAssociationAttribute) attribute;
    }
    return null;
  }

  @Override
  public JPAAssociationPath getAssociationPath(String externalName) throws ODataJPAModelException {
    lazyBuildCompleteAssociationPathMap();
    return resolvedAssociationPathMap.get(externalName);
  }

  @Override
  public List<JPAAssociationPath> getAssociationPathList() throws ODataJPAModelException {
    lazyBuildCompleteAssociationPathMap();
    List<JPAAssociationPath> associationList = new ArrayList<JPAAssociationPath>();

    for (String externalName : resolvedAssociationPathMap.keySet()) {
      associationList.add(resolvedAssociationPathMap.get(externalName));
    }
    return associationList;
  }

  @Override
  public JPAAttribute getAttribute(String internalName) throws ODataJPAModelException {
    lazyBuildEdmItem();
    JPAAttribute result = declaredPropertiesList.get(internalName);
    if (result == null && getBaseType() != null)
      result = getBaseType().getAttribute(internalName);
    else if (result != null && ((IntermediateProperty) result).ignore())
      return null;
    return result;
  }

  @Override
  public JPAAssociationPath getDeclaredAssociation(String externalName) throws ODataJPAModelException {
    lazyBuildCompleteAssociationPathMap();
    for (String internalName : declaredNaviPropertiesList.keySet()) {
      if (externalName.equals(declaredNaviPropertiesList.get(internalName).getExternalName()))
        return resolvedAssociationPathMap.get(externalName);
    }
    IntermediateStructuredType baseType = getBaseType();
    if (baseType != null)
      return baseType.getDeclaredAssociation(externalName);
    return null;
  }

  @Override
  public FullQualifiedName getExternalFQN() {
    return nameBuilder.buildFQN(getExternalName());
  }

  @Override
  public JPAPath getPath(String externalName) throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    JPAPath targetPath = resolvedPathMap.get(externalName);
    if (targetPath == null)
      targetPath = intermediatePathMap.get(externalName);
    if (targetPath.ignore())
      return null;
    return targetPath;
  }

  @Override
  public List<JPAPath> getPathList() throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    List<JPAPath> pathList = new ArrayList<JPAPath>();
    for (String externalName : resolvedPathMap.keySet()) {
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

    for (Attribute<?, ?> jpaAttribute : jpaManagedType.getDeclaredAttributes()) {
      PersistentAttributeType attributeType = jpaAttribute.getPersistentAttributeType();

      switch (attributeType) {
      case BASIC:
      case EMBEDDED:
        IntermediateProperty property = new IntermediateProperty(nameBuilder, jpaAttribute, schema);
        declaredPropertiesList.put(property.internalName, property);
        break;
      case ONE_TO_MANY:
      case ONE_TO_ONE:
      case MANY_TO_MANY:
      case MANY_TO_ONE:
        if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
          EdmDescriptionAssozation jpaDescription = ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(
              EdmDescriptionAssozation.class);
          if (jpaDescription != null) {
            IntermediateDescriptionProperty descProperty = new IntermediateDescriptionProperty(nameBuilder,
                jpaAttribute, schema);
            declaredPropertiesList.put(descProperty.internalName, descProperty);
            break;
          }
        }
        IntermediateNavigationProperty navProp = new IntermediateNavigationProperty(nameBuilder, this, jpaAttribute,
            schema);
        declaredNaviPropertiesList.put(navProp.internalName, navProp);
        break;
      default:
        throw ODataJPAModelException.throwException(ODataJPAModelException.NOT_SUPPORTED_ATTRIBUTE_TYPE,
            "Attribute Type as of now not supported");
      }
    }
  }

  protected FullQualifiedName determineBaseType() throws ODataJPAModelException {

    IntermediateStructuredType baseEntity = getBaseType();
    return baseEntity != null ? nameBuilder.buildFQN(baseEntity.getExternalName()) : null;
  }

  protected IntermediateStructuredType getBaseType() throws ODataJPAModelException {
    Class<?> baseType = jpaManagedType.getJavaType().getSuperclass();
    if (baseType != null) {
      IntermediateStructuredType baseEntity = schema.getEntityType(baseType);
      if (baseEntity != null)
        return baseEntity;
    }
    return null;
  }

  List<JPAAttribute> getAssociations() throws ODataJPAModelException {
    lazyBuildEdmItem();
    List<JPAAttribute> jpaAttributes = new ArrayList<JPAAttribute>();
    for (String internalName : declaredNaviPropertiesList.keySet()) {
      IntermediateNavigationProperty property = declaredNaviPropertiesList.get(internalName);
      if (!property.ignore())
        jpaAttributes.add(property);
    }
    IntermediateStructuredType baseType = getBaseType();
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
  JPAPath getAttributeByDBField(String dbFieldName) throws ODataJPAModelException {
    lazyBuildCompletePathMap();
    for (String internalName : resolvedPathMap.keySet()) {
      JPAPath property = resolvedPathMap.get(internalName);
      if (property.getDBFieldName().equals(dbFieldName))
        return property;
    }
    return null;
  }

  IntermediateNavigationProperty getCorrespondingNavigationProperty(IntermediateStructuredType sourceType) {
    Attribute<?, ?> jpaAttribute = findCorrespondingRelationship(sourceType);
    return jpaAttribute == null ? null : new IntermediateNavigationProperty(nameBuilder, sourceType, jpaAttribute,
        schema);
  }

  private Attribute<?, ?> findCorrespondingRelationship(IntermediateStructuredType sourceType) {
    Class<?> targetClass = null;

    for (Attribute<?, ?> jpaAttribute : jpaManagedType.getAttributes()) {
      if (jpaAttribute.getPersistentAttributeType() != null &&
          jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
        if (jpaAttribute.isCollection()) {
          targetClass = ((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType().getJavaType();
        } else {
          targetClass = jpaAttribute.getJavaType();
        }
        if (targetClass.equals(sourceType.getTypeClass())) {
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

  List<IntermediateJoinColumn> getJoinColumns(IntermediateStructuredType sourceType) {

    List<IntermediateJoinColumn> result = new ArrayList<IntermediateJoinColumn>();
    Attribute<?, ?> jpaAttribute = findCorrespondingRelationship(sourceType);

    if (jpaAttribute != null) {
      AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      JoinColumns columns = annotatedElement.getAnnotation(JoinColumns.class);
      if (columns != null) {
        for (JoinColumn column : columns.value()) {
          result.add(new IntermediateJoinColumn(column));
        }
      } else {
        JoinColumn column = annotatedElement.getAnnotation(JoinColumn.class);
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

  private String determineDBFieldName(IntermediateProperty property, JPAPath jpaPath) {
    Attribute<?, ?> jpaAttribute = jpaManagedType.getAttribute(property.getInternalName());
    if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      AnnotatedElement a = (AnnotatedElement) jpaAttribute.getJavaMember();
      AttributeOverrides overwriteList = a.getAnnotation(AttributeOverrides.class);
      if (overwriteList != null) {
        for (AttributeOverride overwrite : overwriteList.value()) {
          if (overwrite.name().equals(jpaPath.getLeaf().getInternalName()))
            return overwrite.column().name();
        }
      } else {
        AttributeOverride overwrite = a.getAnnotation(AttributeOverride.class);
        if (overwrite != null) {
          if (overwrite.name().equals(jpaPath.getLeaf().getInternalName()))
            return overwrite.column().name();
        }
      }
    }
    return jpaPath.getDBFieldName();
  }

  private List<IntermediateJoinColumn> determineJoinColumns(IntermediateProperty property,
      JPAAssociationPath association) {
    List<IntermediateJoinColumn> result = new ArrayList<IntermediateJoinColumn>();

    Attribute<?, ?> jpaAttribute = jpaManagedType.getAttribute(property.getInternalName());
    if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      AnnotatedElement a = (AnnotatedElement) jpaAttribute.getJavaMember();
      if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
        AssociationOverrides overwriteList = a.getAnnotation(AssociationOverrides.class);
        if (overwriteList != null) {
          for (AssociationOverride overwrite : overwriteList.value()) {
            if (overwrite.name().equals(association.getLeaf().getInternalName())) {
              for (JoinColumn column : overwrite.joinColumns())
                result.add(new IntermediateJoinColumn(column));
            }
          }
        } else {
          AssociationOverride overwrite = a.getAnnotation(AssociationOverride.class);
          if (overwrite != null) {
            if (overwrite.name().equals(association.getLeaf().getInternalName())) {
              for (JoinColumn column : overwrite.joinColumns())
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
      for (JPAAttribute association : getAssociations()) {
        associationPath = new JPAAssociationPathImpl((IntermediateNavigationProperty) association, this);
        resolvedAssociationPathMap.put(associationPath.getAlias(), associationPath);
      }

      for (String key : this.intermediatePathMap.keySet()) {
        JPAPath attributePath = this.intermediatePathMap.get(key);
        if (attributePath.getPath().size() == 1) {
          // Only direct attributes
          IntermediateProperty property = (IntermediateProperty) attributePath.getLeaf();
          IntermediateStructuredType is = (IntermediateStructuredType) property.getStructuredType();

          for (JPAAssociationPath association : is.getAssociationPathList()) {
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
      for (String internalName : declaredPropertiesList.keySet()) {
        IntermediateProperty property = declaredPropertiesList.get(internalName);
        // if (!property.ignore()) {
        if (property.isComplex()) {
          intermediatePathMap.put(property.getExternalName(),
              new JPAPathImpl(property.getExternalName(), null, property));
          Map<String, JPAPathImpl> intermediatePath = ((IntermediateStructuredType) property
              .getStructuredType()).getIntermediatePathMap();
          for (String externalName : intermediatePath.keySet()) {
            pathList = new ArrayList<JPAElement>(intermediatePath.get(externalName).getPath());
            pathList.add(0, property);
            intermediatePathMap.put(nameBuilder.buildPath(property.getExternalName(), externalName),
                new JPAPathImpl(nameBuilder.buildPath(property.getExternalName(),
                    externalName), null, pathList));
          }

          Map<String, JPAPathImpl> resolvedPath = ((IntermediateStructuredType) property
              .getStructuredType()).getResolvedPathMap();

          for (String externalName : resolvedPath.keySet()) {
            pathList = new ArrayList<JPAElement>(resolvedPath.get(externalName).getPath());
            pathList.add(0, property);
            JPAPathImpl newPath = new JPAPathImpl(nameBuilder.buildPath(property.getExternalName(), externalName),
                determineDBFieldName(property, resolvedPath.get(externalName)), pathList);
            resolvedPathMap.put(newPath.getAlias(), newPath);

          }
        } else {
          resolvedPathMap.put(property.getExternalName(), new JPAPathImpl(property.getExternalName(), property
              .getDBFieldName(), property));
        }
      }
    }
    IntermediateStructuredType baseType = getBaseType();
    if (baseType != null) {
      resolvedPathMap.putAll(baseType.getResolvedPathMap());
      intermediatePathMap.putAll(baseType.getIntermediatePathMap());
    }
    // }

  }
}