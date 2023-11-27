package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;

public final record JPADefaultEdmNameBuilder(@Nonnull String namespace) implements JPAEdmNameBuilder {
  private static final int DISTANCE_NEXT_TO_LAST_CHAR = 2;
  private static final int DISTANCE_LAST_CHAR = 1;
  // V2 NameBuilder: package org.apache.olingo.odata2.jpa.processor.core.access.model
  private static final String ENTITY_CONTAINER_SUFFIX = "Container";
  private static final String ENTITY_SET_SUFFIX = "s";

  public static String firstToLower(final String substring) {
    return Character.toLowerCase(substring.charAt(0)) + substring.substring(1);
  }

  public static String firstToUpper(final String jpaAttributeName) {
    return Character.toUpperCase(jpaAttributeName.charAt(0)) + jpaAttributeName.substring(1);
  }

  /**
   * EDM Complex Type Name - RULE:
   * <p>
   * Use JPA Embeddable Type Simple Name as Complex Type Name
   */
  @Override
  public final String buildComplexTypeName(final EmbeddableType<?> jpaEmbeddedType) {
    return jpaEmbeddedType.getJavaType().getSimpleName();
  }

  /**
   * EDM EntityContainer Name - RULE:
   * <p>
   * The Entity Container Name is build of EDM Namespace + Literal "Container". Container names are simple identifiers,
   * so contain only letter, digits and underscores. However namespaces
   * can contain also dots => eliminate dots and convert to camel case.
   */
  @Override
  public String buildContainerName() {
    final StringBuilder containerName = new StringBuilder();
    final String[] elements = namespace.split("\\.");
    for (final String element : elements) {
      containerName.append(firstToUpper(element));
    }
    containerName.append(ENTITY_CONTAINER_SUFFIX);
    return containerName.toString();
  }

  /**
   * EDM EntitySet Name - RULE:
   * <p>
   * Use plural of entity type name. The naming bases on the assumption that English nouns are used.<br>
   * Entity Set Name = JPA Entity Type Name + Literal "s"
   */
  @Override
  public final String buildEntitySetName(final String entityTypeName) {
    if (entityTypeName.charAt(entityTypeName.length() - DISTANCE_LAST_CHAR) == 'y'
        && entityTypeName.charAt(entityTypeName.length() - DISTANCE_NEXT_TO_LAST_CHAR) != 'a'
        && entityTypeName.charAt(entityTypeName.length() - DISTANCE_NEXT_TO_LAST_CHAR) != 'e'
        && entityTypeName.charAt(entityTypeName.length() - DISTANCE_NEXT_TO_LAST_CHAR) != 'i'
        && entityTypeName.charAt(entityTypeName.length() - DISTANCE_NEXT_TO_LAST_CHAR) != 'o'
        && entityTypeName.charAt(entityTypeName.length() - DISTANCE_NEXT_TO_LAST_CHAR) != 'u') {
      return entityTypeName.substring(0, entityTypeName.length() - DISTANCE_LAST_CHAR) + "ie" + ENTITY_SET_SUFFIX;
    }
    return entityTypeName + ENTITY_SET_SUFFIX;
  }

  /**
   * EDM EntityType Name - RULE:
   * <p>
   * Use JPA Entity Name as EDM Entity Type Name
   */
  @Override
  public String buildEntityTypeName(final EntityType<?> jpaEntityType) {
    return jpaEntityType.getName();
  }

  @Override
  public final String getNamespace() {
    return namespace;
  }

  /**
   * EDM Navigation Property Name - RULE:
   * <p>
   * OData requires: "The name of the navigation property MUST be unique
   * within the set of structural and navigation properties of the containing
   * structured type and any of its base types."
   * This is fulfilled by taking the property name it self.
   * @param jpaAttribute
   * @return
   */
  @Override
  public final String buildNaviPropertyName(final Attribute<?, ?> jpaAttribute) {
    return buildPropertyName(jpaAttribute.getName());
  }

  /**
   * EDM Property Name - RULE:
   * <p>
   * OData Property Names are represented in Camel Case. The first character
   * of JPA Attribute Name is converted to an UpperCase Character.
   * @param jpaAttributeName
   * @return
   */
  @Override
  public final String buildPropertyName(final String jpaAttributeName) {
    return firstToUpper(jpaAttributeName);
  }

  /**
   * Convert the internal name of a java based operation into the external entity data model name.
   * @param internalOperationName
   * @return
   */
  @Override
  public final String buildOperationName(final String internalOperationName) {
    return firstToUpper(internalOperationName);
  }

  /**
   * Convert the internal java class name of an enumeration into the external entity data model name.
   * @param javaEnum
   * @return
   */
  @Override
  public final String buildEnumerationTypeName(final Class<? extends Enum<?>> javaEnum) {
    return javaEnum.getSimpleName();
  }

}
