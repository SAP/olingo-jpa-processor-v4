package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.COMPLEX_PROPERTY_MISSING_PROTECTION_PATH;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.PROPERTY_PRECISION_NOT_IN_RANGE;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.TRANSIENT_CALCULATOR_TOO_MANY_CONSTRUCTORS;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.TRANSIENT_KEY_NOT_SUPPORTED;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EntityManager;
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
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtections;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmSearchable;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.annotation.AppliesTo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;

/**
 * Properties can be classified by two different aspects:
 * <ol>
 * <li> If they are complex, so are structured, or primitive</li>
 * <li> If they are a collection of instances or if they are simple and can have up to one instance</li>
 * </ol>
 * So properties maybe e.g. a complex collection property or a simple primitive property
 * @author Oliver Grande
 *
 */
abstract class IntermediateProperty extends IntermediateModelElement implements IntermediatePropertyAccess,
    JPAAttribute {

  private static final int UPPER_LIMIT_PRECISION_TEMP = 12;
  private static final int LOWER_LIMIT_PRECISION_TEMP = 0;
  private static final String DB_FIELD_NAME_PATTERN = "\"&1\"";
  protected final Attribute<?, ?> jpaAttribute;
  protected final IntermediateSchema schema;
  protected CsdlProperty edmProperty;
  protected IntermediateStructuredType<?> type;
  protected AttributeConverter<?, ?> valueConverter;
  protected String dbFieldName;
  protected Class<?> dbType;
  protected Class<?> entityType;
  protected final ManagedType<?> managedType;
  protected boolean isVersion;
  protected boolean searchable;
  protected boolean conversionRequired;
  private final Map<String, JPAProtectionInfo> externalProtectedPathNames;
  private List<String> fieldGroups;
  protected List<String> requiredAttributes;
  private Constructor<? extends EdmTransientPropertyCalculator<?>> transientCalculatorConstructor;

  public IntermediateProperty(final JPAEdmNameBuilder nameBuilder, final Attribute<?, ?> jpaAttribute,
      final IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, IntNameBuilder.buildAttributeName(jpaAttribute));
    this.jpaAttribute = jpaAttribute;
    this.schema = schema;
    this.managedType = jpaAttribute.getDeclaringType();
    this.externalProtectedPathNames = new HashMap<>(1);
    buildProperty(nameBuilder);
  }

  @Override
  public void addAnnotations(final List<CsdlAnnotation> annotations) {
    edmAnnotations.addAll(annotations);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends EdmTransientPropertyCalculator<?>> Constructor<T> getCalculatorConstructor()
      throws ODataJPAModelException {
    if (this.edmProperty == null) {
      lazyBuildEdmItem();
    }
    return (Constructor<T>) transientCalculatorConstructor;
  }

  @Override
  public <X, Y extends Object> AttributeConverter<X, Y> getConverter() {
    return conversionRequired ? getRawConverter() : null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X, Y extends Object> AttributeConverter<X, Y> getRawConverter() {
    return (AttributeConverter<X, Y>) valueConverter;
  }

  @Override
  public EdmPrimitiveTypeKind getEdmType() throws ODataJPAModelException {
    return JPATypeConverter.convertToEdmSimpleType(entityType);
  }

  @Override
  public CsdlProperty getProperty() throws ODataJPAModelException {
    return getEdmItem();
  }

  @Override
  public Set<String> getProtectionClaimNames() {
    return externalProtectedPathNames.keySet();
  }

  @Override
  public List<String> getProtectionPath(final String claimName) throws ODataJPAModelException {
    if (externalProtectedPathNames.containsKey(claimName))
      return externalProtectedPathNames.get(claimName).getPath();
    return new ArrayList<>(0);
  }

  /**
   * @return
   */
  @Override
  public List<String> getRequiredProperties() {
    return requiredAttributes;
  }

  @Override
  public JPAStructuredType getStructuredType() {
    return type == null ? null : type;
  }

  @Override
  public Class<?> getType() {
    if (conversionRequired)
      return dbType.isPrimitive() ? boxPrimitive(dbType) : dbType;
    else
      return entityType.isPrimitive() ? boxPrimitive(entityType) : entityType;
  }

  @Override
  public boolean hasProtection() {
    return !externalProtectedPathNames.isEmpty();
  }

  @Override
  public boolean isEnum() {
    return schema.getEnumerationType(entityType) != null;
  }

  @Override
  public boolean isEtag() {
    return isVersion;
  }

  @Override
  public boolean isSearchable() {
    return searchable;
  }

  @Override
  public boolean isTransient() {
    return requiredAttributes != null;
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
      determineTransient();
      determineSearchable();
      determineStreamInfo();
      determineIsVersion();
      determineProtection();
      determineFieldGroups();
      checkConsistency();
    }
    postProcessor.processProperty(this, jpaAttribute.getDeclaringType().getJavaType().getCanonicalName());
    // Process annotations after post processing, as external name it could
    // have been changed
    getAnnotations(edmAnnotations, this.jpaAttribute.getJavaMember(), internalName, AppliesTo.PROPERTY);
  }

  protected FullQualifiedName determineTypeByPersistenceType(final Enum<?> persistanceType)
      throws ODataJPAModelException {
    if (persistanceType == PersistentAttributeType.BASIC || persistanceType == PersistenceType.BASIC) {
      final IntermediateModelElement odataType = getODataPrimitiveType();
      if (odataType == null)
        return getSimpleType();
      else
        return odataType.getExternalFQN();
    }
    if (persistanceType == PersistentAttributeType.EMBEDDED || persistanceType == PersistenceType.EMBEDDABLE)
      return buildFQN(type.getExternalName());
    else
      return EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
  }

  protected String getDBFieldName() {
    return dbFieldName;
  }

  @Override
  protected CsdlProperty getEdmItem() throws ODataJPAModelException {
    if (this.edmProperty == null) {
      lazyBuildEdmItem();
    }
    return edmProperty;
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmProperty == null) {
      edmProperty = new CsdlProperty();
      edmProperty.setName(this.getExternalName());
      edmProperty.setType(determineType());
      setFacet();
      edmProperty.setMapping(createMapper());
      edmProperty.setAnnotations(edmAnnotations);
    }
  }

  /**
   * Check consistency of provided attribute e.g. check id attribute was annotated with unsupported annotations
   * @throws ODataJPAModelException
   */
  abstract void checkConsistency() throws ODataJPAModelException;

  CsdlMapping createMapper() {
    if (!isLob() && !(getConverter() == null && isEnum())) {
      final CsdlMapping mapping = new CsdlMapping();
      mapping.setInternalName(this.getExternalName());
      mapping.setMappedJavaClass(dbType);
      return mapping;
    }
    return null;
  }

  abstract Class<?> determineEntityType();

  abstract void determineIsVersion();

  void determineProtection() throws ODataJPAModelException {
    final EdmProtections jpaProtections = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(EdmProtections.class);
    if (jpaProtections != null) {
      for (final EdmProtectedBy jpaProtectedBy : jpaProtections.value()) {
        determineOneProtection(jpaProtectedBy);
      }
    } else {
      final EdmProtectedBy jpaProtectedBy = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
          .getAnnotation(EdmProtectedBy.class);
      if (jpaProtectedBy != null) {
        determineOneProtection(jpaProtectedBy);
      }
    }
  }

  void determineSearchable() {
    final EdmSearchable jpaSearchable = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(EdmSearchable.class);
    if (jpaSearchable != null)
      searchable = true;
  }

  abstract void determineStreamInfo() throws ODataJPAModelException;

  abstract void determineStructuredType();

  abstract FullQualifiedName determineType() throws ODataJPAModelException;

  abstract String getDefaultValue() throws ODataJPAModelException;

  /**
   * @return
   */
  List<String> getGroups() {
    return fieldGroups;
  }

  IntermediateModelElement getODataPrimitiveType() {
    return schema.getEnumerationType(entityType);
  }

  FullQualifiedName getSimpleType() throws ODataJPAModelException {

    return JPATypeConverter.convertToEdmSimpleType(getType(), jpaAttribute)
        .getFullQualifiedName();
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

  boolean isPartOfGroup() {
    return !fieldGroups.isEmpty();
  }

  abstract boolean isStream();

  /**
   * Determines if wildcards are supported. In case a complex type is annotated this depends on the type of the target
   * attribute. To prevent deed locks during metadata generation the determination is done late.
   * @param <T>
   * @param claimName
   * @param clazz
   * @return
   */
  <T> boolean protectionWithWildcard(final String claimName, final Class<T> clazz) {
    if (externalProtectedPathNames.containsKey(claimName))
      return externalProtectedPathNames.get(claimName).supportsWildcards(clazz);
    return true;
  }

  void setFacet() throws ODataJPAModelException {
    if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      final Column jpaColumn = ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(Column.class);
      if (jpaColumn != null) {
        edmProperty.setNullable(jpaColumn.nullable());
        edmProperty.setSrid(getSRID());
        edmProperty.setDefaultValue(getDefaultValue());
        edmProperty.setMaxLength(determineMaxLength(jpaColumn));
        determinePrecisionScale(jpaColumn);
        // TODO Attribute Unicode
      }
    }
  }

  private void determinePrecisionScale(final Column jpaColumn) throws ODataJPAModelException {
    if (edmProperty.getType()
        .equals(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().toString())) {
      setPrecisionScale(jpaColumn);
    } else {
      setPrecisionScaleTemporal(jpaColumn);
    }
  }

  private Integer determineMaxLength(final Column jpaColumn) {

    if ((edmProperty.getTypeAsFQNObject().equals(EdmPrimitiveTypeKind.String.getFullQualifiedName())
        || edmProperty.getTypeAsFQNObject().equals(EdmPrimitiveTypeKind.Binary.getFullQualifiedName()))
        && !isLob()
        && jpaColumn.length() > 0) {

      return jpaColumn.length();
    }
    return null;
  }

  /**
   * For a temporal property the value of this attribute specifies the number of decimal
   * places allowed in the seconds portion of the property's value; it MUST be a non-negative integer between
   * zero and twelve. If no value is specified, the temporal property has a precision of zero.<br>
   * See: <a href="https://docs.oasis-open.org/odata/odata-csdl-xml/v4.01/odata-csdl-xml-v4.01.html#_Toc38530360">7.2.3
   * Precision</a>
   * @param jpaColumn
   * @throws ODataJPAModelException
   */
  private void setPrecisionScaleTemporal(final Column jpaColumn) throws ODataJPAModelException {
    if (edmProperty.getType()
        .equals(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName().toString())
        || edmProperty.getType()
            .equals(EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName().toString())
        || edmProperty.getType()
            .equals(EdmPrimitiveTypeKind.Duration.getFullQualifiedName().toString())) {
      if (jpaColumn.precision() < LOWER_LIMIT_PRECISION_TEMP || jpaColumn.precision() > UPPER_LIMIT_PRECISION_TEMP) {
        // The type of property '%1$s' requires a precision between 0 and 12, but was '%2$s'.
        throw new ODataJPAModelException(PROPERTY_PRECISION_NOT_IN_RANGE, jpaAttribute.getName(), Integer.toString(
            jpaColumn.precision()));
      } else {
        edmProperty.setPrecision(jpaColumn.precision());
      }
    }
  }

  /**
   * Sets Precision and Scale for a Decimal:<br>
   * For a decimal property the value of this attribute specifies the maximum number of digits allowed in the
   * properties value; it MUST be a positive integer. If no value is specified, the decimal property has
   * unspecified precision. <br>
   * See: <a href="https://docs.oasis-open.org/odata/odata-csdl-xml/v4.01/odata-csdl-xml-v4.01.html#_Toc38530360">7.2.3
   * Precision</a>
   * @param jpaColumn
   * @throws ODataJPAModelException
   */
  private void setPrecisionScale(final Column jpaColumn) {
    if (jpaColumn.precision() > 0)
      edmProperty.setPrecision(jpaColumn.precision());
    if (edmProperty.getType().equals(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().toString())
        && jpaColumn.scale() > 0)
      edmProperty.setScale(jpaColumn.scale());

  }

  /**
   * Converts an internal path into an external path
   * @param internalPath
   * @return
   */
  private String convertPath(final String internalPath) {

    final String[] pathSegments = internalPath.split(JPAPath.PATH_SEPARATOR);
    final StringBuilder externalPath = new StringBuilder();
    for (final String segment : pathSegments) {
      externalPath.append(nameBuilder.buildPropertyName(segment));
      externalPath.append(JPAPath.PATH_SEPARATOR);
    }
    externalPath.deleteCharAt(externalPath.length() - 1);

    return externalPath.toString();
  }

  /**
   * @param calculator
   * @return
   * @throws ODataJPAModelException
   */
  @SuppressWarnings("unchecked")
  private Constructor<? extends EdmTransientPropertyCalculator<?>> determineCalculatorConstructor(
      final Class<? extends EdmTransientPropertyCalculator<?>> calculator) throws ODataJPAModelException {

    if (calculator.getConstructors().length > 1)
      throw new ODataJPAModelException(TRANSIENT_CALCULATOR_TOO_MANY_CONSTRUCTORS,
          calculator.getName(), jpaAttribute.getName(),
          jpaAttribute.getJavaMember().getDeclaringClass().getName());
    final Constructor<?> c = calculator.getConstructors()[0];
    if (c.getParameters() != null) {
      for (final Parameter p : c.getParameters()) {
        if (!(p.getType().isAssignableFrom(EntityManager.class)
            || p.getType().isAssignableFrom(Map.class)))
          throw new ODataJPAModelException(MessageKeys.TRANSIENT_CALCULATOR_WRONG_PARAMETER,
              calculator.getName(), jpaAttribute.getName(),
              jpaAttribute.getJavaMember().getDeclaringClass().getName());
      }
    }
    return (Constructor<? extends EdmTransientPropertyCalculator<?>>) c;
  }

  private void determineDBFieldName() {
    final Column jpaColumnDetails = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(Column.class);
    if (jpaColumnDetails != null) {
      // TODO allow default name
      dbFieldName = jpaColumnDetails.name();
      if (dbFieldName.isEmpty()) {
        final StringBuilder s = new StringBuilder(DB_FIELD_NAME_PATTERN);
        s.replace(1, 3, internalName);
        dbFieldName = s.toString();
      }
    } else {
      dbFieldName = internalName;
    }
  }

  /**
   *
   */
  private void determineFieldGroups() {
    final EdmVisibleFor jpaFieldGroups = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(EdmVisibleFor.class);
    if (jpaFieldGroups != null)
      fieldGroups = Arrays.stream(jpaFieldGroups.value()).collect(Collectors.toList());
    else
      fieldGroups = new ArrayList<>(0);
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
        final Type[] convType = jpaConverter.converter().getGenericInterfaces();
        final Type[] types = ((ParameterizedType) convType[0]).getActualTypeArguments();
        entityType = (Class<?>) types[0];
        dbType = (Class<?>) types[1];
        conversionRequired = !JPATypeConverter.isSupportedByOlingo(entityType);
        valueConverter = (AttributeConverter<?, ?>) jpaConverter.converter().newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw new ODataJPAModelException(
            ODataJPAModelException.MessageKeys.TYPE_MAPPER_COULD_NOT_INSTANTIATED, e);
      }
    }
  }

  private void determineOneProtection(final EdmProtectedBy jpaProtectedBy) throws ODataJPAModelException {

    List<String> externalNames;
    final String protectionClaimName = jpaProtectedBy.name();
    if (externalProtectedPathNames.containsKey(protectionClaimName))
      externalNames = externalProtectedPathNames.get(protectionClaimName).getPath();
    else
      externalNames = new ArrayList<>(2);
    if (jpaAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
      final String internalProtectedPath = jpaProtectedBy.path();
      if (internalProtectedPath.length() == 0) {
        throw new ODataJPAModelException(COMPLEX_PROPERTY_MISSING_PROTECTION_PATH, this.managedType.getJavaType()
            .getCanonicalName(), this.internalName);
      }
      externalNames.add(getExternalName() + JPAPath.PATH_SEPARATOR + convertPath(jpaProtectedBy.path()));
    } else {
      externalNames.add(getExternalName());
    }
    externalProtectedPathNames.put(protectionClaimName, new JPAProtectionInfo(externalNames, jpaProtectedBy
        .wildcardSupported()));
  }

  private List<String> determineRequiredAttributesTransient(final EdmTransient jpaTransient) {
    return jpaTransient.requiredAttributes() == null ? Collections.emptyList() : Arrays.asList(
        jpaTransient.requiredAttributes());
  }

  /**
   * @throws ODataJPAModelException
   *
   */
  void determineTransient() throws ODataJPAModelException {
    final EdmTransient jpaTransient = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(EdmTransient.class);
    if (jpaTransient != null) {
      if (isKey())
        throw new ODataJPAModelException(TRANSIENT_KEY_NOT_SUPPORTED,
            jpaAttribute.getJavaMember().getDeclaringClass().getName());
      requiredAttributes = determineRequiredAttributesTransient(jpaTransient);
      transientCalculatorConstructor = determineCalculatorConstructor(jpaTransient.calculator());
    }
  }

  private boolean isLob() {
    if (jpaAttribute != null && jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      if (annotatedElement != null && annotatedElement.getAnnotation(Lob.class) != null) {
        return true;
      }
    }
    return false;
  }
}