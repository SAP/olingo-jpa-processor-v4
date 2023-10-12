package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_KEY_PART_OF_GROUP;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_MANDATORY_PART_OF_GROUP;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmMediaStream;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

import jakarta.persistence.Column;
import jakarta.persistence.Version;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * A Property is described on the one hand by its Name and Type and on the other
 * hand by its Property Facets. The type is a qualified name of either a
 * primitive type, a complex type or a enumeration type. Primitive types are
 * mapped by {@link JPATypeConverter}.
 *
 * <p>
 * For details about Property metadata see: <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397954"
 * >OData Version 4.0 Part 3 - 6 Structural Property </a>
 *
 *
 * @author Oliver Grande
 *
 */
class IntermediateSimpleProperty extends IntermediateProperty {

  private static final Log LOGGER = LogFactory.getLog(IntermediateSimpleProperty.class);
  private EdmMediaStream streamInfo;

  IntermediateSimpleProperty(final JPAEdmNameBuilder nameBuilder, final Attribute<?, ?> jpaAttribute,
      final IntermediateSchema schema)
      throws ODataJPAModelException {

    super(nameBuilder, jpaAttribute, schema);
  }

  @Override
  public boolean isAssociation() {
    return false;
  }

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public boolean isComplex() {
    return jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED;
  }

  @Override
  public boolean isKey() {
    if (jpaAttribute instanceof SingularAttribute<?, ?>)
      return ((SingularAttribute<?, ?>) jpaAttribute).isId();
    else
      return false;
  }

  @Override
  void checkConsistency() throws ODataJPAModelException {
    final Column jpaColumn = ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(Column.class);
    if (jpaColumn != null && isPartOfGroup() && !jpaColumn.nullable())
      throw new ODataJPAModelException(NOT_SUPPORTED_MANDATORY_PART_OF_GROUP, jpaAttribute.getDeclaringType()
          .getJavaType().getCanonicalName(), jpaAttribute.getName());
    if (isPartOfGroup() && isKey())
      throw new ODataJPAModelException(NOT_SUPPORTED_KEY_PART_OF_GROUP, jpaAttribute.getDeclaringType()
          .getJavaType().getCanonicalName(), jpaAttribute.getName());
  }

  @Override
  Class<?> determinePropertyType() {
    return jpaAttribute.getJavaType();
  }

  @Override
  void determineIsVersion() {
    final Version jpaVersion = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(Version.class);
    if (jpaVersion != null) {
      isVersion = true;
    }
  }

  @Override
  void determineStreamInfo() throws ODataJPAModelException {
    streamInfo = ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(EdmMediaStream.class);
    if (streamInfo != null
        && (streamInfo.contentType() == null || streamInfo.contentType().isEmpty())
        && (streamInfo.contentTypeAttribute() == null || streamInfo.contentTypeAttribute().isEmpty()))
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ANNOTATION_STREAM_INCOMPLETE,
          internalName);
  }

  @Override
  void determineStructuredType() {
    if (jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED)
      type = schema.getStructuredType(jpaAttribute);
    else
      type = null;
  }

  @Override
  FullQualifiedName determineType() throws ODataJPAModelException {
    return determineTypeByPersistenceType(jpaAttribute.getPersistentAttributeType());
  }

  String getContentType() {
    return streamInfo.contentType();
  }

  String getContentTypeProperty() {
    return streamInfo.contentTypeAttribute();
  }

  @Override
  String getDefaultValue() throws ODataJPAModelException {
    String valueString = null;
    final boolean isAbstract = Modifier.isAbstract(jpaAttribute.getDeclaringType().getJavaType().getModifiers());
    if (!isAbstract
        && jpaAttribute.getJavaMember() instanceof Field
        && jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.BASIC) {
      // It is not possible to get the default value directly from the
      // Field, only from an instance field.get(Object obj).toString(); //NOSONAR
      try {
        // Problem: In case of compound key, which is not referenced via @EmbeddedId Hibernate returns a field of the
        // key class, whereas EclipseLink returns a field of the entity class; which can be checked via
        // field.getDeclaringClass()
        final Field field = (Field) jpaAttribute.getJavaMember();
        Constructor<?> constructor;
        if (!field.getDeclaringClass().equals(jpaAttribute.getDeclaringType().getJavaType()))
          constructor = field.getDeclaringClass().getConstructor();
        else
          constructor = jpaAttribute.getDeclaringType().getJavaType().getConstructor();
        final Object pojo = constructor.newInstance();
        field.setAccessible(true);
        final Object value = field.get(pojo);
        if (value != null)
          valueString = value.toString();
      } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException e) {
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.PROPERTY_DEFAULT_ERROR, e,
            jpaAttribute.getName());
      } catch (final InstantiationException e) {
        // Class could not be instantiated e.g. abstract class like
        // Business Partner => default could not be determined
        // and will be ignored
        LOGGER.debug("Default could not be determined: "
            + jpaAttribute.getDeclaringType().getJavaType().getName()
            + "#"
            + jpaAttribute.getJavaMember().getName(), e);
      }
    }
    return valueString;
  }

  @Override
  boolean isStream() {
    return streamInfo != null && streamInfo.stream();
  }
}
