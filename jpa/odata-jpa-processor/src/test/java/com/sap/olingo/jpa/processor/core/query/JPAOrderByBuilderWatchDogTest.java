package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;

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
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

class JPAOrderByBuilderWatchDogTest {
  private JPAOrderByBuilderWatchDog cut;
  private JPAAnnotatable annotatable;
  private List<CsdlExpression> ascendingOnly;
  private List<CsdlExpression> descendingOnly;
  private List<CsdlExpression> nonSortable;
  private CsdlConstantExpression sortable;
  private CsdlAnnotation annotation;

  @BeforeEach
  void setup() throws ODataJPAQueryException {
    annotatable = mock(JPAAnnotatable.class);
    annotation = mock(CsdlAnnotation.class);
    cut = new JPAOrderByBuilderWatchDog(annotatable);
  }

  @Test
  void testCanCreateInstanceWithoutAnnotatable() {
    assertNotNull(new JPAOrderByBuilderWatchDog());
  }

  private static Stream<Arguments> provideSortingIsNotSupported() {
    return Stream.of(
        Arguments.of(false, false),
        Arguments.of(true, true));
  }

  @ParameterizedTest
  @MethodSource("provideSortingIsNotSupported")
  void testHandleIfSortingIsNotSupportedByOrderByGiven(final boolean isAnnotated, final boolean throwsException)
      throws ODataJPAModelException, ODataJPAQueryException {

    setAnnotation(isAnnotated);
    final List<Order> orderClauses = Collections.singletonList(createOrderByClause("AlternativeCode"));
    when(sortable.getValue()).thenReturn("false");

    cut = new JPAOrderByBuilderWatchDog(annotatable);

    assertShallThrow(throwsException, orderClauses);
  }

  private static Stream<Arguments> provideSortingIsSupported() {
    return Stream.of(
        Arguments.of(false, false),
        Arguments.of(true, false));
  }

  @ParameterizedTest
  @MethodSource("provideSortingIsSupported")
  void testHandleIfSortingIsSupportedByOrderByGiven(final boolean isAnnotated, final boolean throwsException)
      throws ODataJPAModelException, ODataJPAQueryException {

    setAnnotation(isAnnotated);
    final List<Order> orderClauses = Collections.singletonList(createOrderByClause("AlternativeCode"));
    when(sortable.getValue()).thenReturn("true");

    cut = new JPAOrderByBuilderWatchDog(annotatable);

    assertShallThrow(throwsException, orderClauses);
  }

  private static Stream<Arguments> provideHandleSortingOnNonSortableColumn() {
    return Stream.of(
        Arguments.of(false, false),
        Arguments.of(true, true));
  }

  @ParameterizedTest
  @MethodSource("provideHandleSortingOnNonSortableColumn")
  void testHandleSortingOnNonSortableColumn(final boolean isAnnotated, final boolean throwsException)
      throws ODataJPAModelException, ODataJPAQueryException {

    setAnnotation(isAnnotated);
    when(sortable.getValue()).thenReturn("true");
    final List<Order> orderClauses = Collections.singletonList(createOrderByClause("AlternativeCode"));
    nonSortable.add(createAnnotationPath("AlternativeCode"));

    cut = new JPAOrderByBuilderWatchDog(annotatable);

    assertShallThrow(throwsException, orderClauses);
  }

  private static Stream<Arguments> provideHandleSortingOnSortableColumn() {
    return Stream.of(
        Arguments.of(false, false),
        Arguments.of(true, false));
  }

  @ParameterizedTest
  @MethodSource("provideHandleSortingOnSortableColumn")
  void testHandleSortingOnSortableColumn(final boolean isAnnotated, final boolean throwsException)
      throws ODataJPAModelException, ODataJPAQueryException {

    setAnnotation(isAnnotated);
    when(sortable.getValue()).thenReturn("true");
    final List<Order> orderClauses = Collections.singletonList(createOrderByClause("Code"));
    nonSortable.add(createAnnotationPath("AlternativeCode"));

    cut = new JPAOrderByBuilderWatchDog(annotatable);

    assertShallThrow(throwsException, orderClauses);
  }

