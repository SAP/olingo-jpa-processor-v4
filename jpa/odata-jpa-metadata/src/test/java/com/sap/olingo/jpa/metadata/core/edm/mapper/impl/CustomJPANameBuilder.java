/**
 * 
 */
package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.stream.IntStream;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;

/**
 * @author Oliver Grande
 * Created: 19.09.2019
 *
 */
public class CustomJPANameBuilder implements JPAEdmNameBuilder {

  @Override
  public String buildComplexTypeName(EmbeddableType<?> jpaEnbeddedType) {
    return new StringBuilder("T_").append(jpaEnbeddedType.getJavaType().getSimpleName()).toString();
  }

  @Override
  public String buildContainerName() {
    return "service_container";
  }

  @Override
  public String buildEntitySetName(String entityTypeName) {
    return entityTypeName.toUpperCase();
  }

  @Override
  public String buildEntityTypeName(EntityType<?> jpaEntityType) {
    final StringBuilder externalName = new StringBuilder();
    final IntStream name = jpaEntityType.getName().chars();
    name.forEach(i -> this.appeendChar(externalName, i));
    externalName.deleteCharAt(0);
    return externalName.toString();
  }

  @Override
  public String buildEnumerationTypeName(Class<? extends Enum<?>> javaEnum) {
    return new StringBuilder("E_").append(javaEnum.getSimpleName()).toString();
  }

  @Override
  public String buildNaviPropertyName(Attribute<?, ?> jpaAttribute) {
    return jpaAttribute.getName();
  }

  @Override
  public String buildOperationName(String internalOperationName) {
    return new StringBuilder("O_").append(internalOperationName).toString();
  }

  @Override
  public String buildPropertyName(String jpaAttributeName) {
    return jpaAttributeName;
  }

  @Override
  public String getNamespace() {
    return "test";
  }

  private void appeendChar(final StringBuilder builder, int i) {
    if (Character.isUpperCase(i))
      builder.append('_').append(Character.toChars(i)[0]);
    else
      builder.append(Character.toChars(i)[0]);
  }
}
