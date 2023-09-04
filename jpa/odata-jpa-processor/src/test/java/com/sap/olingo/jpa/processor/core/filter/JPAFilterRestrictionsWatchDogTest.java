package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.criteria.Expression;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlCollection;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlDynamicExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyPath;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyValue;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

class JPAFilterRestrictionsWatchDogTest {
  private JPAFilterRestrictionsWatchDog cut;
  private JPAAnnotatable annotatable;
  private CsdlAnnotation annotation;

  private CsdlConstantExpression filterable;
  private CsdlConstantExpression filterRequired;
  private List<CsdlExpression> requiredProperties;

  @BeforeEach
  void setup() throws ODataJPAQueryException {
    annotatable = mock(JPAAnnotatable.class);
    annotation = mock(CsdlAnnotation.class);
    cut = new JPAFilterRestrictionsWatchDog(annotatable);
  }

  private static Stream<Arguments> provideFilteringIsSupported() {
    return Stream.of(
        Arguments.of(false, false, false),
        Arguments.of(true, false, true),
        Arguments.of(true, true, false));
  }

  @ParameterizedTest
  @MethodSource("provideFilteringIsSupported")
  void testHandleIfFilterIsSupportedAndExpressionGiven(final boolean isAnnotated, final boolean annotatedValue,
      final boolean throwsException) throws ODataJPAModelException, ODataJPAQueryException {

    setAnnotation(isAnnotated);
    @SuppressWarnings("unchecked")
    final Expression<Boolean> filter = mock(Expression.class);
    when(filterable.getValue()).thenReturn(Boolean.toString(annotatedValue));
    // when(filterRequired.getValue()).thenReturn(Boolean.toString(false));

    cut = new JPAFilterRestrictionsWatchDog(annotatable);

    assertShallThrow(throwsException, filter);
  }

  private static Stream<Arguments> provideFilteringIsRequired() {
    return Stream.of(
        Arguments.of(false, false, false),
        Arguments.of(true, false, false),
        Arguments.of(true, true, true));
  }

  @ParameterizedTest
  @MethodSource("provideFilteringIsRequired")
  void testHandleIfFilterIsRequiredAndExpressionNotGiven(final boolean isAnnotated, final boolean annotatedValue,
      final boolean throwsException) throws ODataJPAModelException, ODataJPAQueryException {

    setAnnotation(isAnnotated);
    when(filterRequired.getValue()).thenReturn(Boolean.toString(annotatedValue));

    cut = new JPAFilterRestrictionsWatchDog(annotatable);

    assertShallThrow(throwsException, null);
  }

  @Test
  void testHandleBuildRequiredProperties() throws ODataJPAModelException, ODataJPAQueryException {
    setAnnotation(true);
    requiredProperties.add(createAnnotationPath("AlternativeCode"));
    requiredProperties.add(createAnnotationPath("AlternativeId"));
    cut = new JPAFilterRestrictionsWatchDog(annotatable);
    assertEquals(2, cut.getRequiredPropertyPath().size());
  }

  @Test
  void testHandleVisitedProperty() throws ODataJPAModelException, ODataJPAQueryException {
    setAnnotation(true);
    requiredProperties.add(createAnnotationPath("AlternativeCode"));
    requiredProperties.add(createAnnotationPath("AlternativeId"));
    cut = new JPAFilterRestrictionsWatchDog(annotatable);

    final JPAPath firstPath = mock(JPAPath.class);
    when(firstPath.getAlias()).thenReturn("AlternativeCode");
    cut.watch(firstPath);
    assertEquals(1, cut.getRequiredPropertyPath().size());

    final JPAPath secondPath = mock(JPAPath.class);
    when(secondPath.getAlias()).thenReturn("Code");
    cut.watch(secondPath);
    assertEquals(1, cut.getRequiredPropertyPath().size());

    final JPAPath thirdPath = mock(JPAPath.class);
    when(thirdPath.getAlias()).thenReturn("AlternativeId");
    cut.watch(thirdPath);
    assertTrue(cut.getRequiredPropertyPath().isEmpty());
  }

