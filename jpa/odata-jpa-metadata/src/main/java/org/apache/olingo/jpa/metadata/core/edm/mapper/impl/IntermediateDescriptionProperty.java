package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.metamodel.Attribute;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssozation;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateDescriptionProperty extends IntermediateProperty implements JPADescriptionAttribute {
  private IntermediateProperty descriptionProperty;
  private String languageAttribute;
  private String localeAttribute;
  private JPAStructuredType targetEntity;

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
            throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.DESCRIPTION_LOCALE_FIELD_MISSING,
                targetEntity.getInternalName(), this.internalName);
          if (!descriptionProperty.getType().equals(String.class))
            throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.DESCRIPTION_FIELD_WRONG_TYPE,
                targetEntity.getInternalName(), this.internalName);

          edmProperty.setType(JPATypeConvertor.convertToEdmSimpleType(descriptionProperty.getType())
              .getFullQualifiedName());
          edmProperty.setMaxLength(descriptionProperty.getEdmItem().getMaxLength());

        } else
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.DESCRIPTION_ANNOTATION_MISSING,
              targetEntity.getInternalName(), this.internalName);
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
  public JPAPath getLocaleFieldName() throws ODataJPAModelException {
    String attribute;
    if (!languageAttribute.isEmpty())
      attribute = languageAttribute;
    else
      attribute = localeAttribute;

    final String[] pathItems = attribute.split(JPAPath.PATH_SEPERATOR);
    if (pathItems.length > 1) {
      final List<JPAElement> targetPath = new ArrayList<JPAElement>();
      IntermediateProperty nextHop = (IntermediateProperty) targetEntity.getAttribute(pathItems[0]);
      targetPath.add(nextHop);
      for (int i = 1; i < pathItems.length; i++) {
        if (nextHop.isComplex()) {
          nextHop = (IntermediateProperty) nextHop.getStructuredType().getAttribute(pathItems[i]);
          targetPath.add(nextHop);
        }
      }
      return new JPAPathImpl(nextHop.getExternalName(), nextHop.getDBFieldName(), targetPath);
    } else {
      final IntermediateProperty p = (IntermediateProperty) targetEntity.getAttribute(attribute);
      return new JPAPathImpl(p.getExternalName(), p.getDBFieldName(), p);
    }
  }

  @Override
  public Class<?> getType() {
    return descriptionProperty.getType();
  }
}
