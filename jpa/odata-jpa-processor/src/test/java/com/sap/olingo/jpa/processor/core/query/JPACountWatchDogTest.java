package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlDynamicExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyValue;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlRecord;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceCount;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.util.AbstractWatchDogTest;

class JPACountWatchDogTest extends AbstractWatchDogTest {
  private static final String ES_EXTERNAL_NAME = "Organizations";
  private JPACountWatchDog cut;

  private static Stream<Arguments> provideIsExpandable() {
    return Stream.of(
        Arguments.of(Optional.empty(), true),
        Arguments.of(createAnnotatable(false, Collections.emptyList()), false),
        Arguments.of(createAnnotatable(true, Collections.emptyList()), true),
        Arguments.of(createAnnotatable(null, Collections.emptyList()), true));
  }

  @ParameterizedTest
  @MethodSource("provideIsExpandable")
  void testIsCountable(final Optional<JPAAnnotatable> annotatable, final boolean exp)
      throws ODataJPAProcessException {

    cut = new JPACountWatchDog(annotatable);
    assertEquals(exp, cut.isCountable());
  }

  private static Stream<Arguments> provideNonCountableProperties() {
    return Stream.of(
        Arguments.of(Optional.empty(), Collections.emptyList()),
        Arguments.of(createAnnotatable(true, Collections.emptyList(), "Parent"), Arrays.asList("Parent")),
        Arguments.of(createAnnotatable(true, Collections.emptyList()), Collections.emptyList()));
  }

  @ParameterizedTest
  @MethodSource("provideNonCountableProperties")
  void testNonCountableProperties(final Optional<JPAAnnotatable> annotatable, final List<String> exp)
      throws ODataJPAProcessException {

    cut = new JPACountWatchDog(annotatable);
    assertEquals(exp, cut.getNonCountableProperties());
  }

  private static Stream<Arguments> provideNonCountableNavigationProperties() {
    return Stream.of(
        Arguments.of(Optional.empty(), Collections.emptyList()),
        Arguments.of(createAnnotatable(true, Arrays.asList("Children")), Arrays.asList("Children")),
        Arguments.of(createAnnotatable(true, Collections.emptyList()), Collections.emptyList()));
  }

  @ParameterizedTest
  @MethodSource("provideNonCountableNavigationProperties")
  void testNonCountableNavigationProperties(final Optional<JPAAnnotatable> annotatable, final List<String> exp)
      throws ODataJPAProcessException {

    cut = new JPACountWatchDog(annotatable);
    assertEquals(exp, cut.getNonCountableNavigationProperties());
  }

  @Test
  void testWatchChecksCountQueryNotSupported() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(false, Collections.emptyList());
    final UriInfoResource uriResource = mock(UriInfoResource.class);
    final CountOption countOption = mock(CountOption.class);
    when(uriResource.getCountOption()).thenReturn(countOption);
    when(countOption.getValue()).thenReturn(Boolean.TRUE);

