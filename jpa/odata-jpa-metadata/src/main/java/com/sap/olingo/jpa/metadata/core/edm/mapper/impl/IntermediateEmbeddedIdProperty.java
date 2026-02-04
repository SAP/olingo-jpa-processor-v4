package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.TRANSIENT_KEY_NOT_SUPPORTED;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

import jakarta.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

final class IntermediateEmbeddedIdProperty extends IntermediateSimpleProperty {

  IntermediateEmbeddedIdProperty(final JPAEdmNameBuilder nameBuilder, final Attribute<?, ?> jpaAttribute,
      final IntermediateSchema schema)
      throws ODataJPAModelException {
    super(nameBuilder, jpaAttribute, schema);
  }

  IntermediateEmbeddedIdProperty(final IntermediateEmbeddedIdProperty source,
      final List<String> userGroups, final boolean hideRestrictedProperties) throws ODataJPAModelException {
    super(source, userGroups, hideRestrictedProperties);
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

  @SuppressWarnings("unchecked")
  @Override
  protected <T extends IntermediateModelElement> T asUserGroupRestricted(final List<String> userGroups,
      final boolean hideRestrictedProperties) // NOSONAR
      throws ODataJPAModelException {
    return (T) new IntermediateEmbeddedIdProperty(this, userGroups, hideRestrictedProperties);
  }
}
