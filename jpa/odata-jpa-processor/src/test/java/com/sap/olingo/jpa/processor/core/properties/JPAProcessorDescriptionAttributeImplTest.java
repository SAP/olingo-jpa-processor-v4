package com.sap.olingo.jpa.processor.core.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalArgumentException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

class JPAProcessorDescriptionAttributeImplTest extends AbstractJPAProcessorAttributeTest {

  @Override
  protected void createCutSortOrder(final boolean descending) {
    cut = new JPAProcessorDescriptionAttributeImpl(path, Collections.emptyList(), descending, Locale.ENGLISH);
  }

  @Test
  @Override
  void testJoinRequired() {
    cut = new JPAProcessorDescriptionAttributeImpl(path, Collections.emptyList(), false, Locale.ENGLISH);
    assertTrue(cut.requiresJoin());
  }

  @Override
  void testJoinNotRequired() {
    // Description Properties allays require join
  }

  @Test
  void testIsSortable() {
    cut = new JPAProcessorDescriptionAttributeImpl(path, Collections.emptyList(), true, Locale.ENGLISH);
    assertTrue(cut.isSortable());
  }

  @Test
  void testAliasFromPath() {
    cut = new JPAProcessorDescriptionAttributeImpl(path, Collections.emptyList(), true, Locale.UK);
    assertEquals("path", cut.getAlias());
  }

  @Test
  void testExceptionOnMoreThanOneHop() {
    final var hop2 = mock(JPAAssociationPath.class);
    final ODataJPAIllegalArgumentException act = assertThrows(ODataJPAIllegalArgumentException.class, // NOSONAR
        () -> new JPAProcessorDescriptionAttributeImpl(path, Arrays.asList(hop, hop2), true, Locale.ENGLISH));
    assertEquals(1, act.getParams().length);
  }

  private static Stream<Arguments> provideJoinPreconditions() {
    return Stream.of(
        Arguments.of(true),
        Arguments.of(false));
  }

  @ParameterizedTest
  @MethodSource("provideJoinPreconditions")
  void testCreateJoin(final boolean isLocale) {
    final var cb = mock(CriteriaBuilder.class);
    final var from = mock(From.class);
    final var join = mock(Join.class);
    final var localePath = createLocaleFiledPath(join);
    createDescriptionProperty(isLocale, localePath);

    final var target = mock(JPAElement.class);
    when(target.getInternalName()).thenReturn("description");
    when(from.join("description", JoinType.LEFT)).thenReturn(join);
    when(hop.getPath()).thenReturn(Collections.singletonList(target));

    cut = new JPAProcessorDescriptionAttributeImpl(path, Collections.emptyList(), true, Locale.UK);
    cut.setTarget(from, Collections.emptyMap(), cb);
    final var act = cut.createJoin();
    assertEquals(join, act);
    verify(cb).equal(localePath.right, isLocale ? Locale.UK.toString() : Locale.UK.getLanguage());
  }

  @Test
  void testCreateJoinWithOnCondition() {
    final var cb = mock(CriteriaBuilder.class);
    final var from = mock(From.class);
    final var join = mock(Join.class);
    final var onCondition = mock(Predicate.class);
    final var localeOnCondition = mock(Predicate.class);
    final var localePath = createLocaleFiledPath(join);
    createDescriptionProperty(true, localePath);

    final var target = mock(JPAElement.class);
    when(target.getInternalName()).thenReturn("description");
    when(from.join("description", JoinType.LEFT)).thenReturn(join);
    when(hop.getPath()).thenReturn(Collections.singletonList(target));
    when(join.getOn()).thenReturn(onCondition);
    when(cb.equal(localePath.right, Locale.UK.toString())).thenReturn(localeOnCondition);

    cut = new JPAProcessorDescriptionAttributeImpl(path, Collections.emptyList(), true, Locale.UK);
    cut.setTarget(from, Collections.emptyMap(), cb);
    final var act = cut.createJoin();
    assertEquals(join, act);
    verify(cb).and(onCondition, localeOnCondition);
  }

  @Test
  void testCreateJoinWithFixedValues() {
    final var cb = mock(CriteriaBuilder.class);
    final var from = mock(From.class);
    final var join = mock(Join.class);
    final var localeOnCondition = mock(Predicate.class);
    final ImmutablePair<Map<Path<Object>, String>, Map<JPAPath, String>> fixedValues = getFixValues(join);
    final var localePath = createLocaleFiledPath(join);
    final var descriptionProperty = createDescriptionProperty(true, localePath);

    final var target = mock(JPAElement.class);
    when(target.getInternalName()).thenReturn("description");
    when(from.join("description", JoinType.LEFT)).thenReturn(join);
    when(hop.getPath()).thenReturn(Collections.singletonList(target));
    when(descriptionProperty.getFixedValueAssignment()).thenReturn(fixedValues.right);

    when(cb.equal(localePath.right, Locale.UK.toString())).thenReturn(localeOnCondition);

    cut = new JPAProcessorDescriptionAttributeImpl(path, Collections.emptyList(), true, Locale.UK);
    cut.setTarget(from, Collections.emptyMap(), cb);
    final var act = cut.createJoin();
    assertEquals(join, act);
    for (final var fixValue : fixedValues.left.entrySet())
      verify(cb).equal(fixValue.getKey(), fixValue.getValue());
  }

  @Test
  @Override
  void testCreateJoinThrowsExceptionIfSetTargetNotCalled() {

    cut = new JPAProcessorDescriptionAttributeImpl(path, Collections.emptyList(), true, Locale.UK);
    assertThrows(IllegalAccessError.class, () -> cut.createJoin());
  }

