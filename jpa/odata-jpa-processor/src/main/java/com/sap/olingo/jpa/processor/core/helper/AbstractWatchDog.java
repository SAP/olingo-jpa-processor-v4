package com.sap.olingo.jpa.processor.core.helper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlCollection;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlDynamicExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlNavigationPropertyPath;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyPath;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyValue;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlRecord;

public class AbstractWatchDog {

  public AbstractWatchDog() {
    super();
  }

  protected Optional<String> determineConstantExpression(final Map<String, CsdlExpression> properties,
      final String propertyName) {
    return Optional.ofNullable(properties.get(propertyName))
        .map(CsdlExpression::asConstant)
        .map(CsdlConstantExpression::getValue);
  }

  protected List<CsdlExpression> getItems(final Map<String, CsdlExpression> properties, final String property) {
    return Optional.ofNullable(properties.get(property))
        .map(CsdlExpression::asDynamic)
        .map(CsdlDynamicExpression::asCollection)
        .map(CsdlCollection::getItems)
        .orElseGet(Collections::emptyList);
  }

  protected List<String> getPathItems(final Map<String, CsdlExpression> properties, final String property) {
    return getItems(properties, property)
        .stream()
        .map(CsdlExpression::asDynamic)
        .map(CsdlDynamicExpression::asPropertyPath)
        .map(CsdlPropertyPath::getValue)
        .collect(Collectors.toList());
  }

  protected List<String> getNavigationPathItems(final Map<String, CsdlExpression> properties, final String property) {
    return getItems(properties, property)
        .stream()
        .map(CsdlExpression::asDynamic)
        .map(CsdlDynamicExpression::asNavigationPropertyPath)
        .map(CsdlNavigationPropertyPath::getValue)
        .collect(Collectors.toList());
  }

  protected Map<String, CsdlExpression> getAnnotationProperties(final Optional<CsdlAnnotation> annotation) {
    return annotation
        .map(CsdlAnnotation::getExpression)
        .map(CsdlExpression::asDynamic)
        .map(CsdlDynamicExpression::asRecord)
        .map(CsdlRecord::getPropertyValues)
        .orElse(Collections.emptyList())
        .stream()
        .collect(Collectors.toMap(CsdlPropertyValue::getProperty, CsdlPropertyValue::getValue));
  }
}