    cut = new JPACountWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(uriResource));
  }

  @Test
  void testWatchChecksCountPathNotSupported() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(false, Collections.emptyList());
    final UriInfoResource uriResource = mock(UriInfoResource.class);
    final List<UriResource> uriResourceParts = new ArrayList<>();
    when(uriResource.getUriResourceParts()).thenReturn(uriResourceParts);

    final UriResourceCount count = mock(UriResourceCount.class);
    when(count.getKind()).thenReturn(UriResourceKind.count);
    final UriResourceEntitySet es = mock(UriResourceEntitySet.class);
    uriResourceParts.add(es);
    uriResourceParts.add(count);

    cut = new JPACountWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(uriResource));
  }

  private static Stream<Arguments> provideWatchNonCountableProperties() {
    return Stream.of(
        Arguments.of(Optional.empty(), "Parent", false),
        Arguments.of(createAnnotatable(true, Collections.emptyList(), "Parent"), "Parent", true),
        Arguments.of(createAnnotatable(true, Collections.emptyList()), "Parent", false));
  }

  @ParameterizedTest
  @MethodSource("provideWatchNonCountableProperties")
  void testWatchNonCountableComplexProperties(final Optional<JPAAnnotatable> annotatable,
      final String propertyName, final boolean throwException) throws ODataJPAProcessException {

    final UriInfoResource uriResource = mock(UriInfoResource.class);
    final List<UriResource> uriResourceParts = new ArrayList<>();
    when(uriResource.getUriResourceParts()).thenReturn(uriResourceParts);

    final UriResourceCount count = mock(UriResourceCount.class);
    when(count.getKind()).thenReturn(UriResourceKind.count);
    final UriResourceEntitySet es = mock(UriResourceEntitySet.class);
    final EdmEntitySet set = mock(EdmEntitySet.class);
    when(es.getEntitySet()).thenReturn(set);
    when(set.getName()).thenReturn(ES_EXTERNAL_NAME);
    final UriResourceComplexProperty collection = mock(UriResourceComplexProperty.class);
    final EdmProperty property = mock(EdmProperty.class);
    when(collection.getProperty()).thenReturn(property);
    when(collection.isCollection()).thenReturn(Boolean.TRUE);
    when(property.getName()).thenReturn(propertyName);
    when(property.isCollection()).thenReturn(Boolean.TRUE);
    uriResourceParts.add(es);
    uriResourceParts.add(collection);
    uriResourceParts.add(count);

    cut = new JPACountWatchDog(annotatable);
    if (throwException)
      assertThrows(ODataJPAProcessException.class, () -> cut.watch(uriResource));
  }

  @ParameterizedTest
  @MethodSource("provideWatchNonCountableProperties")
  void testWatchNonCountableSimpleProperties(final Optional<JPAAnnotatable> annotatable,
      final String propertyName, final boolean throwException) throws ODataJPAProcessException {

    final UriInfoResource uriResource = mock(UriInfoResource.class);
    final List<UriResource> uriResourceParts = new ArrayList<>();
    when(uriResource.getUriResourceParts()).thenReturn(uriResourceParts);

    final UriResourceCount count = mock(UriResourceCount.class);
    when(count.getKind()).thenReturn(UriResourceKind.count);
    final UriResourceEntitySet es = mock(UriResourceEntitySet.class);
    final EdmEntitySet set = mock(EdmEntitySet.class);
    when(es.getEntitySet()).thenReturn(set);
    when(set.getName()).thenReturn(ES_EXTERNAL_NAME);
    final UriResourcePrimitiveProperty collection = mock(UriResourcePrimitiveProperty.class);
    final EdmProperty property = mock(EdmProperty.class);
    when(collection.getProperty()).thenReturn(property);
    when(collection.isCollection()).thenReturn(Boolean.TRUE);
    when(property.getName()).thenReturn(propertyName);
    when(property.isCollection()).thenReturn(Boolean.TRUE);
    uriResourceParts.add(es);
    uriResourceParts.add(collection);
    uriResourceParts.add(count);

    cut = new JPACountWatchDog(annotatable);
    if (throwException)
      assertThrows(ODataJPAProcessException.class, () -> cut.watch(uriResource));
  }

  private static Stream<Arguments> provideWatchNonCountableNavigationProperties() {
    return Stream.of(
        Arguments.of(Optional.empty(), "Parent", false),
        Arguments.of(createAnnotatable(true, Arrays.asList("Parent")), "Parent", true),
        Arguments.of(createAnnotatable(true, Collections.emptyList()), "Parent", false));
  }

  @ParameterizedTest
  @MethodSource("provideWatchNonCountableNavigationProperties")
  void testWatchNonCountableNavigationProperties(final Optional<JPAAnnotatable> annotatable,
      final String propertyName, final boolean throwException) throws ODataJPAProcessException {

    final UriInfoResource uriResource = mock(UriInfoResource.class);
    final List<UriResource> uriResourceParts = new ArrayList<>();
    when(uriResource.getUriResourceParts()).thenReturn(uriResourceParts);

    final UriResourceCount count = mock(UriResourceCount.class);
    when(count.getKind()).thenReturn(UriResourceKind.count);
    final UriResourceEntitySet es = mock(UriResourceEntitySet.class);
    final EdmEntitySet set = mock(EdmEntitySet.class);
    when(es.getEntitySet()).thenReturn(set);
    when(set.getName()).thenReturn(ES_EXTERNAL_NAME);
    final UriResourceNavigation navigation = mock(UriResourceNavigation.class);
    final EdmNavigationProperty property = mock(EdmNavigationProperty.class);
    when(navigation.getProperty()).thenReturn(property);
    when(property.getName()).thenReturn(propertyName);
    uriResourceParts.add(es);
    uriResourceParts.add(navigation);
    uriResourceParts.add(count);

    cut = new JPACountWatchDog(annotatable);
    if (throwException)
      assertThrows(ODataJPAProcessException.class, () -> cut.watch(uriResource));
  }

  private static Optional<JPAAnnotatable> createAnnotatable(final Boolean isCountable,
      final List<String> nonCountableNavigation,
      final String... nonCountable) {

    final JPAAnnotatable annotatable = mock(JPAAnnotatable.class);
    final CsdlAnnotation annotation = mock(CsdlAnnotation.class);

    final CsdlDynamicExpression expression = mock(CsdlDynamicExpression.class);
    final CsdlRecord record = mock(CsdlRecord.class);

    final CsdlPropertyValue countableValue = createConstantsExpression(JPACountWatchDog.COUNTABLE,
        isCountable == null ? null : isCountable.toString());
    final CsdlPropertyValue nonCountableNavigationValue = createCollectionExpression(
        JPACountWatchDog.NON_COUNTABLE_NAVIGATION_PROPERTIES, asExpression(nonCountableNavigation));
    final CsdlPropertyValue nonCountableValue = createCollectionExpression(JPACountWatchDog.NON_COUNTABLE_PROPERTIES,
        asExpression(nonCountable));

    final List<CsdlPropertyValue> propertyValues = Arrays.asList(countableValue, nonCountableValue,
        nonCountableNavigationValue);

    when(annotation.getExpression()).thenReturn(expression);
    when(expression.asDynamic()).thenReturn(expression);
    when(expression.asRecord()).thenReturn(record);
    when(record.getPropertyValues()).thenReturn(propertyValues);
    when(annotatable.getExternalName()).thenReturn(ES_EXTERNAL_NAME);

    try {
      when(annotatable.getAnnotation(JPACountWatchDog.VOCABULARY_ALIAS, JPACountWatchDog.TERM))
          .thenReturn(annotation);
    } catch (final ODataJPAModelException e) {
      fail();
    }

    return Optional.of(annotatable);
  }
}
