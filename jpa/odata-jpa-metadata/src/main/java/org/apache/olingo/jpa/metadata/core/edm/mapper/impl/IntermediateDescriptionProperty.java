package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import javax.persistence.metamodel.Attribute;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssozation;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateDescriptionProperty extends IntermediateProperty implements JPADescriptionAttribute {
  private IntermediateProperty descriptionProperty;
  private String languageAttribute;
  private String localeAttribute;

  IntermediateDescriptionProperty(final JPAEdmNameBuilder nameBuilder, final Attribute<?, ?> jpaAttribute,
      final IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, jpaAttribute, schema);
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (this.edmProperty == null) {
      super.lazyBuildEdmItem();
      if (this.jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
        final EdmDescriptionAssozation assozation = ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(
            EdmDescriptionAssozation.class);
        if (assozation != null) {
          // determine generic type of a collection in case of an OneToMany association
          final Field jpaField = (Field) jpaAttribute.getJavaMember();
          final ParameterizedType jpaTargetEntityType = (ParameterizedType) jpaField.getGenericType();
          JPAStructuredType targetEntity;
          if (jpaTargetEntityType != null)
            targetEntity = schema.getEntityType((Class<?>) jpaTargetEntityType.getActualTypeArguments()[0]);
          else
            targetEntity = schema.getEntityType(jpaAttribute.getJavaType());
          descriptionProperty = (IntermediateProperty) targetEntity.getAttribute(assozation
              .descriptionAttribute());
          languageAttribute = assozation.languageAttribute();
          localeAttribute = assozation.localeAttribute();
          if (languageAttribute.isEmpty() && localeAttribute.isEmpty() ||
              !languageAttribute.isEmpty() && !localeAttribute.isEmpty())
            throw ODataJPAModelException.throwException(ODataJPAModelException.DESCRIPTION_LOCALE_FIELD_MISSING,
                "EdmDescriptionAssozation: At one of the fields languageAttribute or localeAttribute must be filled");
          // TODO Error handling: Determine type: Should be String
          edmProperty.setType(JPATypeConvertor.convertToEdmSimpleType(descriptionProperty.getType())
              .getFullQualifiedName());
          edmProperty.setMaxLength(descriptionProperty.getEdmItem().getMaxLength());

        } else
          // TODO Error handling: It makes no sense to create a Description Property w/o annotation
          ;
      }
    }
  }

  @Override
  public boolean isAssociation() {
    return true;
  }

  @Override
  public boolean isLocationJoin() {
    if (!localeAttribute.isEmpty())
      return true;
    return false;
  }

  @Override
  public JPAAttribute getDescriptionAttribute() {
    return descriptionProperty;
  }

  @Override
  public String getLocaleFieldName() {
    if (!languageAttribute.isEmpty())
      return languageAttribute;
    else
      return localeAttribute;
  }
}
