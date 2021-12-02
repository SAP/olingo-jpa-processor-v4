package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.TRANSIENT_KEY_NOT_SUPPORTED;

import java.lang.reflect.AnnotatedElement;

import javax.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

final class IntermediateEmbeddedIdProperty extends IntermediateSimpleProperty {
  IntermediateEmbeddedIdProperty(final JPAEdmNameBuilder nameBuilder, final Attribute<?, ?> jpaAttribute,
      final IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, jpaAttribute, schema);
  }

  @Override
  public boolean isKey() {
    return true;
  }

  @Override
  void determineTransient() throws ODataJPAModelException {
    final EdmTransient jpaTransient = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(EdmTransient.class);
    if (jpaTransient != null) {
      throw new ODataJPAModelException(TRANSIENT_KEY_NOT_SUPPORTED,
          jpaAttribute.getJavaMember().getDeclaringClass().getName());
    }
  }
}
