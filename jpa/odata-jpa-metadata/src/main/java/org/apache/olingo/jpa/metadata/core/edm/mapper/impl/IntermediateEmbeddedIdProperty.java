package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import javax.persistence.metamodel.Attribute;

import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class IntermediateEmbeddedIdProperty extends IntermediateProperty {
  private final Attribute<?, ?> embeddable;

  IntermediateEmbeddedIdProperty(JPAEdmNameBuilder nameBuilder, Attribute<?, ?> jpaAttribute, IntermediateSchema schema,
      Attribute<?, ?> embeddable)
          throws ODataJPAModelException {
    super(nameBuilder, jpaAttribute, schema);
    this.embeddable = embeddable;
  }

  IntermediateEmbeddedIdProperty(JPAEdmNameBuilder nameBuilder, Attribute<?, ?> jpaAttribute,
      IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, jpaAttribute, schema);
    this.embeddable = null;
  }

  @Override
  public boolean isKey() {
    return true;
  }

  Attribute<?, ?> getEmbeddable() {
    return embeddable;
  }
}
