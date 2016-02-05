package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import javax.persistence.metamodel.Attribute;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction;

/**
 * Build the internal name for Intermediate Model Elements
 * 
 * @author Oliver Grande
 *
 */
final class JPANameBuilder {
  public String buildStructuredTypeName(Class<?> clazz) {
    return clazz.getCanonicalName();
  }

  public String buildAttributeName(Attribute<?, ?> jpaAttribute) {
    return jpaAttribute.getName();
  }

  public String buildAssociationName(Attribute<?, ?> jpaAttribute) {
    return jpaAttribute.getName();
  }

  public String buildFunctionName(EdmFunction jpaFunction) {
    return jpaFunction.name();
  }

  public String buildEntitySetName(JPAEdmNameBuilder nameBuilder, IntermediateEntityType et) {
    return nameBuilder.buildFQN(et.getInternalName()).getFullQualifiedNameAsString();
  }
}