  @Test
  void testGetPath() {
    final var cb = mock(CriteriaBuilder.class);
    final var from = mock(From.class);
    final var join = mock(Join.class);
    final var localePath = createLocaleFiledPath(join);
    final JPADescriptionAttribute attribute = createDescriptionProperty(false, localePath);
    final JPAAttribute descriptionAttribute = mock(JPAAttribute.class);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.getDescriptionAttribute()).thenReturn(descriptionAttribute);
    when(descriptionAttribute.getInternalName()).thenReturn("name");

    final var criteriaPath = mock(Path.class);
    final var target = mock(JPAElement.class);
    when(target.getInternalName()).thenReturn("description");
    when(from.join("description", JoinType.LEFT)).thenReturn(join);
    when(hop.getPath()).thenReturn(Collections.singletonList(target));
    when(join.get("name")).thenReturn(criteriaPath);

    cut = new JPAProcessorDescriptionAttributeImpl(path, Collections.emptyList(), true, Locale.UK);
    cut.setTarget(from, Collections.emptyMap(), cb);
    assertNotNull(cut.getPath());
    assertEquals(criteriaPath, cut.getPath());
  }

  @Test
  void testCreateJoinTakenFromCache() {
    final var cb = mock(CriteriaBuilder.class);
    final var from = mock(From.class);
    final var join = mock(Join.class);
    final var target = mock(JPAElement.class);
    when(target.getInternalName()).thenReturn("target");
    when(hop.getPath()).thenReturn(Collections.singletonList(target));

    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.singletonList(hop), true);
    cut.setTarget(from, Collections.singletonMap("hophop", join), cb);
    final var act = cut.createJoin();
    assertEquals(join, act);
    verify(from, times(0)).join(anyString(), any());
  }

  @Test
  void testOrderByOfTransientThrowsException() {
    final var cb = mock(CriteriaBuilder.class);
    final var attribute = mock(JPAAttribute.class);
    when(path.isTransient()).thenReturn(true);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.toString()).thenReturn("Test");
    when(path.isPartOfGroups(any())).thenReturn(true);
    cut = new JPAProcessorDescriptionAttributeImpl(path, Collections.emptyList(), true, Locale.UK);
    final var act = assertThrows(ODataJPAQueryException.class, () -> cut.createOrderBy(cb, Collections.emptyList()));
    assertEquals(400, act.getStatusCode());
  }

  @Test
  void testOrderByNotInGroupsThrowsException() {
    final var cb = mock(CriteriaBuilder.class);
    final var attribute = mock(JPAAttribute.class);
    when(path.isTransient()).thenReturn(false);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.toString()).thenReturn("Test");
    when(path.isPartOfGroups(any())).thenReturn(false);
    cut = new JPAProcessorDescriptionAttributeImpl(path, Collections.emptyList(), true, Locale.UK);
    final var act = assertThrows(ODataJPAQueryException.class, () -> cut.createOrderBy(cb, Collections.emptyList()));
    assertEquals(403, act.getStatusCode());
  }

  private ImmutablePair<Map<Path<Object>, String>, Map<JPAPath, String>>
      getFixValues(final Join<?, ?> join) {
    final Map<Path<Object>, String> fixedPath = new HashMap<>();
    final Map<JPAPath, String> fixedValues = new HashMap<>();

    final var valueOnePath = getFixValue(join, "one");
    final var valueTwoPath = getFixValue(join, "two");

    fixedValues.put(valueOnePath.right, "One");
    fixedPath.put(valueOnePath.left, "One");
    fixedValues.put(valueTwoPath.right, "Two");
    fixedPath.put(valueTwoPath.left, "Two");
    return new ImmutablePair<>(fixedPath, fixedValues);
  }

  @SuppressWarnings("unchecked")
  private ImmutablePair<Path<Object>, JPAPath> getFixValue(final Join<?, ?> join, final String name) {
    final var valueOnePath = mock(JPAPath.class);
    final var valueOneElement = mock(JPAElement.class);
    final var valueOneExpression = mock(Path.class);
    when(valueOneElement.getInternalName()).thenReturn(name);
    when(valueOnePath.getPath()).thenReturn(Collections.singletonList(valueOneElement));
    when(join.get(name)).thenReturn(valueOneExpression);
    return new ImmutablePair<>(valueOneExpression, valueOnePath);
  }

  protected JPADescriptionAttribute createDescriptionProperty(final boolean isLocale,
      final ImmutablePair<JPAPath, Path<Object>> localePath) {
    final var descriptionProperty = mock(JPADescriptionAttribute.class);
    when(descriptionProperty.getLocaleFieldName()).thenReturn(localePath.left);
    when(descriptionProperty.isLocationJoin()).thenReturn(isLocale);
    when(descriptionProperty.getInternalName()).thenReturn("description");

    when(path.getLeaf()).thenReturn(descriptionProperty);
    when(path.getPath()).thenReturn(Arrays.asList(descriptionProperty));
    return descriptionProperty;
  }

  @SuppressWarnings("unchecked")
  private ImmutablePair<JPAPath, Path<Object>> createLocaleFiledPath(final Join<?, ?> join) {
    final var complex = mock(JPAElement.class);
    final var locale = mock(JPAElement.class);
    final var path = mock(JPAPath.class);

    final var complexPath = mock(Path.class);
    final var localePath = mock(Path.class);

    when(path.getPath()).thenReturn(Arrays.asList(complex, locale));
    when(complex.getInternalName()).thenReturn("complex");
    when(locale.getInternalName()).thenReturn("locale");

    when(join.get("complex")).thenReturn(complexPath);
    when(complexPath.get("locale")).thenReturn(localePath);

    return new ImmutablePair<>(path, localePath);
  }
}
