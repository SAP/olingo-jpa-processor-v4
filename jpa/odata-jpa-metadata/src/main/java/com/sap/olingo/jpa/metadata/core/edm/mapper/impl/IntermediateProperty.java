package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmGeospatial;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtections;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmSearchable;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.annotation.AppliesTo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlMapping;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Lob;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type.PersistenceType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.COMPLEX_PROPERTY_MISSING_PROTECTION_PATH;

/**
 * Properties can be classified by two different aspects:
 * <ol>
 * <li> If they are complex, so are structured, or primitive</li>
 * <li> If they are a collection of instances or if they are simple and can have up to one instance</li>
 * </ol>
 * So properties maybe e.g. a complex collection property or a simple primitive property
 *
 * @author Oliver Grande
 * @contributor FvdB
 */
abstract class IntermediateProperty extends IntermediateModelElement implements IntermediatePropertyAccess, JPAAttribute {
    private AttributeConverter<?, ?> valueConverter;
    private boolean searchable;
    private Class<?> dbType;
    private Class<?> entityType;
    private List<String> fieldGroups;
    private final Map<String, JPAProtectionInfo> externalProtectedPathNames;
    private static final String DB_FIELD_NAME_PATTERN = "\"&1\"";
    protected boolean isVersion;
    protected CsdlProperty edmProperty;
    protected final Attribute<?, ?> jpaAttribute;
    protected final IntermediateSchema schema;
    protected final ManagedType<?> managedType;
    protected IntermediateStructuredType type;
    protected String dbFieldName;

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
    public void addAnnotations(List<CsdlAnnotation> annotations) { edmAnnotations.addAll(annotations); }

    @SuppressWarnings("unchecked")
    @Override
    public <X, Y> AttributeConverter<X, Y> getConverter() { return (AttributeConverter<X, Y>) valueConverter; }

    @Override
    public EdmPrimitiveTypeKind getEdmType() throws ODataJPAModelException { return JPATypeConvertor.convertToEdmSimpleType(entityType); }

    @Override
    public CsdlProperty getProperty() throws ODataJPAModelException { return getEdmItem(); }

    @Override
    public Set<String> getProtectionClaimNames() { return externalProtectedPathNames.keySet(); }

    @Override
    public List<String> getProtectionPath(final String claimName) {
        return (externalProtectedPathNames.containsKey(claimName)) ?
                externalProtectedPathNames.get(claimName).getPath() : new ArrayList<>(0);
    }

    @Override
    public JPAStructuredType getStructuredType() { return type; }

    @Override
    public Class<?> getType() {
        return valueConverter != null
                ? jpaAttribute.getJavaType().isPrimitive() ? boxPrimitive(dbType) : dbType
                : jpaAttribute.getJavaType().isPrimitive() ? boxPrimitive(entityType) : entityType;
    }

    @Override
    public boolean hasProtection() { return !externalProtectedPathNames.isEmpty(); }

    @Override
    public boolean isEnum() { return schema.getEnumerationType(entityType) != null; }

    @Override
    public boolean isEtag() { return isVersion; }

    @Override
    public boolean isSearchable() { return searchable; }

    private void buildProperty(final JPAEdmNameBuilder nameBuilder) throws ODataJPAModelException {
        // Set element specific attributes of super type
        this.setExternalName(nameBuilder.buildPropertyName(internalName));
        entityType = dbType = determineEntityType();

        if (this.jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
            determineIgnore();
            determineStructuredType();
            determineInternalTypesFromConverter();
            determineDBFieldName();
            // TODO @Transient -> e.g. Calculated fields like formatted name
            determineSearchable();
            determineStreamInfo();
            determineIsVersion();
            determineProtection();
            determineFieldGroups();
            checkConsistency();
        }
        postProcessor.processProperty(this, jpaAttribute.getDeclaringType().getJavaType().getCanonicalName());
        // Process annotations after post processing, as external name it could have been changed
        getAnnotations(edmAnnotations, this.jpaAttribute.getJavaMember(), internalName, AppliesTo.PROPERTY);
    }

    protected FullQualifiedName determineTypeByPersistenceType(Enum<?> persistenceType) throws ODataJPAModelException {
        if (persistenceType == PersistentAttributeType.BASIC || persistenceType == PersistenceType.BASIC) {
            final IntermediateModelElement odataType = getODataPrimitiveType();
            return (odataType == null) ? getSimpleType() : odataType.getExternalFQN();
        }
        return (persistenceType == PersistentAttributeType.EMBEDDED || persistenceType == PersistenceType.EMBEDDABLE)
            ? buildFQN(type.getExternalName()) : EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
    }

    protected String getDBFieldName() { return dbFieldName; }

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

    /**
     * Check consistency of provided attribute e.g. check id attribute was annotated with unsupported annotations
     *
     * @throws ODataJPAModelException
     */
    abstract void checkConsistency() throws ODataJPAModelException;

    CsdlMapping createMapper() {
        if (!isLob() && !(getConverter() == null && isEnum())) {
            CsdlMapping mapping = new CsdlMapping();
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
            for (final EdmProtectedBy jpaprivateBy : jpaProtections.value()) {
                determineOneProtection(jpaprivateBy);
            }
        } else {
            final EdmProtectedBy jpaprivateBy =
                    ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(EdmProtectedBy.class);
            if (jpaprivateBy != null) { determineOneProtection(jpaprivateBy); }
        }
    }

    void determineSearchable() {
        final EdmSearchable jpaSearchable = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
                .getAnnotation(EdmSearchable.class);
        if (jpaSearchable != null) searchable = true;
    }

    abstract void determineStreamInfo() throws ODataJPAModelException;

    abstract void determineStructuredType();

    abstract FullQualifiedName determineType() throws ODataJPAModelException;

