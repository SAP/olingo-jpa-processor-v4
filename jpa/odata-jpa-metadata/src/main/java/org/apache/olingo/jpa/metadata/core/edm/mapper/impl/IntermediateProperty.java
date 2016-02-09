package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;

/**
 * A Property contains on the one hand of the attributes Name and Type and on the other hand of the Property Facets. A
 * type is either a
 * primitive type, a complex type or a enumeration type. Primitive types are mapped by
 * {@link org.apache.olingo.odata4.jpa.processor.core.edm.mapper.impl.JPATypeConvertor}.
 * 
 * <p>For details about Property metadata see:
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397954"
 * >OData Version 4.0 Part 3 - 6 Structural Property </a>
 * 
 * 
 * @author Oliver Grande
 *
 */
class IntermediateProperty extends IntermediateModelElement implements IntermediatePropertyAccess, JPAAttribute {
  protected final Attribute<?, ?> jpaAttribute;
  protected final IntermediateSchema schema;
  protected CsdlProperty edmProperty = null;
  private IntermediateStructuredType type;
  // TODO Store a type @Convert
  private AttributeConverter<?, ?> valueConverter = null;
  private String dbFieldName = null;

  IntermediateProperty(JPAEdmNameBuilder nameBuilder, Attribute<?, ?> jpaAttribute,
      IntermediateSchema schema) throws ODataJPAModelException {

    super(nameBuilder, intNameBuilder.buildAttributeName(jpaAttribute));
    this.jpaAttribute = jpaAttribute;
    this.schema = schema;

    buildProperty(nameBuilder);
  }

  @Override
  public AttributeConverter<?, ?> getConverter() {
    return valueConverter;
  }

  @Override
  public JPAStructuredType getStructuredType() {

    return type == null ? null : type;
  }

  @Override
  public Class<?> getType() {
    return jpaAttribute.getJavaType();
  }

  @Override
  public boolean isComplex() {
    return jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED ? true : false;
  }

  public boolean isKey() {
    if (jpaAttribute instanceof SingularAttribute<?, ?>)
      return ((SingularAttribute<?, ?>) jpaAttribute).isId();
    else
      return false;
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmProperty == null) {
      edmProperty = new CsdlProperty();
      edmProperty.setName(this.getExternalName());

      if (jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.BASIC)
        edmProperty.setType(JPATypeConvertor.convertToEdmSimpleType(jpaAttribute.getJavaType(), jpaAttribute)
            .getFullQualifiedName());
      if (jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED)
        edmProperty.setType(nameBuilder.buildFQN(type.getExternalName()));

      if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
        ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotations();
        Column jpaColumn = ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(Column.class);
        if (jpaColumn != null) {
          edmProperty.setNullable(jpaColumn.nullable());
          // TODO Attribute SRID
          // TODO Attribute DefaultValue
          // TODO Attribute Unicode
          if (edmProperty.getTypeAsFQNObject().equals(EdmPrimitiveTypeKind.String.getFullQualifiedName()) || edmProperty
              .getTypeAsFQNObject().equals(EdmPrimitiveTypeKind.Binary.getFullQualifiedName())) {
            if (jpaColumn.length() > 0)
              edmProperty.setMaxLength(jpaColumn.length());
          } else if (edmProperty.getType().equals(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().toString()) ||
              edmProperty.getType().equals(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName().toString()) ||
              edmProperty.getType().equals(EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName().toString())) {
            // For a decimal property the value of this attribute specifies the maximum number of digits allowed in the
            // properties value; it MUST be a positive integer. If no value is specified, the decimal property has
            // unspecified precision.
            // For a temporal property the value of this attribute specifies the number of decimal places allowed in the
            // seconds portion of the property�s value; it MUST be a non-negative integer between zero and twelve. If no
            // value is specified, the temporal property has a precision of zero.
            if (jpaColumn.precision() > 0)
              edmProperty.setPrecision(jpaColumn.precision());
            if (edmProperty.getType().equals(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().toString())
                && jpaColumn
                    .scale() > 0)
              edmProperty.setScale(jpaColumn.scale());
          }
        }
      }
    }
  }

  @Override
      CsdlProperty getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmProperty;
  }

  private void buildProperty(JPAEdmNameBuilder nameBuilder) throws ODataJPAModelException {
    // Set element specific attributes of super type
    this.setExternalName(nameBuilder.buildPropertyName(internalName));
    if (this.jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      EdmIgnore jpaIgnore = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          EdmIgnore.class);
      if (jpaIgnore != null) {
        this.setIgnore(true);
      }
      if (jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED)
        type = schema.getEntityType(jpaAttribute);
      else
        type = null;
      Convert jpaConverter = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          Convert.class);
      if (jpaConverter != null) {
        try {
          valueConverter = (AttributeConverter<?, ?>) jpaConverter.converter().newInstance();
        } catch (InstantiationException e) {
          throw ODataJPAModelException.throwException(ODataJPAModelException.TYPE_MAPPER_COULD_NOT_INSANTIATE,
              "Type mapper could not be insantated", e);
        } catch (IllegalAccessException e) {
          throw ODataJPAModelException.throwException(ODataJPAModelException.TYPE_MAPPER_COULD_NOT_INSANTIATE,
              "Type mapper could not be insantated", e);
        }
      }
      Column jpaColunnDetails = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          Column.class);
      if (jpaColunnDetails != null)
        dbFieldName = jpaColunnDetails.name();
      else
        dbFieldName = internalName;
      // TODO @Transient -> e.g. Calculated fields like formated name

    }
    postProcessor.processProperty(this, jpaAttribute.getDeclaringType().getJavaType()
        .getCanonicalName());
  }

  @Override
  public boolean isAssociation() {
    return false;
  }

  String getDBFieldName() {
    return dbFieldName;
  }

  @Override
  public EdmPrimitiveTypeKind getEdmType() throws ODataJPAModelException {
    return JPATypeConvertor.convertToEdmSimpleType(jpaAttribute.getJavaType());
  }

  @Override
  public CsdlProperty getProperty() throws ODataJPAModelException {
    return getEdmItem();
  }

}
