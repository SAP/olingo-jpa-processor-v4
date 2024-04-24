package com.sap.olingo.jpa.metadata.odata.v4.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlNamed;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlCollection;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlNavigationPropertyPath;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyPath;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyValue;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlRecord;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.JPAReferences;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPathNotFoundException;
import com.sap.olingo.jpa.metadata.odata.v4.general.Vocabulary;

class JavaAnnotationConverter {
  private static final Log LOGGER = LogFactory.getLog(JavaAnnotationConverter.class);
  static final String EDM_NAMESPACE = "Edm";
  static final String IS_COLLECTION_TERM = "Collection";
  static final String NULL = "null";

  JavaAnnotationConverter() {
    super();
  }

  Optional<CsdlAnnotation> convert(@Nonnull final JPAReferences references,
      @Nonnull final Annotation annotation, @Nonnull final ODataAnnotatable annotatable) {

    return Optional.ofNullable(annotation.annotationType().getAnnotation(Vocabulary.class))
        .map(Vocabulary::alias)
        .map(references::convertAlias)
        .map(name -> new FullQualifiedName(name, annotation.annotationType().getSimpleName()))
        .map(fqn -> references.getTerm(fqn).orElse(null))
        .map(term -> asEdmAnnotation(references, annotation, term, annotatable));

  }

  private CsdlCollection asCollectionExpression(final JPAReferences references,
      final AnnotationType propertyType, final ODataAnnotatable annotatable, final Object[] values) {

    final var collection = new CsdlCollection();
    final var collectionItems = collection.getItems();
    final var fqn = propertyType.getFqn();
    for (final Object item : splitCollectionValues(propertyType, values)) {
      if (propertyType.isPrimitiveType())
        collectionItems.add(asConstantExpression(propertyType, item));
      else if (propertyType.isPathType()) {
        final var expression = asPathExpression(annotatable, fqn, item);
        if (expression != null)
          collectionItems.add(asPathExpression(annotatable, fqn, item));
      } else
        collectionItems.add(asRecordExpression(references, (Annotation) item, propertyType, annotatable));
    }
    return collection;
  }

  private CsdlExpression asConstantExpression(final AnnotationType annotation, final Object value) {
    if (value.getClass().isEnum())
      return new CsdlConstantExpression(ConstantExpressionType.String, value.toString());
    return new CsdlConstantExpression(asExpressionType(annotation), value.toString());
  }

  private CsdlPropertyValue asCsdlProperty(final CsdlProperty property, final CsdlExpression expression) {
    final var propertyValue = new CsdlPropertyValue();
    propertyValue.setValue(expression);
    propertyValue.setProperty(property.getName());
    return propertyValue;
  }

  private CsdlAnnotation asEdmAnnotation(final JPAReferences references,
      final Annotation annotation, final CsdlTerm term, final ODataAnnotatable annotatable) {

    final var alias = annotation.annotationType().getAnnotation(Vocabulary.class).alias();
    final var namespace = references.convertAlias(alias);
    final var edmAnnotation = new CsdlAnnotation();
    final var annotationType = new AnnotationType(references, term);
    edmAnnotation.setTerm(new FullQualifiedName(namespace, term.getName()).getFullQualifiedNameAsString());
    edmAnnotation.setExpression(buildExpression(references, annotation, annotationType, annotatable));
    return edmAnnotation;
  }

  private Optional<CsdlExpression> asEnumExpression(final Annotation annotation,
      final AnnotationType propertyType, final String propertyName) {

    final var propertyValue = getPropertyValue(annotation, propertyName);
    return propertyValue.map(value -> asEnumExpression(propertyType, value));
  }

  private CsdlExpression asEnumExpression(final AnnotationType propertyType,
      @Nonnull final Object propertyValue) {

    final var enumType = propertyType.asEnumeration();
    final var enumMember = enumType.getMember(propertyValue.toString());
    return new CsdlConstantExpression(ConstantExpressionType.EnumMember,
        enumMember == null ? NULL : enumMember.getName());
  }

  private ConstantExpressionType asExpressionType(final AnnotationType annotation) {

    final var fqn = annotation.getBaseType() != null ? annotation.getBaseType() : annotation.getFqn();
    return switch (EdmPrimitiveTypeKind.valueOfFQN(fqn)) {
      case Boolean -> ConstantExpressionType.Bool;
      case Int32 -> ConstantExpressionType.Int;
      case String -> ConstantExpressionType.String;
      default -> null;
    };
  }

  @CheckForNull
  private CsdlExpression asPathExpression(final ODataAnnotatable annotatable, final FullQualifiedName fqn,
      final Object item) {

    try {
      return switch (fqn.getName()) {
        case "PropertyPath" -> {
          // http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752659
          final var path = new CsdlPropertyPath();
          path.setValue(annotatable.convertStringToPath(item.toString()).getPathAsString());
          yield path;
        }
        case "NavigationPropertyPath" -> {
          // http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752657
          final var navigationPath = new CsdlNavigationPropertyPath();
          navigationPath.setValue(annotatable.convertStringToNavigationPath(item.toString()).getPathAsString());
          yield navigationPath;
        }
        default -> null;
      };
    } catch (final ODataPathNotFoundException e) {
      LOGGER.error("Error orrured when trying to convert path '" + item.toString() + "' of " +
          annotatable.getClass().getSimpleName(), e);
      return null;
    }
  }

