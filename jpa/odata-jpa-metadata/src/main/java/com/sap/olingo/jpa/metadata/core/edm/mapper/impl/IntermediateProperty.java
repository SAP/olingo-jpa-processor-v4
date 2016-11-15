package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Version;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmGeospatial;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmMediaStream;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmSearchable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;

/**
 * A Property is described on the one hand by its Name and Type and on the other hand by its Property Facets. The
 * type is a qualified name of either a primitive type, a complex type or a enumeration type. Primitive types are mapped
 * by {@link JPATypeConvertor}.
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
  private static final String DB_FIELD_NAME_PATTERN = "\"&1\"";

  protected final Attribute<?, ?>    jpaAttribute;
  protected final IntermediateSchema schema;
  protected CsdlProperty             edmProperty;
  private IntermediateStructuredType type;
  private AttributeConverter<?, ?>   valueConverter;
  private String                     dbFieldName;
  private boolean                    searchable;
  private boolean                    isVersion;
  private EdmMediaStream             streamInfo;

  IntermediateProperty(final JPAEdmNameBuilder nameBuilder, final Attribute<?, ?> jpaAttribute,
      final IntermediateSchema schema) throws ODataJPAModelException {

    super(nameBuilder, IntNameBuilder.buildAttributeName(jpaAttribute));
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

  @Override
  public boolean isKey() {
    if (jpaAttribute instanceof SingularAttribute<?, ?>)
      return ((SingularAttribute<?, ?>) jpaAttribute).isId();
    else
      return false;
  }

  boolean isStream() {
    return streamInfo == null ? false : streamInfo.stream();
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
        final Column jpaColumn = ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(Column.class);
        if (jpaColumn != null) {
          edmProperty.setNullable(jpaColumn.nullable());
          edmProperty.setSrid(getSRID());
          edmProperty.setDefaultValue(getDeafultValue());
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
            // seconds portion of the property's value; it MUST be a non-negative integer between zero and twelve. If no
            // value is specified, the temporal property has a precision of zero.
            if (jpaColumn.precision() > 0)
              edmProperty.setPrecision(jpaColumn.precision());
            if (edmProperty.getType().equals(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().toString())
                && jpaColumn.scale() > 0)
              edmProperty.setScale(jpaColumn.scale());
          }
        }
      }
    }
  }

  private SRID getSRID() {
    SRID result = null;
    if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      final EdmGeospatial spatialDetails = annotatedElement.getAnnotation(EdmGeospatial.class);
      if (spatialDetails != null) {
        final String srid = spatialDetails.srid();
        if (srid.isEmpty())
          result = SRID.valueOf(null);
        else
          result = SRID.valueOf(srid);
        result.setDimension(spatialDetails.dimension());
      }
    }
    return result;
  }

  private String getDeafultValue() throws ODataJPAModelException {
    String valueString = null;
    if (jpaAttribute.getJavaMember() instanceof Field
        && jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.BASIC) {
      // It is not possible to get the default value directly from the Field,
      // only from an instance field.get(Object obj).toString();
      try {
        final Field field = (Field) jpaAttribute.getJavaMember();
        final Constructor<?> constructor = jpaAttribute.getDeclaringType().getJavaType().getConstructor();
        final Object pojo = constructor.newInstance();
        field.setAccessible(true);
        final Object value = field.get(pojo);
        if (value != null)
          valueString = value.toString();
      } catch (NoSuchMethodException e) {
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.PROPERTY_DEFAULT_ERROR, e, jpaAttribute
            .getName());
      } catch (InstantiationException e) {
        // Class could not be instantiated e.g. abstract class like Business Partner=> default could not be determined
        // and will be ignored
      } catch (IllegalAccessException e) {
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.PROPERTY_DEFAULT_ERROR, e, jpaAttribute
            .getName());
      } catch (IllegalArgumentException e) {
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.PROPERTY_DEFAULT_ERROR, e, jpaAttribute
            .getName());
      } catch (InvocationTargetException e) {
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.PROPERTY_DEFAULT_ERROR, e, jpaAttribute
            .getName());
      }
    }
    return valueString;
  }

  @Override
  CsdlProperty getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmProperty;
  }

  private void buildProperty(final JPAEdmNameBuilder nameBuilder) throws ODataJPAModelException {
    // Set element specific attributes of super type
    this.setExternalName(nameBuilder.buildPropertyName(internalName));
    if (this.jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      final EdmIgnore jpaIgnore = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          EdmIgnore.class);
      if (jpaIgnore != null) {
        this.setIgnore(true);
      }
      if (jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED)
        type = schema.getStructuredType(jpaAttribute);
      else
        type = null;

      final Convert jpaConverter = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          Convert.class);
      if (jpaConverter != null) {
        try {
          valueConverter = (AttributeConverter<?, ?>) jpaConverter.converter().newInstance();
        } catch (InstantiationException e) {
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.TYPE_MAPPER_COULD_NOT_INSANTIATE, e);
        } catch (IllegalAccessException e) {
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.TYPE_MAPPER_COULD_NOT_INSANTIATE, e);
        }
      }
      final Column jpaColunnDetails = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          Column.class);
      if (jpaColunnDetails != null) {
        dbFieldName = jpaColunnDetails.name();
        if (dbFieldName.isEmpty()) {
          final StringBuffer s = new StringBuffer(DB_FIELD_NAME_PATTERN);
          s.replace(1, 3, internalName);
          dbFieldName = s.toString();
        }
      } else
        dbFieldName = internalName;
      // TODO @Transient -> e.g. Calculated fields like formated name
      final EdmSearchable jpaSearchable = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          EdmSearchable.class);
      if (jpaSearchable != null)
        searchable = true;

      streamInfo = ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(EdmMediaStream.class);
      if (streamInfo != null) {
        if ((streamInfo.contentType() == null || streamInfo.contentType().isEmpty())
            && (streamInfo.contentTypeAttribute() == null || streamInfo.contentTypeAttribute().isEmpty()))
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ANNOTATION_STREAM_INCOMPLETE,
              internalName);
      }
      final Version jpaVersion = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          Version.class);
      if (jpaVersion != null) {
        isVersion = true;
      }
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

  @Override
  public boolean isSearchable() {
    return searchable;
  }

  String getContentType() {
    return streamInfo.contentType();
  }

  String getContentTypeProperty() {
    return streamInfo.contentTypeAttribute();
  }

  @Override
  public boolean isEtag() {
    return isVersion;
  }
}
