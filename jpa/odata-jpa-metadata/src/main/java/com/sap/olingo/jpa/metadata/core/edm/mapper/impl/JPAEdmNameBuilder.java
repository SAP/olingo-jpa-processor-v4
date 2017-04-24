package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

public final class JPAEdmNameBuilder {
  // V2 NameBuilder: package org.apache.olingo.odata2.jpa.processor.core.access.model
  private static final String ENTITY_CONTAINER_SUFFIX = "Container";
  private static final String ENTITY_SET_SUFFIX = "s";

  public static String firstToLower(final String substring) {
    return Character.toLowerCase(substring.charAt(0)) + substring.substring(1);
  }

  public static String firstToUpper(final String jpaAttributeName) {
    return Character.toUpperCase(jpaAttributeName.charAt(0)) + jpaAttributeName.substring(1);
  }

  final private String namespace;

  public JPAEdmNameBuilder(final String namespace) {
    super();
    this.namespace = namespace;
  }

  /*
   * ************************************************************************
   * EDM Complex Type Name - RULES
   * ************************************************************************
   * Complex Type Name = JPA Embeddable Type Simple Name.
   * ************************************************************************
   * EDM Complex Type Name - RULES
   * ************************************************************************
   */
  final public String buildComplexTypeName(final Attribute<?, ?> jpaAttribute) {
    return jpaAttribute.getJavaType().getSimpleName();
  }

  final public String buildComplexTypeName(final EmbeddableType<?> jpaEnbeddedType) {
    return jpaEnbeddedType.getJavaType().getSimpleName();
  }

  /*
   * ************************************************************************
   * EDM EntityContainer Name - RULES
   * ************************************************************************
   * Entity Container Name = EDM Namespace + Literal "Container"
   * Container names are simple identifiers:
   * http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-
   * complete.html#_SimpleIdentifier
   * So contain only letter, digits and underscores. However namespaces
   * can contain also dots => eliminate dots and convert to camel case
   * ************************************************************************
   * EDM EntityContainer Name - RULES
   * ************************************************************************
   */
  public String buildContainerName() {
    final StringBuffer containerName = new StringBuffer();
    final String[] elements = namespace.split("\\.");
    for (final String element : elements) {
      containerName.append(firstToUpper(element));
    }
    containerName.append(ENTITY_CONTAINER_SUFFIX);
    return containerName.toString();
    // return firstToUpper(namespace) + ENTITY_CONTAINER_SUFFIX;
  }

  /*
   * ************************************************************************
   * EDM EntitySet Name - RULES
   * ************************************************************************
   * The naming bases on the assumption that English nouns are used.
   * Entity Set Name = JPA Entity Type Name + Literal "s"
   * ************************************************************************
   * EDM EntitySet Name - RULES
   * ************************************************************************
   */
  final public String buildEntitySetName(final CsdlEntityType entityType) {
    return buildEntitySetName(entityType.getName());
  }

  final public String buildEntitySetName(final FullQualifiedName entityTypeFQName) {
    return buildEntitySetName(entityTypeFQName.getName());
  }

  final public String buildEntitySetName(final String entityTypeName) {
    if (entityTypeName.charAt(entityTypeName.length() - 1) == 'y'
        && entityTypeName.charAt(entityTypeName.length() - 2) != 'a'
        && entityTypeName.charAt(entityTypeName.length() - 2) != 'e'
        && entityTypeName.charAt(entityTypeName.length() - 2) != 'i'
        && entityTypeName.charAt(entityTypeName.length() - 2) != 'o'
        && entityTypeName.charAt(entityTypeName.length() - 2) != 'u') {
      return entityTypeName.substring(0, entityTypeName.length() - 1) + "ie" + ENTITY_SET_SUFFIX;
    }
    return entityTypeName + ENTITY_SET_SUFFIX;
  }