  private static Stream<Arguments> provideHandleDescendingOnAscendingOnlyColumn() {
    return Stream.of(
        Arguments.of(false, false),
        Arguments.of(true, true));
  }

  @ParameterizedTest
  @MethodSource("provideHandleDescendingOnAscendingOnlyColumn")
  void testHandleDescendingOnAscendingOnlyColumn(final boolean isAnnotated, final boolean throwsException)
      throws ODataJPAModelException, ODataJPAQueryException {

    setAnnotation(isAnnotated);
    when(sortable.getValue()).thenReturn("true");
    final List<Order> orderClauses = Collections.singletonList(createOrderByClause("AlternativeCode", false));
    ascendingOnly.add(createAnnotationPath("AlternativeCode"));

    cut = new JPAOrderByBuilderWatchDog(annotatable);

    assertShallThrow(throwsException, orderClauses);
  }

  private static Stream<Arguments> provideHandleAscendingOnAscendingOnlyColumn() {
    return Stream.of(
        Arguments.of(false, false),
        Arguments.of(true, false));
  }

  @ParameterizedTest
  @MethodSource("provideHandleAscendingOnAscendingOnlyColumn")
  void testHandleAscendingOnAscendingOnlyColumn(final boolean isAnnotated, final boolean throwsException)
      throws ODataJPAModelException, ODataJPAQueryException {

    setAnnotation(isAnnotated);
    when(sortable.getValue()).thenReturn("true");
    final List<Order> orderClauses = Collections.singletonList(createOrderByClause("AlternativeCode", true));
    ascendingOnly.add(createAnnotationPath("AlternativeCode"));

    cut = new JPAOrderByBuilderWatchDog(annotatable);

    assertShallThrow(throwsException, orderClauses);
  }

  private static Stream<Arguments> provideHandleAscendingOnDescendingOnlyColumn() {
    return Stream.of(
        Arguments.of(false, false),
        Arguments.of(true, true));
  }

  @ParameterizedTest
  @MethodSource("provideHandleAscendingOnDescendingOnlyColumn")
  void testHandleAscendingOnDescendingOnlyColumn(final boolean isAnnotated, final boolean throwsException)
      throws ODataJPAModelException, ODataJPAQueryException {

    setAnnotation(isAnnotated);
    when(sortable.getValue()).thenReturn("true");
    final List<Order> orderClauses = Collections.singletonList(createOrderByClause("AlternativeCode", true));
    descendingOnly.add(createAnnotationPath("AlternativeCode"));

    cut = new JPAOrderByBuilderWatchDog(annotatable);

    assertShallThrow(throwsException, orderClauses);
  }

  private static Stream<Arguments> provideHandleDescendingOnDescendingOnlyColumn() {
    return Stream.of(
        Arguments.of(false, false),
        Arguments.of(true, false));
  }

  @ParameterizedTest
  @MethodSource("provideHandleDescendingOnDescendingOnlyColumn")
  void testHandleDescendingOnDescendingOnlyColumn(final boolean isAnnotated, final boolean throwsException)
      throws ODataJPAModelException, ODataJPAQueryException {

    setAnnotation(isAnnotated);
    when(sortable.getValue()).thenReturn("true");
    final List<Order> orderClauses = Collections.singletonList(createOrderByClause("AlternativeCode", false));
    descendingOnly.add(createAnnotationPath("AlternativeCode"));

    cut = new JPAOrderByBuilderWatchDog(annotatable);

    assertShallThrow(throwsException, orderClauses);
  }

  private void assertShallThrow(final boolean throwsException, final List<Order> orderClauses) {
    if (throwsException)
      assertThrows(ODataJPAQueryException.class, () -> cut.watch(orderClauses));
    else
      assertDoesNotThrow(() -> cut.watch(orderClauses));
  }

