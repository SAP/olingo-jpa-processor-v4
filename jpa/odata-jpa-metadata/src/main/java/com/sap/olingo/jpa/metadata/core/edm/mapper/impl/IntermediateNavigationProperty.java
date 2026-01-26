package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.MISSING_ONE_TO_ONE_ANNOTATION;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.REFERENCED_PROPERTY_NOT_FOUND;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.CheckForNull;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.PluralAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDeleteAction;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlDynamicExpression;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCache;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCacheSupplier;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.ListCache;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.ListCacheSupplier;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelIgnoreException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;

/**
 * A navigation property describes a relation of one entity type to another
 * entity type and allows to navigate to it.
 * IntermediateNavigationProperty represents a navigation within on service,
 * that is source and target are described by
 * the same service document.
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397962"
 * >OData Version 4.0 Part 3 - 7 Navigation Property</a>
 *
 * @author Oliver Grande
 * @param <S> Type of the parent the navigation belongs to, also called source
 * type
 */
final class IntermediateNavigationProperty<S> extends IntermediateModelElement implements
    IntermediateNavigationPropertyAccess, JPAAssociationAttribute, ODataAnnotatable {

  private static final Log LOGGER = LogFactory.getLog(IntermediateNavigationProperty.class);

  private final Attribute<?, ?> jpaAttribute;
  private final ListCache<CsdlReferentialConstraint> referentialConstraints;
  private CsdlNavigationProperty edmNavigationProperty;
  private CsdlOnDelete edmOnDelete;
  private final IntermediateStructuredType<S> sourceType;
  private IntermediateStructuredType<?> targetType;
  private final InstanceCache<JPAAssociationAttribute> partner;
  private final IntermediateSchema schema;
  private IntermediateJoinTable joinTable;
  private final ListCache<IntermediateJoinColumn> joinColumns;
  private final List<String> requiredDbColumns = new ArrayList<>();
  private PersistentAttributeType cardinality;
  private Optional<String> mappedBy;

  IntermediateNavigationProperty(final JPAEdmNameBuilder nameBuilder, final IntermediateStructuredType<S> parent,
      final Attribute<?, ?> jpaAttribute, final IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, InternalNameBuilder.buildAssociationName(jpaAttribute), schema.getAnnotationInformation());
    this.jpaAttribute = jpaAttribute;
    this.schema = schema;
    this.sourceType = parent;
    this.referentialConstraints = new ListCacheSupplier<>(this::determineReferentialConstraints);
    this.joinColumns = new ListCacheSupplier<>(this::buildJoinColumns);
    this.partner = new InstanceCacheSupplier<>(this::determinePartner);
    buildNavigationProperty();
  }

  @Override
  public void addAnnotations(final List<CsdlAnnotation> annotations) {
    edmAnnotations.addAll(annotations);
  }

  @Override
  public <T extends EdmTransientPropertyCalculator<?>> Constructor<T> getCalculatorConstructor() {
    return null;
  }

  @Override
  public <X, Y extends Object> AttributeConverter<X, Y> getConverter() {
    return null;
  }

  @Override
  public Class<?> getDbType() {
    return null;
  }

  @Override
  public Class<?> getJavaType() {
    return null;
  }

  @Override
  public EdmPrimitiveTypeKind getEdmType() {
    return null;
  }

  public JPAJoinTable getJoinTable() {
    return joinTable;
  }

  @Override
  public JPAAssociationAttribute getPartner() throws ODataJPAModelException {
    return partner.get().orElse(null);
  }

  @Override
  public JPAAssociationPath getPath() throws ODataJPAModelException {
    return getStructuredType().getAssociationPath(getExternalName());
  }

  @Override
  public CsdlNavigationProperty getProperty() throws ODataJPAModelException {
    return getEdmItem();

  }

  @Override
  public Set<String> getProtectionClaimNames() {
    return new HashSet<>(0);
  }

  @Override
  public List<String> getProtectionPath(final String claimName) throws ODataJPAModelException {
    return new ArrayList<>(0);
  }

  @Override
  public <X, Y extends Object> AttributeConverter<X, Y> getRawConverter() {
    return null;
  }

  @Override
  public List<String> getRequiredProperties() {
    return Collections.emptyList();
  }

  @Override
  public JPAStructuredType getStructuredType() throws ODataJPAModelException {
    return sourceType;
  }

  @Override
  public JPAStructuredType getTargetEntity() throws ODataJPAModelException {
    return targetType;
  }

  @Override
  public Class<?> getType() {
    return jpaAttribute.getJavaType();
  }

  @Override
  public boolean hasProtection() {
    return false;
  }

  @Override
  public boolean isAssociation() {
    return true;
  }

  @Override
  public boolean isCollection() {
    return jpaAttribute.isCollection();
  }

  @Override
  public boolean isTransient() {
    return false;
  }

  @Override
  public void setOnDelete(final CsdlOnDelete onDelete) {
    edmOnDelete = onDelete;
  }

  @Override
  public String toString() {
    return getExternalName() + ": [sourceType=" + sourceType + ", targetType=" + targetType
        + ", partner=" + partnerToString() + ", joinTable=" + joinTable + "]";
  }

  private String partnerToString() {
    try {
      final var other = getPartner();
      return other == null ? "null" : other.getExternalName();
    } catch (final ODataJPAModelException e) {
      return null;
    }
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmNavigationProperty == null) {
      edmNavigationProperty = new CsdlNavigationProperty();
      edmNavigationProperty.setName(getExternalName());
      edmNavigationProperty.setType(buildFQN(targetType.getExternalName()));
      edmNavigationProperty.setCollection(jpaAttribute.isCollection());
      edmNavigationProperty.setAnnotations(edmAnnotations);
      edmNavigationProperty.getReferentialConstraints().addAll(getReferentialConstraints());
      // Optional --> RelationAnnotation
      determineOnDelete();
      // TODO determine ContainsTarget
      edmNavigationProperty.setPartner(getPartner() == null ? null : getPartner().getExternalName());
    }
  }

  @Override
  CsdlNavigationProperty getEdmItem() throws ODataJPAModelException {
    if (edmNavigationProperty == null) {
      lazyBuildEdmItem();
    }
    return edmNavigationProperty;
  }

  @Override
  public CsdlAnnotation getAnnotation(final String alias, final String term) throws ODataJPAModelException {
    return filterAnnotation(alias, term);
  }

  @Override
  public Object getAnnotationValue(final String alias, final String term, final String property)
      throws ODataJPAModelException {

    try {
      return Optional.ofNullable(getAnnotation(alias, term))
          .map(a -> a.getExpression())
          .map(expression -> getAnnotationValue(property, expression))
          .orElse(null);
    } catch (final ODataJPAModelInternalException e) {
      throw (ODataJPAModelException) e.getCause();
    }

  }

  @Override
  protected Object getAnnotationDynamicValue(final String property, final CsdlDynamicExpression expression)
      throws ODataJPAModelInternalException {

    if (expression.isRecord()) {
      // This may create a problem if the property in question is a record itself.
      // Currently non is supported in
      // standard
      final var propertyValue = findAnnotationPropertyValue(property, expression);
      if (propertyValue.isPresent()) {
        return getAnnotationValue(property, propertyValue.get());
      }
    }
    return null;
  }

  PersistentAttributeType getJoinCardinality() {
    return jpaAttribute.getPersistentAttributeType();
  }

  @SuppressWarnings("unchecked")
  <T extends JPAJoinColumn> List<T> getJoinColumns() throws ODataJPAModelException {
    return (List<T>) joinColumns.get();

  }

  List<String> getRequiredDbColumns() throws ODataJPAModelException {
    if (edmNavigationProperty == null)
      lazyBuildEdmItem();
    if (requiredDbColumns.isEmpty()) {
      for (final JPAJoinColumn joinColumn : getJoinColumns()) {
        requiredDbColumns.add(joinColumn.getName());
      }
    }
    return requiredDbColumns;
  }

  IntermediateStructuredType<S> getSourceType() {
    return sourceType;
  }

  boolean isMapped() {
    return mappedBy.isPresent();
  }

  /**
   * In case the column name is not given at an association a default name is
   * generated according to the JPA standard.
   * The default name will be &lt;association name&gt;_&lt;target key name&gt;,
   * all upper case.
   *
   * @param annotatedElement
   * @param isSourceOne
   * @return
   * @throws ODataJPAModelException
   */
  private String buildDefaultName(final boolean isSourceOne) throws ODataJPAModelException {

    final var columnName = new StringBuilder(jpaAttribute.getName());
    columnName.append('_');
    if (isSourceOne)
      columnName.append(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) targetType)
          .getKey().get(0)).getDBFieldName());
    else
      columnName.append(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) sourceType)
          .getKey().get(0)).getDBFieldName());
    return columnName.toString().replace("\"", "").toUpperCase(Locale.ENGLISH);
  }

  private IntermediateJoinColumn buildImplicitJoinColumnPair(final boolean isSourceOne) throws ODataJPAModelException {
    final var intermediateColumn = new IntermediateJoinColumn(buildDefaultName(isSourceOne),
        fillMissingName());
    if (LOGGER.isTraceEnabled())
      LOGGER.trace(getExternalName() + ": Add join condition with default name = " + intermediateColumn.toString());
    return intermediateColumn;
  }

  private void buildJoinColumnPairList(final boolean isSourceOne, int implicitColumns,
      final List<IntermediateJoinColumn> result, final JoinColumn[] columns) throws ODataJPAModelException {
    for (final JoinColumn column : columns) {
      final var referencedColumnName = column.referencedColumnName();
      final var name = column.name();
      result.add(buildOneJoinColumnPair(isSourceOne, column));
      if (referencedColumnName == null || referencedColumnName.isEmpty() || name == null || name.isEmpty()) {
        implicitColumns += 1;
        if (implicitColumns > 1)
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
              getInternalName());
      }
    }
  }

  private List<IntermediateJoinColumn> buildJoinColumns() {
    try {
      List<IntermediateJoinColumn> columns = new ArrayList<>();
      if (jpaAttribute.getJavaMember() instanceof final AnnotatedElement annotatedElement) {

        // Determine referential constraint
        final var isSourceOne = !mappedBy.isPresent();
        if (mappedBy.isPresent()) {
          joinTable = ((IntermediateJoinTable) ((IntermediateNavigationProperty<?>) targetType.getAssociation(
              mappedBy.get(), false)).getJoinTable());
          if (joinTable == null)
            columns = buildJoinColumnsMapped(mappedBy.get());
          else
            columns = joinTable.buildInverseJoinColumns();
          joinTable = joinTable == null ? null : joinTable.asMapped(this);
        } else {
          if (joinTable == null)
            columns = buildJoinColumnsFromAnnotations(isSourceOne, annotatedElement);
          else
            columns = joinTable.buildJoinColumns();
        }
      }
      return columns;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private List<IntermediateJoinColumn> buildJoinColumnsFromAnnotations(final boolean isSourceOne,
      final AnnotatedElement annotatedElement) throws ODataJPAModelException {

    final var implicitColumns = 0;
    final List<IntermediateJoinColumn> result = new ArrayList<>();
    final var columns = annotatedElement.getAnnotation(JoinColumns.class) != null ? annotatedElement
        .getAnnotation(JoinColumns.class).value() : null;

    if (columns != null) {
      buildJoinColumnPairList(isSourceOne, implicitColumns, result, columns);
    } else {
      final var column = annotatedElement.getAnnotation(JoinColumn.class);
      if (column != null) {
        result.add(buildOneJoinColumnPair(isSourceOne, column));
      } else {
        // No explicit Join Columns given: Build implicit one.
        result.add(buildImplicitJoinColumnPair(isSourceOne));
      }
    }
    return result;
  }

  private List<IntermediateJoinColumn> buildJoinColumnsMapped(final String mappedBy) throws ODataJPAModelException {

    var implicitColumns = 0;
    final var result = invertJoinColumns(targetType.getJoinColumns(mappedBy));
    for (final IntermediateJoinColumn intermediateColumn : result) {
      final var columnName = intermediateColumn.getName();
      if (columnName == null || columnName.isEmpty()) {
        implicitColumns += 1;
        if (implicitColumns > 1)
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
              getInternalName());
        intermediateColumn.setName(((IntermediateProperty) ((IntermediateEntityType<?>) sourceType)
            .getKey().get(0)).getDBFieldName());
      }
    }
    return result;
  }

  @Override
  public Map<String, Annotation> javaAnnotations(final String packageName) {
    return findJavaAnnotation(packageName, ((AnnotatedElement) this.jpaAttribute.getJavaMember()));
  }

  private void buildNavigationProperty() throws ODataJPAModelException {

    this.setExternalName(nameBuilder.buildNaviPropertyName(jpaAttribute));
    retrieveAnnotations(this, Applicability.NAVIGATION_PROPERTY);
    evaluateAnnotation();

    targetType = schema.getEntityType(determineTargetClass());
    if (targetType == null)
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NAVI_PROPERTY_NOT_FOUND,
          jpaAttribute.getName(), sourceType.internalName);

    postProcessor.processNavigationProperty(this, jpaAttribute.getDeclaringType().getJavaType()
        .getCanonicalName());
    // Process annotations after post processing, as external name could have been
    // changed
    getAnnotations(edmAnnotations, this.jpaAttribute.getJavaMember(), internalName);

    checkConsistency();
    if (LOGGER.isTraceEnabled())
      LOGGER.trace(toString());
  }

  private void evaluateAnnotation() throws ODataJPAModelException {

    if (this.jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      final var jpaIgnore = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          EdmIgnore.class);
      if (jpaIgnore != null) {
        this.setIgnore(true);
      }
      final var jpaJoinTable = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
          .getAnnotation(jakarta.persistence.JoinTable.class);
      joinTable = jpaJoinTable != null ? new IntermediateJoinTable(this, jpaJoinTable, schema) : null;

      final var annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      cardinality = jpaAttribute.getPersistentAttributeType();
      if (cardinality != null) {
        mappedBy = switch (cardinality) {
          case ONE_TO_MANY -> {
            // Association '%1$s' of '%2$s' requires a cardinality annotation.
            final var oneTOMany = Optional.ofNullable(annotatedElement.getAnnotation(OneToMany.class))
                .orElseThrow(() -> new ODataJPAModelException(MISSING_ONE_TO_ONE_ANNOTATION, internalName, sourceType
                    .getInternalName()));
            yield Optional.ofNullable(returnNullIfEmpty(oneTOMany.mappedBy()));
          }
          case ONE_TO_ONE -> {
            // Association '%1$s' of '%2$s' requires a cardinality annotation.
            final var annotation = Optional.ofNullable(annotatedElement.getAnnotation(OneToOne.class))
                .orElseThrow(() -> new ODataJPAModelException(MISSING_ONE_TO_ONE_ANNOTATION, internalName, sourceType
                    .getInternalName()));
            yield Optional.ofNullable(returnNullIfEmpty(annotation.mappedBy()));
          }
          case MANY_TO_MANY -> Optional.ofNullable(returnNullIfEmpty(
              annotatedElement.getAnnotation(ManyToMany.class).mappedBy()));
          default -> Optional.empty();
        };
      } else {
        mappedBy = Optional.empty();
      }
    }
  }

  private Class<?> determineTargetClass() {
    Class<?> targetClass = null;
    if (jpaAttribute.isCollection()) {
      targetClass = ((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType().getJavaType();
    } else {
      targetClass = jpaAttribute.getJavaType();
    }
    return targetClass;
  }

  private IntermediateJoinColumn buildOneJoinColumnPair(final boolean isSourceOne, final JoinColumn column)
      throws ODataJPAModelException {
    var intermediateColumn = new IntermediateJoinColumn(column);
    if (cardinality == PersistentAttributeType.ONE_TO_MANY)
      intermediateColumn = new IntermediateJoinColumn(intermediateColumn.getReferencedColumnName(),
          intermediateColumn.getName());
    fillMissingName(isSourceOne, intermediateColumn);
    if (LOGGER.isTraceEnabled())
      LOGGER.trace(getExternalName() + ": Add join condition = " + intermediateColumn.toString());
    return intermediateColumn;
  }

  private void checkConsistency() throws ODataJPAModelException {
    final var jpaProtectedBy = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(EdmProtectedBy.class);
    if (jpaProtectedBy != null) {
      // Navigation Properties do not support EdmProtectedBy
      throw new ODataJPAModelException(MessageKeys.NOT_SUPPORTED_PROTECTED_NAVIGATION, this.sourceType.getTypeClass()
          .getCanonicalName(), this.internalName);
    }
    final var jpaFieldGroups = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(EdmVisibleFor.class);
    if (jpaFieldGroups != null) {
      throw new ODataJPAModelException(MessageKeys.NOT_SUPPORTED_NAVIGATION_PART_OF_GROUP,
          this.sourceType.getTypeClass().getCanonicalName(), this.internalName);
    }
  }

  @CheckForNull
  private void determineOnDelete() {

    if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      final var annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      cardinality = jpaAttribute.getPersistentAttributeType();
      switch (cardinality) {
        case ONE_TO_MANY:
          final var cardinalityOtM = annotatedElement.getAnnotation(OneToMany.class);
          edmNavigationProperty.setOnDelete(edmOnDelete != null ? edmOnDelete
              : setJPAOnDelete(cardinalityOtM
                  .cascade()));
          break;
        case ONE_TO_ONE:
          final var cardinalityOtO = annotatedElement.getAnnotation(OneToOne.class);
          edmNavigationProperty.setNullable(cardinalityOtO.optional());
          edmNavigationProperty.setOnDelete(edmOnDelete != null ? edmOnDelete
              : setJPAOnDelete(cardinalityOtO
                  .cascade()));
          break;
        case MANY_TO_ONE:
          final var cardinalityMtO = annotatedElement.getAnnotation(ManyToOne.class);
          edmNavigationProperty.setNullable(cardinalityMtO.optional());
          edmNavigationProperty.setOnDelete(edmOnDelete != null ? edmOnDelete
              : setJPAOnDelete(cardinalityMtO
                  .cascade()));
          break;
        case MANY_TO_MANY:
          final var cardinalityMtM = annotatedElement.getAnnotation(ManyToMany.class);
          edmNavigationProperty.setOnDelete(edmOnDelete != null ? edmOnDelete
              : setJPAOnDelete(cardinalityMtM
                  .cascade()));
          break;
        default:
          break;
      }
    }
  }

  private JPAAssociationAttribute determinePartner() {
    if (sourceType instanceof IntermediateEntityType) {
      // Partner Attribute must not be defined at Complex Types.
      // JPA bi-directional associations are defined at both sides, e.g.
      // at the BusinessPartner and at the Roles. JPA only defines the
      // "mappedBy" at the Parent.
      try {
        JPAAssociationAttribute other;
        if (mappedBy.isPresent()) {
          other = targetType.getAssociation(mappedBy.get(), true);
        } else {
          other = targetType.getCorrespondingAssociation(sourceType, getInternalName());
        }
        if (other != null) {
          if (((IntermediateModelElement) other).ignore()) {
            LOGGER
                .trace(getExternalName() + ": Found partner = " + other.getExternalName() + ", but has to be ignored");
            return null;
          }
          LOGGER.trace(getExternalName() + ": Found partner = " + other.getExternalName());
          return other;
        }
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAModelInternalException(e);
      }
    }
    return null;
  }

  private Optional<CsdlReferentialConstraint> determineReferentialConstraint(
      final IntermediateJoinColumn intermediateColumn) throws ODataJPAModelException, ODataJPAModelIgnoreException {

    final var constraint = new CsdlReferentialConstraint();
    var ignore = false;
    var property = sourceType.getPropertyByDBField(intermediateColumn.getName());
    if (property != null) {
      if (property.ignore())
        ignore = true;
      constraint.setProperty(property.getExternalName());
      property = targetType.getPropertyByDBField(intermediateColumn.getReferencedColumnName());
      if (property != null) {
        if (property.ignore())
          ignore = true;
        constraint.setReferencedProperty(property.getExternalName());
      } else {
        throw new ODataJPAModelException(REFERENCED_PROPERTY_NOT_FOUND, getInternalName(), intermediateColumn
            .getReferencedColumnName(), targetType.getExternalName());
      }
    } else {
      if (targetType.getPropertyByDBField(intermediateColumn.getReferencedColumnName()) != null) {
        LOGGER.trace("Cloud not determine referential constraint for " + this.getType().getName() + "#"
            + getInternalName());
        throw new ODataJPAModelIgnoreException();
      } else
        return Optional.empty();
    }
    if (ignore)
      throw new ODataJPAModelIgnoreException();
    return Optional.of(constraint);
  }

  private List<CsdlReferentialConstraint> getReferentialConstraints() throws ODataJPAModelException {
    return referentialConstraints.get();
  }

  private List<CsdlReferentialConstraint> determineReferentialConstraints() {
    try {
      final List<CsdlReferentialConstraint> constraints = new ArrayList<>();
      if (jpaAttribute.getJavaMember() instanceof final AnnotatedElement annotatedElement) {
        getJoinColumns();
        final var overwrite = annotatedElement.getAnnotation(AssociationOverride.class);
        if (overwrite != null || joinTable != null)
          return List.of();
        var ignore = false;
        for (final JPAJoinColumn intermediateColumn : getJoinColumns()) {
          try {
            var constraint = determineReferentialConstraint((IntermediateJoinColumn) intermediateColumn);
            if (!constraint.isPresent())
              constraint = determineReverseReferentialConstraint((IntermediateJoinColumn) intermediateColumn);
            constraints.add(constraint.orElseThrow(
                () -> new ODataJPAModelException(REFERENCED_PROPERTY_NOT_FOUND, getInternalName(), intermediateColumn
                    .getReferencedColumnName(), sourceType.getExternalName())));
          } catch (final ODataJPAModelIgnoreException e) {
            ignore = true;
          }
        }
        if (ignore)
          constraints.clear();
      }
      return constraints;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  private Optional<CsdlReferentialConstraint> determineReverseReferentialConstraint(
      final IntermediateJoinColumn intermediateColumn) throws ODataJPAModelException, ODataJPAModelIgnoreException {

    final var constraint = new CsdlReferentialConstraint();
    var ignore = false;
    IntermediateModelElement property = null;
    property = sourceType.getPropertyByDBField(intermediateColumn.getReferencedColumnName());
    if (property != null) {
      if (property.ignore())
        ignore = true;
      constraint.setProperty(property.getExternalName());
      property = targetType.getPropertyByDBField(intermediateColumn.getName());
      if (property != null) {
        if (property.ignore())
          ignore = true;
        constraint.setReferencedProperty(property.getExternalName());
      } else {
        // Target not found: system will fallback to default column names, no
        // referential constraint can be given
        LOGGER.trace("Cloud not determine referential constraint for " + this.getType().getName() + "#"
            + getInternalName());
        throw new ODataJPAModelIgnoreException();
      }
    } else {
      return Optional.empty();
    }
    if (ignore)
      throw new ODataJPAModelIgnoreException();
    return Optional.of(constraint);
  }

  private String fillMissingName() throws ODataJPAModelException {
    return ((IntermediateSimpleProperty) ((IntermediateEntityType<?>) targetType)
        .getKey().get(0)).getDBFieldName();
  }

  private void fillMissingName(final boolean isSourceOne, final IntermediateJoinColumn intermediateColumn)
      throws ODataJPAModelException {

    final var referencedColumnName = intermediateColumn.getReferencedColumnName();
    final var name = intermediateColumn.getName();

    if (isSourceOne && (name == null || name.isEmpty()))
      intermediateColumn.setName(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) sourceType)
          .getKey().get(0)).getDBFieldName());
    else if (isSourceOne && (referencedColumnName == null || referencedColumnName.isEmpty()))
      intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) targetType)
          .getKey().get(0)).getDBFieldName());
    else if (!isSourceOne && (referencedColumnName == null || referencedColumnName.isEmpty()))
      intermediateColumn.setName(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) targetType)
          .getKey().get(0)).getDBFieldName());
    else if (!isSourceOne && (name == null || name.isEmpty()))
      intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) sourceType)
          .getKey().get(0)).getDBFieldName());
  }

  private List<IntermediateJoinColumn> invertJoinColumns(final List<IntermediateJoinColumn> joinColumns) {
    final List<IntermediateJoinColumn> invertedJoinColumns = new ArrayList<>(joinColumns.size());
    for (final IntermediateJoinColumn joinColumn : joinColumns) {
      invertedJoinColumns.add(
          new IntermediateJoinColumn(joinColumn.getReferencedColumnName(), joinColumn.getName()));
    }
    return invertedJoinColumns;
  }

  private CsdlOnDelete setJPAOnDelete(final CascadeType[] cascades) {
    for (final CascadeType cascade : cascades) {
      if (cascade == CascadeType.REMOVE || cascade == CascadeType.ALL) {
        final var onDelete = new CsdlOnDelete();
        onDelete.setAction(CsdlOnDeleteAction.Cascade);
        return onDelete;
      }
    }
    return null;
  }

}
