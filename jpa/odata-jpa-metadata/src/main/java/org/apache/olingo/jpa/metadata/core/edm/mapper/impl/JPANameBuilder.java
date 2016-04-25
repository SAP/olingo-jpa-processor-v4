package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import javax.persistence.metamodel.Attribute;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;

/**
 * Build the internal name for Intermediate Model Elements
 * 
 * @author Oliver Grande
 *
 */
final class JPANameBuilder {
  public String buildStructuredTypeName(final Class<?> clazz) {
    return clazz.getCanonicalName();
  }

  public String buildAttributeName(final Attribute<?, ?> jpaAttribute) {
    return jpaAttribute.getName();
  }

  public String buildAssociationName(final Attribute<?, ?> jpaAttribute) {
    return jpaAttribute.getName();
  }

  public String buildFunctionName(final EdmFunction jpaFunction) {
    return jpaFunction.name();
  }

  public String buildEntitySetName(final JPAEdmNameBuilder nameBuilder, final JPAStructuredType entityType) {
    return nameBuilder.buildFQN(entityType.getInternalName()).getFullQualifiedNameAsString();
  }
}