  private Order createOrderByClause(final String alias) {
    return createOrderByClause(alias, true);
  }

  private Order createOrderByClause(final String alias, final boolean isAscending) {
    final Order orderClause = mock(Order.class);
    final Expression<?> orderExpression = mock(Expression.class);
    when(orderExpression.getAlias()).thenReturn(alias);
    when(orderClause.isAscending()).thenReturn(isAscending);
    doReturn(orderExpression).when(orderClause).getExpression();
    return orderClause;
  }

  private CsdlPropertyPath createAnnotationPath(final String pathString) {
    final CsdlPropertyPath path = mock(CsdlPropertyPath.class);
    when(path.asPropertyPath()).thenReturn(path);
    when(path.asDynamic()).thenReturn(path);
    when(path.getValue()).thenReturn(pathString);
    return path;
  }

  private void setAnnotation(final boolean isAnnotated) throws ODataJPAModelException {
    initAnnotation();

    if(isAnnotated)
      when(annotatable.getAnnotation(JPAOrderByBuilderWatchDog.VOCABULARY_ALIAS, JPAOrderByBuilderWatchDog.TERM))
        .thenReturn(annotation);
    else
      when(annotatable.getAnnotation(JPAOrderByBuilderWatchDog.VOCABULARY_ALIAS, JPAOrderByBuilderWatchDog.TERM))
        .thenReturn(null);
  }

  private void initAnnotation() {
    final CsdlDynamicExpression expression = mock(CsdlDynamicExpression.class);
    final CsdlRecord record = mock(CsdlRecord.class);

    sortable = mock(CsdlConstantExpression.class);
    ascendingOnly = new ArrayList<>();
    descendingOnly = new ArrayList<>();
    nonSortable = new ArrayList<>();

    final CsdlPropertyValue sortableValue = mock(CsdlPropertyValue.class);
    when(sortableValue.getProperty()).thenReturn(JPAOrderByBuilderWatchDog.SORTABLE);
    when(sortableValue.getValue()).thenReturn(sortable);
    when(sortable.asConstant()).thenReturn(sortable);

    final CsdlPropertyValue ascendingOnlyValue = createCollectionExpression(ascendingOnly,
        JPAOrderByBuilderWatchDog.ASCENDING_ONLY_PROPERTIES);
    final CsdlPropertyValue descendingOnlyValue = createCollectionExpression(descendingOnly,
        JPAOrderByBuilderWatchDog.DESCENDING_ONLY_PROPERTIES);
    final CsdlPropertyValue nonSortableValue = createCollectionExpression(nonSortable,
        JPAOrderByBuilderWatchDog.NON_SORTABLE_PROPERTIES);

    final List<CsdlPropertyValue> propertyValues = Arrays.asList(sortableValue, ascendingOnlyValue, descendingOnlyValue,
        nonSortableValue);

    when(annotation.getExpression()).thenReturn(expression);
    when(expression.asDynamic()).thenReturn(expression);
    when(expression.asRecord()).thenReturn(record);
    when(record.getPropertyValues()).thenReturn(propertyValues);
  }

  private CsdlPropertyValue createCollectionExpression(final List<CsdlExpression> collection, final String property) {
    final CsdlPropertyValue value = mock(CsdlPropertyValue.class);
    final CsdlCollection ascendingOnlyCollection = mock(CsdlCollection.class);
    when(value.getProperty()).thenReturn(property);
    when(value.getValue()).thenReturn(ascendingOnlyCollection);
    when(ascendingOnlyCollection.getItems()).thenReturn(collection);
    when(ascendingOnlyCollection.asDynamic()).thenReturn(ascendingOnlyCollection);
    when(ascendingOnlyCollection.asCollection()).thenReturn(ascendingOnlyCollection);
    return value;
  }
}