  /*
   * ************************************************************************
   * EDM EntityType Name - RULES
   * ************************************************************************
   * EDM Entity Type Name = JPA Entity Name EDM Entity Type Internal Name =
   * JPA Entity Name
   * ************************************************************************
   * EDM Entity Type Name - RULES
   * ************************************************************************
   */
  public String buildEntityTypeName(final EntityType<?> jpaEntityType) {
    return jpaEntityType.getName();
  }

  final public FullQualifiedName buildFQN(final String name) {
    final FullQualifiedName fqName = new FullQualifiedName(buildNamespace(), name);
    return fqName;
  }

  /*
   * ************************************************************************
   * EDM Schema Name - RULES
   * ************************************************************************
   * Java Persistence Unit name is set as Schema's Namespace
   * ************************************************************************
   * EDM Schema Name - RULES
   * ************************************************************************
   */
  final public String buildNamespace() {
    return namespace;
  }

  /*
   * ************************************************************************
   * EDM Navigation Property Binding - RULES
   * ************************************************************************
   * V4 specification states:
   * A navigation property binding MUST name a navigation property of the
   * entity set’s, singleton's, or containment navigation property's entity
   * type or one of its subtypes in the Path attribute. If the navigation
   * property is defined on a subtype, the path attribute MUST contain the
   * QualifiedName of the subtype, followed by a forward slash, followed by
   * the navigation property name. If the navigation property is defined on
   * a complex type used in the definition of the entity set’s entity type,
   * the path attribute MUST contain a forward-slash separated list of complex
   * property names and qualified type names that describe the path leading
   * to the navigation property.
   * 
   * http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-
   * complete.html#_Toc406398035
   * ************************************************************************
   * EDM Property Name - RULES
   * ************************************************************************
   */
  // TODO respect subtype name
  final public String buildNaviPropertyBindingName(final JPAAssociationPath associationPath,
      final JPAAttribute parent) {
    final StringBuffer name = new StringBuffer();

    name.append(parent.getExternalName());
    for (final JPAElement pathElement : associationPath.getPath()) {
      name.append(JPAPath.PATH_SEPERATOR);
      name.append(pathElement.getExternalName());

    }
    return name.toString();
  }

  /*
   * ************************************************************************
   * EDM Navigation Property Name - RULES
   * ************************************************************************
   * V2 rules were navigation target entity name + Details. In case of
   * multiple navigation properties with the same target an counter was added
   * 
   * New rules for V4:
   * OData requires: "The name of the navigation property MUST be unique
   * within the set of structural and navigation properties of the containing
   * structured type and any of its base types."
   * The is fulfilled by taking the property name it self. In addition it
   * could be expected that, in case of multiple navigation properties with
   * the same target, the name is more expressive, if the property name is
   * well chosen;-)
   * ************************************************************************
   * EDM Navigation Property Name - RULES
   * ************************************************************************
   */
  final public String buildNaviPropertyName(final Attribute<?, ?> jpaAttribute) {
    return buildPropertyName(jpaAttribute.getName());
  }

  final public String buildPath(final String pathRoot, final String pathElement) {
    return pathRoot + JPAPath.PATH_SEPERATOR + pathElement;
  }

  final public FullQualifiedName buildPropertyFQN(final String jpaAttributeName) {
    return buildFQN(buildPropertyName(jpaAttributeName));
  }

  /*
   * ************************************************************************
   * EDM Property Name - RULES
   * ************************************************************************
   * OData Property Names are represented in Camel Case. The first character
   * of JPA Attribute Name is converted to an UpperCase Character and set as
   * OData Property Name. JPA Attribute Name is set as Internal Name for OData
   * Property. The Column name (annotated as @Column(name="x")) is set as
   * column name in the mapping object.
   * ************************************************************************
   * EDM Property Name - RULES
   * ************************************************************************
   */
  final public String buildPropertyName(final String jpaAttributeName) {
    return firstToUpper(jpaAttributeName);
  }

}