    abstract String getDefaultValue() throws ODataJPAModelException;

    protected List<String> getGroups() { return fieldGroups; }

    IntermediateModelElement getODataPrimitiveType() { return schema.getEnumerationType(entityType); }

    FullQualifiedName getSimpleType() throws ODataJPAModelException {
        Class<?> javaType = (valueConverter != null) ? dbType : entityType;

        return JPATypeConvertor.convertToEdmSimpleType(javaType, jpaAttribute).getFullQualifiedName();
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

    boolean isPartOfGroup() { return !fieldGroups.isEmpty(); }

    abstract boolean isStream();

    /**
     * Determines if wildcards are supported. In case a complex type is annotated this depends on the type of the target
     * attribute. To prevent deed locks during metadata generation the determination is done late.
     *
     * @param <T>
     * @param claimName
     * @param clazz
     * @return
     */
    protected <T> boolean protectionWithWildcard(final String claimName, final Class<T> clazz) {
        if (externalProtectedPathNames.containsKey(claimName))
            return externalProtectedPathNames.get(claimName).supportsWildcards(clazz);
        return true;
    }

    private void setFacet() throws ODataJPAModelException {
        if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
            final Column jpaColumn = ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(Column.class);
            if (jpaColumn != null) {
                final int precision = jpaColumn.precision();

                edmProperty.setNullable(jpaColumn.nullable());
                edmProperty.setSrid(getSRID());
                edmProperty.setDefaultValue(getDefaultValue());
                // TODO Attribute Unicode
                // For a decimal property the value of this attribute specifies the maximum number of digits allowed in the
                // properties value; it MUST be a positive integer. If no value is specified, the decimal property has
                // unspecified precision.
                if (edmProperty.getTypeAsFQNObject().equals(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                        || edmProperty.getTypeAsFQNObject().equals(EdmPrimitiveTypeKind.Binary.getFullQualifiedName())) {
                    if (jpaColumn.length() > 0) edmProperty.setMaxLength(jpaColumn.length());
                    if (isLob()) edmProperty.setMaxLength(null);
                } else if (edmProperty.getType().equals(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().toString())) {
                    if (precision > 0) edmProperty.setPrecision(precision);
                    if (jpaColumn.scale() > 0) edmProperty.setScale(jpaColumn.scale());
                } else if (edmProperty.getType().equals(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName().toString())
                        || edmProperty.getType().equals(EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName().toString())) {

                    // For a TEMPORAL PROPERTY the value of this attribute specifies the number of decimal
                    // places allowed in the seconds portion of the property's value; it MUST be a non-negative integer between
                    // zero and twelve. If no value is specified, the temporal property has a precision of zero.
                    if (precision > 0) edmProperty.setPrecision(precision);
                }
            }
        }
    }

    /**
     * Converts an internal path into an external path
     *
     * @param internalPath
     * @return
     */
    private String convertPath(final String internalPath) {
        String[] pathSegments = internalPath.split(JPAPath.PATH_SEPARATOR);
        StringBuilder externalPath = new StringBuilder();

        for (final String segment : pathSegments) {
            externalPath.append(nameBuilder.buildPropertyName(segment));
            externalPath.append(JPAPath.PATH_SEPARATOR);
        }
        externalPath.deleteCharAt(externalPath.length() - 1);

        return externalPath.toString();
    }

    private void determineDBFieldName() {
        final Column jpnColumnDetails =
                ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(Column.class);
        if (jpnColumnDetails != null) {
            // TODO allow default name
            dbFieldName = jpnColumnDetails.name();
            if (dbFieldName.isEmpty()) {
                final StringBuilder s = new StringBuilder(DB_FIELD_NAME_PATTERN);
                s.replace(1, 3, internalName);
                dbFieldName = s.toString();
            }
        }
        else { dbFieldName = internalName; }
    }

    private void determineFieldGroups() {
        final EdmVisibleFor jpaFieldGroups =
                ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(EdmVisibleFor.class);
        if (jpaFieldGroups != null) fieldGroups = Arrays.stream(jpaFieldGroups.value()).collect(Collectors.toList());
        else fieldGroups = new ArrayList<>(0);
    }

    private void determineIgnore() {
        final EdmIgnore jpaIgnore = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
                .getAnnotation(EdmIgnore.class);
        if (jpaIgnore != null) { this.setIgnore(true); }
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
                    valueConverter = (AttributeConverter<?, ?>) jpaConverter.converter().getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.TYPE_MAPPER_COULD_NOT_INSTANTIATED, e);
            }
        }
    }

    private void determineOneProtection(final EdmProtectedBy jpaprivateBy) throws ODataJPAModelException {

        List<String> externalNames;
        final String protectionClaimName = jpaprivateBy.name();
        if (externalProtectedPathNames.containsKey(protectionClaimName))
            externalNames = externalProtectedPathNames.get(protectionClaimName).getPath();
        else externalNames = new ArrayList<>(2);
        if (jpaAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
            String internalprivatePath = jpaprivateBy.path();
            if (internalprivatePath.length() == 0) {
                throw new ODataJPAModelException(COMPLEX_PROPERTY_MISSING_PROTECTION_PATH, this.managedType.getJavaType()
                        .getCanonicalName(), this.internalName);
            }
            externalNames.add(getExternalName() + JPAPath.PATH_SEPARATOR + convertPath(jpaprivateBy.path()));
        }
        else { externalNames.add(getExternalName()); }
        externalProtectedPathNames
                .put(protectionClaimName, new JPAProtectionInfo(externalNames, jpaprivateBy.wildcardSupported()));
    }

    private boolean isLob() {
        if (jpaAttribute != null) {
            final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
            return annotatedElement != null && annotatedElement.getAnnotation(Lob.class) != null;
        }
        return false;
    }

}