package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssozation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssozation.valueAssignment;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

class IntermediateDescriptionProperty extends IntermediateProperty implements JPADescriptionAttribute {
  private IntermediateProperty     descriptionProperty;
  private String                   languageAttribute;
  private String                   localeAttribute;
  private JPAStructuredType        targetEntity;
  private HashMap<JPAPath, String> fixedValues;
  private JPAPath                  localFieldPath;

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
          if (descriptionProperty == null)
            // The attribute %2$s has not been found at entity %1$s
            throw new ODataJPAModelException(MessageKeys.INVALID_DESCIPTION_PROPERTY, targetEntity.getInternalName(),
                assozation.descriptionAttribute());
          languageAttribute = assozation.languageAttribute();
          localeAttribute = assozation.localeAttribute();
          // TODO check path is valid
          fixedValues = convertFixedValues(assozation.valueAssignments());

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
          localFieldPath = convertAttributeToPath(!languageAttribute.isEmpty() ? languageAttribute : localeAttribute);
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
  public JPAPath getLocaleFieldName() {
    return localFieldPath;
  }

  @Override
  public Class<?> getType() {
    return descriptionProperty.getType();
  }

  @Override
  public HashMap<JPAPath, String> getFixedValueAssignment() {
    return fixedValues;
  }

  private HashMap<JPAPath, String> convertFixedValues(final valueAssignment[] valueAssignments)
      throws ODataJPAModelException {
    final HashMap<JPAPath, String> result = new HashMap<JPAPath, String>();
    for (final EdmDescriptionAssozation.valueAssignment value : valueAssignments) {
      result.put(convertAttributeToPath(value.attribute()), value.value());
    }
    return result;
  }

  private JPAPath convertAttributeToPath(final String attribute) throws ODataJPAModelException {
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
}
