/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.stream.IntStream;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;

/**
 * @author Oliver Grande
 * Created: 19.09.2019
 *
 */
public class CustomJPANameBuilder implements JPAEdmNameBuilder {

  @Override
  public String buildComplexTypeName(final EmbeddableType<?> jpaEmbeddedType) {
    return new StringBuilder("T_").append(jpaEmbeddedType.getJavaType().getSimpleName()).toString();
  }

  @Override
  public String buildContainerName() {
    return "service_container";
  }

  @Override
  public String buildEntitySetName(final String entityTypeName) {
    return entityTypeName.toUpperCase();
  }

  @Override
  public String buildEntityTypeName(final EntityType<?> jpaEntityType) {
    final StringBuilder externalName = new StringBuilder();
    final IntStream name = jpaEntityType.getName().chars();
    name.forEach(i -> this.appendChar(externalName, i));
    externalName.deleteCharAt(0);
    return externalName.toString();
  }

  @Override
  public String buildEnumerationTypeName(final Class<? extends Enum<?>> javaEnum) {
    return new StringBuilder("E_").append(javaEnum.getSimpleName()).toString();
  }

  @Override
  public String buildNaviPropertyName(final Attribute<?, ?> jpaAttribute) {
    return jpaAttribute.getName();
  }

  @Override
  public String buildOperationName(final String internalOperationName) {
    return new StringBuilder("O_").append(internalOperationName).toString();
  }

  @Override
  public String buildPropertyName(final String jpaAttributeName) {
    return jpaAttributeName;
  }

  @Override
  public String getNamespace() {
    return "test";
  }

  private void appendChar(final StringBuilder builder, final int i) {
    if (Character.isUpperCase(i))
      builder.append('_').append(Character.toChars(i)[0]);
    else
      builder.append(Character.toChars(i)[0]);
  }
}