  private CsdlExpression asRecordExpression(final JPAReferences references, final Annotation annotation,
      final AnnotationType annotationType, final ODataAnnotatable annotatable) {

    final var csdlRecord = new CsdlRecord();
    final List<CsdlPropertyValue> recordProperties = new ArrayList<>();
    csdlRecord.setType(annotationType.getFqn().getFullQualifiedNameAsString());
    csdlRecord.setPropertyValues(recordProperties);
    for (final CsdlProperty property : annotationType.asComplexType().getProperties()) {
      final var propertyType = new AnnotationType(references, property);
      if (propertyType.isCollection()) {
        getPropertyValue(annotation, property.getName())
            .map(Object[].class::cast)
            .map(values -> asCollectionExpression(references, propertyType, annotatable, values))
            .map(expression -> asCsdlProperty(property, expression))
            .ifPresent(recordProperties::add);
      } else if (propertyType.isEnumeration()) {
        asEnumExpression(annotation, propertyType, property.getName())
            .map(expression -> asCsdlProperty(property, expression))
            .ifPresent(recordProperties::add);
      } else if (propertyType.isPathType()) {
        getPropertyValue(annotation, property.getName())
            .map(value -> asPathExpression(annotatable, propertyType.getFqn(), value))
            .filter(Objects::nonNull)
            .map(expression -> asCsdlProperty(property, expression))
            .ifPresent(recordProperties::add);
      } else {
        // A property may have a list of allowed values e.g. FilterRestrictions
        // (https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L430)
        // contains FilterExpressionRestrictionType, which contains AllowedExpressions of type FilterExpressionType. The
        // later one is of type Validation.AllowedValues, which is a list of the allowed values. Such a list is
        // converted into an enumeration, which has to be converted back into a String
        getPropertyValue(annotation, property.getName())
            .map(value -> asConstantExpression(propertyType, value))
            .map(expression -> asCsdlProperty(property, expression))
            .ifPresent(recordProperties::add);
      }
    }
    return csdlRecord;
  }

  private CsdlExpression buildExpression(final JPAReferences references, final Annotation annotation,
      final AnnotationType annotationType, final ODataAnnotatable annotatable) {

    final var value = getPropertyValue(annotation, "value");
    if (annotationType.isCollection) {
      final var collection = new CsdlCollection();
      if (value.isPresent()) {
        for (final Object item : (Object[]) value.get()) {
          collection.getItems().add(buildRowExpression(references, annotation, annotationType, annotatable, item));
        }
      }
      return collection;
    } else {
      return buildRowExpression(references, annotation, annotationType, annotatable, value.orElse(null));
    }
  }

  private CsdlExpression buildRowExpression(final JPAReferences references, final Annotation annotation,
      final AnnotationType annotationType, final ODataAnnotatable annotatable, final Object value) {

    if (annotationType.isComplexType()) {
      return asRecordExpression(references, annotation, annotationType, annotatable);
    } else if (annotationType.isEnumeration()) {
      return asEnumExpression(annotationType, value);
    } else {
      return asConstantExpression(annotationType, value);
    }
  }

  private String firstToLower(@Nonnull final String name) {
    final var asArray = name.toCharArray();
    asArray[0] = Character.toLowerCase(asArray[0]);
    return String.valueOf(asArray);
  }

  private Optional<Object> getPropertyValue(final Annotation annotation, final String propertyName) {
    try {
      final var method = annotation.getClass().getMethod(firstToLower(propertyName));
      return Optional.ofNullable(method.invoke(annotation));
    } catch (final NoSuchMethodException unSupported) {
      LOGGER.trace("Unsupported property of annotation: " + propertyName);
      return Optional.empty();
    } catch (SecurityException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e1) {
      LOGGER.error("Not able to convert property: " + propertyName, e1);
      return Optional.empty();
    }
  }

  private List<Object> splitCollectionValues(final AnnotationType propertyType, final Object[] values) {
    final List<Object> items = new ArrayList<>();
    for (final Object value : values) {
      if (propertyType.isComplexType()) {
        items.add(value);
      } else {
        final var itemElements = value.toString().split(",");
        items.addAll(Arrays.asList(itemElements));
      }
    }
    return items;
  }

  private static class AnnotationType {

    private final FullQualifiedName typeFqn;
    private final Optional<? extends CsdlNamed> type;
    private final boolean isCollection;

    private AnnotationType(final JPAReferences references, final CsdlProperty property) {
      this.typeFqn = new FullQualifiedName(property.getType());
      this.isCollection = property.isCollection();
      this.type = references.getType(typeFqn);
    }

    private AnnotationType(final JPAReferences references, final CsdlTerm term) {
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

    private FullQualifiedName getBaseType() {
      return type
          .filter(CsdlTypeDefinition.class::isInstance)
          .map(CsdlTypeDefinition.class::cast)
          .map(CsdlTypeDefinition::getUnderlyingType)
          .map(FullQualifiedName::new)
          .orElse(null);
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
