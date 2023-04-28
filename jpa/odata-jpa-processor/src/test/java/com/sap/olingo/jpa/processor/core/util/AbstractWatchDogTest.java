package com.sap.olingo.jpa.processor.core.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.annotation.CsdlCollection;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlNavigationPropertyPath;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyValue;

public abstract class AbstractWatchDogTest {

  protected static CsdlPropertyValue createConstantsExpression(final String property, final String value) {
    final CsdlConstantExpression expandable = mock(CsdlConstantExpression.class);
    final CsdlPropertyValue expandableValue = mock(CsdlPropertyValue.class);
    when(expandableValue.getProperty()).thenReturn(property);
    when(expandableValue.getValue()).thenReturn(expandable);
    when(expandable.asConstant()).thenReturn(expandable);
    when(expandable.getValue()).thenReturn(value);
    return expandableValue;
  }

  protected static CsdlPropertyValue createCollectionExpression(final String property,
      final List<CsdlExpression> collection) {
    final CsdlPropertyValue value = mock(CsdlPropertyValue.class);
    final CsdlCollection ascendingOnlyCollection = mock(CsdlCollection.class);
    when(value.getProperty()).thenReturn(property);
    when(value.getValue()).thenReturn(ascendingOnlyCollection);
    when(ascendingOnlyCollection.getItems()).thenReturn(collection);
    when(ascendingOnlyCollection.asDynamic()).thenReturn(ascendingOnlyCollection);
    when(ascendingOnlyCollection.asCollection()).thenReturn(ascendingOnlyCollection);
    return value;
  }

  protected static CsdlNavigationPropertyPath createAnnotationNavigationPath(final String pathString) {
    final CsdlNavigationPropertyPath path = mock(CsdlNavigationPropertyPath.class);
    when(path.asNavigationPropertyPath()).thenReturn(path);
    when(path.asDynamic()).thenReturn(path);
    when(path.getValue()).thenReturn(pathString);
    return path;
  }

  protected static List<CsdlExpression> asExpression(final String[] paths) {
    return asExpression(Arrays.asList(paths));
  }

  protected static List<CsdlExpression> asExpression(final List<String> paths) {
    return paths
        .stream()
        .map(string -> createAnnotationNavigationPath(string))
        .map(path -> (CsdlExpression) path)
        .collect(Collectors.toList());
  }
}
