package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Lob;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type.PersistenceType;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlMapping;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmGeospatial;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmSearchable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.annotation.AppliesTo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;

abstract class IntermediateProperty extends IntermediateModelElement implements IntermediatePropertyAccess,
    JPAAttribute {

  private static final String DB_FIELD_NAME_PATTERN = "\"&1\"";
  protected final Attribute<?, ?> jpaAttribute;
  protected final IntermediateSchema schema;
  protected CsdlProperty edmProperty;
  protected IntermediateStructuredType type;
  protected AttributeConverter<?, ?> valueConverter;
  protected String dbFieldName;
  protected Class<?> dbType;
  protected Class<?> entityType;
  protected final ManagedType<?> managedType;
  protected boolean isVersion;
  protected boolean searchable;

  public IntermediateProperty(final JPAEdmNameBuilder nameBuilder, final Attribute<?, ?> jpaAttribute,
      final IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, IntNameBuilder.buildAttributeName(jpaAttribute));
    this.jpaAttribute = jpaAttribute;
    this.schema = schema;
    this.managedType = jpaAttribute.getDeclaringType();
    buildProperty(nameBuilder);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X, Y extends Object> AttributeConverter<X, Y> getConverter() {
    return (AttributeConverter<X, Y>) valueConverter;
  }

  @Override
  public void addAnnotations(List<CsdlAnnotation> annotations) {
    edmAnnotations.addAll(annotations);
  }

  @Override
  public EdmPrimitiveTypeKind getEdmType() throws ODataJPAModelException {
    return JPATypeConvertor.convertToEdmSimpleType(entityType);
  }

  @Override
  public CsdlProperty getProperty() throws ODataJPAModelException {
    return getEdmItem();
  }

  @Override
  public JPAStructuredType getStructuredType() {
    return type == null ? null : type;
  }

  @Override
  public Class<?> getType() {
    if (valueConverter != null)
      return jpaAttribute.getJavaType().isPrimitive() ? boxPrimitive(dbType) : dbType;
    else
      return jpaAttribute.getJavaType().isPrimitive() ? boxPrimitive(entityType) : entityType;
  }

  @Override
  public boolean isEnum() {
    return schema.getEnumerationType(entityType) != null;
  }

  protected void buildProperty(final JPAEdmNameBuilder nameBuilder) throws ODataJPAModelException {
    // Set element specific attributes of super type
    this.setExternalName(nameBuilder.buildPropertyName(internalName));
    entityType = dbType = determineEntityType();

    if (this.jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      determineIgnore();
      determineStructuredType();
      determineInternalTypesFromConverter();
      determineDBFieldName();
      // TODO @Transient -> e.g. Calculated fields like formated name
      determineSearchable();
      determineStreamInfo();
      determineIsVersion();
    }
    postProcessor.processProperty(this, jpaAttribute.getDeclaringType().getJavaType().getCanonicalName());
    // Process annotations after post processing, as external name it could
    // have been changed
    getAnnotations(edmAnnotations, this.jpaAttribute.getJavaMember(), internalName, AppliesTo.PROPERTY);
  }

  abstract Class<?> determineEntityType();

  protected String getDBFieldName() {
    return dbFieldName;
  }

  @Override
  protected CsdlProperty getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmProperty;
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmProperty == null) {
      edmProperty = new CsdlProperty();
      edmProperty.setName(this.getExternalName());
      edmProperty.setType(determineType());
      setFacet();
      edmProperty.setMapping(createMapper());
      edmProperty.setAnnotations(edmAnnotations);
    }
  }

  CsdlMapping createMapper() {
    if (!isLob() && !(getConverter() == null && isEnum())) {
      CsdlMapping mapping = new CsdlMapping();
      mapping.setInternalName(this.getExternalName());
      mapping.setMappedJavaClass(dbType);
      return mapping;
    }
    return null;
  }

  abstract void determineIsVersion();

  void determineSearchable() {
    final EdmSearchable jpaSearchable = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(EdmSearchable.class);
    if (jpaSearchable != null)
      searchable = true;
  }

  SRID getSRID() {
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

  abstract void determineStreamInfo() throws ODataJPAModelException;

  abstract FullQualifiedName determineType() throws ODataJPAModelException;

  abstract String getDeafultValue() throws ODataJPAModelException;

  abstract boolean isStream();

  void setFacet() throws ODataJPAModelException {
    if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotations();
      final Column jpaColumn = ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(Column.class);
      if (jpaColumn != null) {
        edmProperty.setNullable(jpaColumn.nullable());
        edmProperty.setSrid(getSRID());
        edmProperty.setDefaultValue(getDeafultValue());
        // TODO Attribute Unicode
        if (edmProperty.getTypeAsFQNObject().equals(EdmPrimitiveTypeKind.String.getFullQualifiedName())
            || edmProperty.getTypeAsFQNObject().equals(EdmPrimitiveTypeKind.Binary.getFullQualifiedName())) {
          if (jpaColumn.length() > 0)
            edmProperty.setMaxLength(jpaColumn.length());
          if (isLob())
            edmProperty.setMaxLength(null);
        } else if (edmProperty.getType()
            .equals(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().toString())
            || edmProperty.getType()
                .equals(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName().toString())
            || edmProperty.getType()
                .equals(EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName().toString())) {
          // For a decimal property the value of this attribute specifies the maximum number of digits allowed in the
          // properties value; it MUST be a positive integer. If no value is specified, the decimal property has
          // unspecified precision. For a temporal property the value of this attribute specifies the number of decimal
          // places allowed in the seconds portion of the property's value; it MUST be a non-negative integer between
          // zero and twelve. If no value is specified, the temporal property has a precision of zero.
          if (jpaColumn.precision() > 0)
            edmProperty.setPrecision(jpaColumn.precision());
          else if (edmProperty.getType().equals(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName().toString())
              && jpaColumn.precision() == 0)
            throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.PROPERTY_MISSING_PRECISION,
                jpaAttribute.getName());
          if (edmProperty.getType().equals(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().toString())
              && jpaColumn.scale() > 0)
            edmProperty.setScale(jpaColumn.scale());
        }
      }
    }
  }

  private void determineDBFieldName() {
    final Column jpaColunnDetails = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(Column.class);
    if (jpaColunnDetails != null) {
      // TODO allow default name
      dbFieldName = jpaColunnDetails.name();
      if (dbFieldName.isEmpty()) {
        final StringBuilder s = new StringBuilder(DB_FIELD_NAME_PATTERN);
        s.replace(1, 3, internalName);
        dbFieldName = s.toString();
      }
    } else
      dbFieldName = internalName;
  }

  private void determineIgnore() {
    final EdmIgnore jpaIgnore = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(EdmIgnore.class);
    if (jpaIgnore != null) {
      this.setIgnore(true);
    }
  }

  private void determineInternalTypesFromConverter() throws ODataJPAModelException {
    final Convert jpaConverter = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(Convert.class);
    if (jpaConverter != null) {
      try {
        Type[] convType = jpaConverter.converter().getGenericInterfaces();
        Type[] types = ((ParameterizedType) convType[0]).getActualTypeArguments();
        entityType = (Class<?>) types[0];
        dbType = (Class<?>) types[1];
        if (!JPATypeConvertor.isSupportedByOlingo(entityType))
          valueConverter = (AttributeConverter<?, ?>) jpaConverter.converter().newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw new ODataJPAModelException(
            ODataJPAModelException.MessageKeys.TYPE_MAPPER_COULD_NOT_INSANTIATE, e);
      }
    }
  }

  abstract void determineStructuredType();

  private boolean isLob() {
    if (jpaAttribute != null) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      if (annotatedElement != null && annotatedElement.getAnnotation(Lob.class) != null) {
        return true;
      }
    }
    return false;
  }

  protected FullQualifiedName determineTypeByPersistanceType(Enum<?> persistanceType) throws ODataJPAModelException {
    if (persistanceType == PersistentAttributeType.BASIC || persistanceType == PersistenceType.BASIC) {
      final IntermediateModelElement odataType = getODataPrimitiveType();
      if (odataType == null) {
        return getSimpleType();
      } else
        return odataType.getExternalFQN();
    }
    if (persistanceType == PersistentAttributeType.EMBEDDED || persistanceType == PersistenceType.EMBEDDABLE)
      return nameBuilder.buildFQN(type.getExternalName());
    else
      return EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
  }

  IntermediateModelElement getODataPrimitiveType() {
    return schema.getEnumerationType(entityType);
  }

  FullQualifiedName getSimpleType() throws ODataJPAModelException {
    Class<?> javaType = null;
    if (valueConverter != null) {
      javaType = dbType;
    } else {
      javaType = entityType;
    }
    return JPATypeConvertor.convertToEdmSimpleType(javaType, jpaAttribute)
        .getFullQualifiedName();
  }

  @Override
  public boolean isEtag() {
    return isVersion;
  }

  @Override
  public boolean isSearchable() {
    return searchable;
  }
}