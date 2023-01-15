package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlNamed;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlCollection;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlNavigationPropertyPath;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyPath;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyValue;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlRecord;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.odata.v4.general.Vocabulary;

class IntermediateAnnotationConverter {
  private static final Log LOGGER = LogFactory.getLog(IntermediateAnnotationConverter.class);
  static final String EDM_NAMESPACE = "Edm";
  static final String IS_COLLECTION_TERM = "Collection";
  static final String CAPABILITIES_NAME = "Org.OData.Capabilities.V1";

  static Optional<CsdlAnnotation> convert(@Nonnull final IntermediateReferences references,
      @Nonnull final Annotation annotation, @Nonnull final IntermediateAnnotatable annotatable) {

    return Optional.ofNullable(annotation.annotationType().getAnnotation(Vocabulary.class))
        .map(Vocabulary::alias)
        .map(a -> new FullQualifiedName(a, annotation.annotationType().getSimpleName()))
        .map(references::getTerm)
        .map(term -> asEdmAnnotation(references, annotation, term, annotatable));

  }

  private static CsdlCollection asCollectionExpression(final IntermediateReferences references,
      final AnnotationType propertyType, final IntermediateAnnotatable annotatable, final Object[] values) {

    final CsdlCollection c = new CsdlCollection();
    final List<CsdlExpression> collectionItems = c.getItems();
    final FullQualifiedName fqn = propertyType.getFqn();
    for (final Object item : splitCollectionValues(propertyType, values)) {
      if (propertyType.isPrimitiveType())
        collectionItems.add(asConstantExpression(fqn, item));
      else if (propertyType.isPathType())
        collectionItems.add(asPathExpression(annotatable, fqn, item));
      else
        collectionItems.add(asRecordExpression(references, (Annotation) item, propertyType, annotatable));
    }
    return c;
  }

  private static CsdlExpression asConstantExpression(final FullQualifiedName typeName, final Object value) {
    if (value.getClass().isEnum())
      return new CsdlConstantExpression(ConstantExpressionType.String, value.toString());
    return new CsdlConstantExpression(asExpressionType(typeName), value.toString());
  }

  private static CsdlPropertyValue asCsdlProperty(final CsdlProperty property, final CsdlExpression expression) {
    final CsdlPropertyValue p = new CsdlPropertyValue();
    p.setValue(expression);
    p.setProperty(property.getName());
    return p;
  }

  private static CsdlAnnotation asEdmAnnotation(final IntermediateReferences references,
      final Annotation annotation, final CsdlTerm term, final IntermediateAnnotatable annotatable) {

    final String alias = annotation.annotationType().getAnnotation(Vocabulary.class).alias();
    final String namespace = references.convertAlias(alias);
    final CsdlAnnotation edmAnnotation = new CsdlAnnotation();
    final AnnotationType annotationType = new AnnotationType(references, term);
    edmAnnotation.setQualifier(namespace);
    edmAnnotation.setTerm(term.getName());
    edmAnnotation.setExpression(buildExpression(references, annotation, annotationType, annotatable));
    return edmAnnotation;
  }

  private static Optional<CsdlExpression> asEnumExpression(final Annotation annotation,
      final AnnotationType propertyType, final String propertyName) {

    final Optional<Object> propertyValue = getPropertyValue(annotation, propertyName);
    return propertyValue.map(v -> asEnumExpression(propertyType, v));
  }

  private static CsdlExpression asEnumExpression(final AnnotationType propertyType,
      @Nonnull final Object propertyValue) {

    final CsdlEnumType enumType = propertyType.asEnumeration();
    final CsdlEnumMember enumMember = enumType.getMember(propertyValue.toString());
    return new CsdlConstantExpression(ConstantExpressionType.EnumMember, enumMember.getName());
  }

  private static ConstantExpressionType asExpressionType(final FullQualifiedName typeFqn) {
    switch (EdmPrimitiveTypeKind.valueOfFQN(typeFqn)) {
      case Boolean:
        return ConstantExpressionType.Bool;
      case Int32:
        return ConstantExpressionType.Int;
      case String:
        return ConstantExpressionType.String;
      default:
        break;
    }
    return null;
  }

  private static CsdlExpression asPathExpression(final IntermediateAnnotatable annotatable, final FullQualifiedName fqn,
      final Object item) {

    try {
      switch (fqn.getName()) {
        case "PropertyPath":
          // http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752659
          final CsdlPropertyPath path = new CsdlPropertyPath();
          path.setValue(annotatable.convertStringToPath(item.toString()).getAlias());
          return path;
        case "NavigationPropertyPath":
          // http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752657
          final CsdlNavigationPropertyPath navigationPath = new CsdlNavigationPropertyPath();
          navigationPath.setValue(annotatable.convertStringToNavigationPath(item.toString()).getAlias());
          return navigationPath;
        default:
          return null;
      }
    } catch (final ODataJPAModelException e) {
      LOGGER.error("Error orrured when trying to convert path '" + item.toString() + "' of " +
          annotatable.getClass().getSimpleName(), e);
      return null;
    }
  }