  @Test
  void testHandleNotAllPropertiesUsed() throws ODataJPAModelException, ODataJPAQueryException {
    @SuppressWarnings("unchecked")
    final Expression<Boolean> filter = mock(Expression.class);
    setAnnotation(true);
    requiredProperties.add(createAnnotationPath("AlternativeId"));
    when(filterable.getValue()).thenReturn(Boolean.toString(true));
    when(filterRequired.getValue()).thenReturn(Boolean.toString(true));

    cut = new JPAFilterRestrictionsWatchDog(annotatable);

    assertThrows(ODataJPAFilterException.class, () -> cut.watch(filter));
  }

  @Test
  void testHandleNotAllPropertiesUsedButNotRequired() throws ODataJPAModelException, ODataJPAQueryException {
    @SuppressWarnings("unchecked")
    final Expression<Boolean> filter = mock(Expression.class);
    setAnnotation(true);
    requiredProperties.add(createAnnotationPath("AlternativeId"));
    when(filterable.getValue()).thenReturn(Boolean.toString(true));
    when(filterRequired.getValue()).thenReturn(Boolean.toString(false));

    cut = new JPAFilterRestrictionsWatchDog(annotatable);

    assertDoesNotThrow(() -> cut.watch(filter));
  }

  // Filter not required but required properties

  private void assertShallThrow(final boolean throwsException, final Expression<Boolean> filter) {
    if (throwsException)
      assertThrows(ODataJPAFilterException.class, () -> cut.watch(filter));
    else
      assertDoesNotThrow(() -> cut.watch(filter));
  }

  private void setAnnotation(final boolean isAnnotated) throws ODataJPAModelException {
    initAnnotation();

    if(isAnnotated)
      when(annotatable.getAnnotation(JPAFilterRestrictionsWatchDog.VOCABULARY_ALIAS, JPAFilterRestrictionsWatchDog.TERM))
        .thenReturn(annotation);
    else
      when(annotatable.getAnnotation(JPAFilterRestrictionsWatchDog.VOCABULARY_ALIAS, JPAFilterRestrictionsWatchDog.TERM))
        .thenReturn(null);
  }

  private CsdlPropertyPath createAnnotationPath(final String pathString) {
    final CsdlPropertyPath path = mock(CsdlPropertyPath.class);
    when(path.asPropertyPath()).thenReturn(path);
    when(path.asDynamic()).thenReturn(path);
    when(path.getValue()).thenReturn(pathString);
    return path;
  }

  private void initAnnotation() {
    final CsdlDynamicExpression expression = mock(CsdlDynamicExpression.class);
    final CsdlRecord record = mock(CsdlRecord.class);

    filterable = mock(CsdlConstantExpression.class);
    filterRequired = mock(CsdlConstantExpression.class);
    requiredProperties = new ArrayList<>();

    final CsdlPropertyValue filterableValue = createSingleExpression(filterable,
        JPAFilterRestrictionsWatchDog.FILTERABLE);
    final CsdlPropertyValue filterRequiredValue = createSingleExpression(filterRequired,
        JPAFilterRestrictionsWatchDog.REQUIRES_FILTER);
    final CsdlPropertyValue requiredPropertiesValue = createCollectionExpression(requiredProperties,
        JPAFilterRestrictionsWatchDog.REQUIRED_PROPERTIES);

    final List<CsdlPropertyValue> propertyValues = Arrays.asList(filterableValue, filterRequiredValue,
        requiredPropertiesValue);

    when(annotation.getExpression()).thenReturn(expression);
    when(expression.asDynamic()).thenReturn(expression);
    when(expression.asRecord()).thenReturn(record);
    when(record.getPropertyValues()).thenReturn(propertyValues);
  }

  private CsdlPropertyValue createSingleExpression(final CsdlConstantExpression expression,
      final String property) {

    final CsdlPropertyValue value = mock(CsdlPropertyValue.class);
    when(value.getProperty()).thenReturn(property);
    when(value.getValue()).thenReturn(expression);
    when(expression.asConstant()).thenReturn(expression);
    return value;
  }

  private CsdlPropertyValue createCollectionExpression(final List<CsdlExpression> collection,
      final String property) {

    final CsdlPropertyValue value = mock(CsdlPropertyValue.class);
    final CsdlCollection csdlCollection = mock(CsdlCollection.class);
    when(value.getProperty()).thenReturn(property);
    when(value.getValue()).thenReturn(csdlCollection);
    when(csdlCollection.getItems()).thenReturn(collection);
    when(csdlCollection.asDynamic()).thenReturn(csdlCollection);
    when(csdlCollection.asCollection()).thenReturn(csdlCollection);
    return value;
  }
}