  private static CsdlExpression asRecordExpression(final IntermediateReferences references, final Annotation annotation,
      final AnnotationType annotationType, final IntermediateAnnotatable annotatable) {

    final CsdlRecord r = new CsdlRecord();
    final List<CsdlPropertyValue> recordProperties = new ArrayList<>();
    r.setType(annotationType.asComplexType().getName());
    r.setPropertyValues(recordProperties);
    for (final CsdlProperty property : annotationType.asComplexType().getProperties()) {
      final AnnotationType propertyType = new AnnotationType(references, property);
      if (propertyType.isCollection()) {
        getPropertyValue(annotation, property.getName())
            .map(Object[].class::cast)
            .map(values -> asCollectionExpression(references, propertyType, annotatable, values))
            .map(e -> asCsdlProperty(property, e))
            .ifPresent(recordProperties::add);
      } else if (propertyType.isEnumeration()) {
        asEnumExpression(annotation, propertyType, property.getName())
            .map(e -> asCsdlProperty(property, e))
            .ifPresent(recordProperties::add);
      } else if (propertyType.isPathType()) {
        getPropertyValue(annotation, property.getName())
            .map(v -> asPathExpression(annotatable, propertyType.getFqn(), v))
            .map(e -> asCsdlProperty(property, e))
            .ifPresent(recordProperties::add);
      } else {
        // A property may have a list of allowed values e.g. FilterRestrictions
        // (https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L430)
        // contains FilterExpressionRestrictionType, which contains AllowedExpressions of type FilterExpressionType. The
        // later one is of type Validation.AllowedValues, which is a list of the allowed values. Such a list is
        // converted into an enumeration, which has to be converted back into a String
        getPropertyValue(annotation, property.getName())
            .map(v -> asConstantExpression(propertyType.getFqn(), v))
            .map(e -> asCsdlProperty(property, e))
            .ifPresent(recordProperties::add);
      }
    }
    return r;
  }

  private static CsdlExpression buildExpression(final IntermediateReferences references, final Annotation annotation,
      final AnnotationType annotationType, final IntermediateAnnotatable annotatable) {

    if (annotationType.isCollection) {
      final CsdlCollection c = new CsdlCollection();
      final Optional<Object> value = getPropertyValue(annotation, "value");
      if (value.isPresent()) {
        for (final Object item : (Object[]) value.get()) {
          c.getItems().add(buildRowExpression(references, annotation, annotationType, annotatable, item));
        }
      }
      return c;
    } else {
      return buildRowExpression(references, annotation, annotationType, annotatable, null);
    }
  }

  private static CsdlExpression buildRowExpression(final IntermediateReferences references, final Annotation annotation,
      final AnnotationType annotationType, final IntermediateAnnotatable annotatable, final Object value) {

    if (annotationType.isComplexType()) {
      return asRecordExpression(references, annotation, annotationType, annotatable);
    } else if (annotationType.isEnumeration()) {
      return asEnumExpression(annotationType, value);
    } else {
      return asConstantExpression(annotationType.getFqn(), value);
    }
  }

  private static String firstToLower(@Nonnull final String name) {
    final char[] asArray = name.toCharArray();
    asArray[0] = Character.toLowerCase(asArray[0]);
    return String.valueOf(asArray);
  }

  private static Optional<Object> getPropertyValue(final Annotation annotation, final String propertyName) {
    try {
      final Method m = annotation.getClass().getMethod(firstToLower(propertyName));
      return Optional.ofNullable(m.invoke(annotation));
    } catch (final NoSuchMethodException unSupported) {
      LOGGER.trace("Unsupported property of annotation: " + propertyName);
      return Optional.empty();
    } catch (SecurityException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e1) {
      LOGGER.error("Not able to convert property: " + propertyName, e1);
      return Optional.empty();
    }
  }

  private static List<Object> splitCollectionValues(final AnnotationType propertyType, final Object[] values) {
    final List<Object> items = new ArrayList<>();
    for (final Object value : values) {
      if (propertyType.isComplexType()) {
        items.add(value);
      } else {
        final String[] itemElements = value.toString().split(",");
        items.addAll(Arrays.asList(itemElements));
      }
    }
    return items;
  }

  private IntermediateAnnotationConverter() {
    super();
  }

  private static class AnnotationType {

    private final FullQualifiedName typeFqn;
    private final Optional<? extends CsdlNamed> type;
    private final boolean isCollection;

    private AnnotationType(final IntermediateReferences references, final CsdlProperty property) {
      this.typeFqn = new FullQualifiedName(property.getType());
      this.isCollection = property.isCollection();
      this.type = references.getType(typeFqn);
    }

    private AnnotationType(final IntermediateReferences references, final CsdlTerm term) {
      this.typeFqn = determineTypeOfTerm(term);
      this.isCollection = this.isCollectionType(term);
      this.type = references.getType(typeFqn);
    }

    @Override
    public String toString() {
      return "AnnotationType [typeFqn=" + typeFqn + "]";
    }

    private CsdlComplexType asComplexType() {
      return (CsdlComplexType) type.get();
    }

    private CsdlEnumType asEnumeration() {
      return ((CsdlEnumType) type.get());
    }

    private FullQualifiedName determineTypeOfTerm(final CsdlTerm term) {
      // E.g.: "Collection(Edm.String) or Capabilities.FilterRestrictionsType
      if (isCollectionType(term))
        return new FullQualifiedName(term.getType().split("\\(")[1].replace(")", ""));
      return new FullQualifiedName(term.getType());
    }

    private FullQualifiedName getFqn() {
      return typeFqn;
    }

    private boolean isCollection() {
      return isCollection;
    }

    private boolean isCollectionType(final CsdlTerm term) {
      return term.getType().startsWith(IS_COLLECTION_TERM);
    }

    private boolean isComplexType() {
      return type.isPresent() && type.get() instanceof CsdlComplexType;
    }

    private boolean isEnumeration() {
      return type.isPresent() && type.get() instanceof CsdlEnumType;
    }

    private boolean isPathType() {
      return EDM_NAMESPACE.equals(typeFqn.getNamespace())
          && ("PropertyPath".equals(typeFqn.getName())
              || "NavigationPropertyPath".equals(typeFqn.getName()));
    }

    private boolean isPrimitiveType() {
      return (EDM_NAMESPACE.equals(typeFqn.getNamespace())
          && EdmPrimitiveTypeKind.getByName(typeFqn.getName()) != null);
    }
  }
}
