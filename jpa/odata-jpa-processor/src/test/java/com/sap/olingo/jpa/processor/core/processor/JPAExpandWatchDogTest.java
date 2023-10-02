package com.sap.olingo.jpa.processor.core.processor;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlDynamicExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyValue;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlRecord;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.util.AbstractWatchDogTest;
import com.sap.olingo.jpa.processor.core.util.ExpandOptionDouble;

class JPAExpandWatchDogTest extends AbstractWatchDogTest {
  private static final String ES_EXTERNAL_NAME = "Organizations";
  private JPAExpandWatchDog cut;

  @Test
  void testCanCreateInstanceWithoutAnnotatable() throws ODataJPAProcessException {
    assertNotNull(new JPAExpandWatchDog(Optional.empty()));
  }

  private static Stream<Arguments> provideIsExpandable() {
    return Stream.of(
        Arguments.of(Optional.empty(), true),
        Arguments.of(createAnnotatable(false, -1), false),
        Arguments.of(createAnnotatable(true, -1), true),
        Arguments.of(createAnnotatable(null, -1), true));
  }

  @ParameterizedTest
  @MethodSource("provideIsExpandable")
  void testIsExpandable(final Optional<JPAAnnotatable> annotatable, final boolean exp)
      throws ODataJPAProcessException {

    cut = new JPAExpandWatchDog(annotatable);
    assertEquals(exp, cut.isExpandable());
  }

  private static Stream<Arguments> provideRemainingLevels() {
    return Stream.of(
        Arguments.of(Optional.empty(), Integer.MAX_VALUE),
        Arguments.of(createAnnotatable(true, -1), Integer.MAX_VALUE),
        Arguments.of(createAnnotatable(true, -7), Integer.MAX_VALUE),
        Arguments.of(createAnnotatable(true, 0), 0),
        Arguments.of(createAnnotatable(true, 3), 3),
        Arguments.of(createAnnotatable(true, null), Integer.MAX_VALUE));
  }

  @ParameterizedTest
  @MethodSource("provideRemainingLevels")
  void testRemainingLevels(final Optional<JPAAnnotatable> annotatable, final Integer exp)
      throws ODataJPAProcessException {

    cut = new JPAExpandWatchDog(annotatable);
    assertEquals(exp, cut.getRemainingLevels());
  }

  private static Stream<Arguments> provideSimpleChecks() {
    return Stream.of(
        Arguments.of(createAnnotatable(true, -1)),
        Arguments.of(createAnnotatable(false, -1)));
  }

  @ParameterizedTest
  @MethodSource("provideSimpleChecks")
  void testWatchChecksExpandOptNonNull(final Optional<JPAAnnotatable> annotatable)
      throws ODataJPAProcessException {
    cut = new JPAExpandWatchDog(annotatable);
    assertDoesNotThrow(() -> cut.watch(null, null));
  }

  @ParameterizedTest
  @MethodSource("provideSimpleChecks")
  void testWatchChecksExpandOptionWithoutItems(final Optional<JPAAnnotatable> annotatable)
      throws ODataJPAProcessException {
    final ExpandOption expandOption = new ExpandOptionDouble("", Collections.emptyList());
    cut = new JPAExpandWatchDog(annotatable);
    assertDoesNotThrow(() -> cut.watch(expandOption, emptyList()));
  }

  @Test
  void testWatchChecksExpandNotSupported() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(false, -1, "Children");
    final ExpandItem expandItem = createExpandItem("Parent");
    final ExpandOption expandOption = new ExpandOptionDouble("", Collections.singletonList(expandItem));
    cut = new JPAExpandWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(expandOption, emptyList()));
  }

  @Test
  void testWatchChecksOneExpandAllowed() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, -1, "Children");
    final ExpandItem expandItem = createExpandItem("Parent");
    final ExpandOption expandOption = new ExpandOptionDouble("", Collections.singletonList(expandItem));
    cut = new JPAExpandWatchDog(annotatable);
    assertDoesNotThrow(() -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  private static Stream<Arguments> provideOneExpandNotAllowed() {
    return Stream.of(
        Arguments.of(createAnnotatable(true, -1, "Parent"), new String[] { "Parent" }),
        Arguments.of(createAnnotatable(true, -1, "Parent/Parent"), new String[] { "Parent", "Parent" }));
  }

  @ParameterizedTest
  @MethodSource("provideOneExpandNotAllowed")
  void testWatchChecksOneExpandNotAllowed(final Optional<JPAAnnotatable> annotatable, final String[] expandPaths)
      throws ODataJPAProcessException {
    final ExpandItem expandItem = createExpandItem(expandPaths);
    final ExpandOption expandOption = new ExpandOptionDouble("", Collections.singletonList(expandItem));
    cut = new JPAExpandWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  @Test
  void testWatchChecksOneOfMultipleExpandNotAllowed() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, -1, "Parent");
    final ExpandItem expandItemChildren = createExpandItem("Children");
    final ExpandItem expandItemParent = createExpandItem("Parent");
    final ExpandItem expandItemSibling = createExpandItem("Silbling");
    final ExpandOption expandOption = new ExpandOptionDouble("",
        Arrays.asList(expandItemChildren, expandItemParent, expandItemSibling));
    cut = new JPAExpandWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  @Test
  void testWatchDoesNotCheckStar() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, -1, "Parent");
    final ExpandItem expandItem = mock(ExpandItem.class);
    when(expandItem.isStar()).thenReturn(Boolean.TRUE);

    final ExpandOption expandOption = new ExpandOptionDouble("", singletonList(expandItem));

    cut = new JPAExpandWatchDog(annotatable);
    assertDoesNotThrow(() -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  private static Stream<Arguments> provideNonExpandableProperties() {
    return Stream.of(
        Arguments.of(Optional.empty(), Collections.emptyList()),
        Arguments.of(createAnnotatable(true, -1, "Parent"), Arrays.asList("Parent")),
        Arguments.of(createAnnotatable(true, -1), Collections.emptyList()));
  }

  @ParameterizedTest
  @MethodSource("provideNonExpandableProperties")
  void testNonExpandableProperties(final Optional<JPAAnnotatable> annotatable, final List<String> exp)
      throws ODataJPAProcessException {

    cut = new JPAExpandWatchDog(annotatable);
    assertEquals(exp, cut.getNonExpandableProperties());
  }

  @Test
  void testWatchChecksOneExpandViaNavigationNotAllowed() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, -1,
        "AdministrativeInformation/CreatedBy/User");
    final ExpandItem expandItem = createExpandItem("CreatedBy", "User");
    final ExpandOption expandOption = new ExpandOptionDouble("", Collections.singletonList(expandItem));

    final UriResource esUriResource = mock(UriResource.class);
    when(esUriResource.getSegmentValue()).thenReturn(ES_EXTERNAL_NAME);
    final UriResource complexUriResource = mock(UriResource.class);
    when(complexUriResource.getSegmentValue()).thenReturn("AdministrativeInformation");

    cut = new JPAExpandWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(expandOption,
        Arrays.asList(esUriResource, complexUriResource)));
  }

  @Test
  void testWatchChecksOneExpandLevelBelowMaxLevel() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, 2);
    final ExpandItem expandItem = createExpandItem("Parent");
    final ExpandOption expandOption = new ExpandOptionDouble("", Collections.singletonList(expandItem));

    cut = new JPAExpandWatchDog(annotatable);
    assertDoesNotThrow(() -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  @Test
  void testWatchChecksOneExpandLevelAboveMaxLevel1() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, 1);
    final ExpandItem expandItem = createExpandItem("Parent");
    final ExpandOption expandOption = new ExpandOptionDouble("", singletonList(expandItem));
    final ExpandItem secondExpandItem = createExpandItem("Parent");
    final ExpandOption secondExpandOption = new ExpandOptionDouble("", singletonList(secondExpandItem));

    when(expandItem.getExpandOption()).thenReturn(secondExpandOption);

    cut = new JPAExpandWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  @Test
  void testWatchChecksOneExpandLevelAboveMaxLevel2() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, 2);
    final ExpandItem expandItem = createExpandItem("Parent");
    final ExpandOption expandOption = new ExpandOptionDouble("", singletonList(expandItem));
    final ExpandItem secondExpandItem = createExpandItem("Parent");
    final ExpandOption secondExpandOption = new ExpandOptionDouble("", singletonList(secondExpandItem));
    final ExpandItem thirdExpandItem = createExpandItem("Parent");
    final ExpandOption thirdExpandOption = new ExpandOptionDouble("", singletonList(thirdExpandItem));

    when(secondExpandItem.getExpandOption()).thenReturn(thirdExpandOption);
    when(expandItem.getExpandOption()).thenReturn(secondExpandOption);

    cut = new JPAExpandWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  @Test
  void testWatchChecksTwoExpandLevelAboveMaxLevel2() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, 2);
    final ExpandItem expandItem1 = createExpandItem("Parent");
    final ExpandItem expandItem2 = createExpandItem("Parent");
    final ExpandOption expandOption = new ExpandOptionDouble("", Arrays.asList(expandItem2, expandItem1, expandItem2));
    final ExpandItem secondExpandItem = createExpandItem("Parent");
    final ExpandOption secondExpandOption = new ExpandOptionDouble("", singletonList(secondExpandItem));
    final ExpandItem thirdExpandItem = createExpandItem("Parent");
    final ExpandOption thirdExpandOption = new ExpandOptionDouble("", singletonList(thirdExpandItem));

    when(secondExpandItem.getExpandOption()).thenReturn(thirdExpandOption);
    when(expandItem1.getExpandOption()).thenReturn(secondExpandOption);

    cut = new JPAExpandWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  @Test
  void testWatchChecksOneExpandWithLevelBelowMaxLevel() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, 2);
    final ExpandItem expandItem = createExpandItem("Parent");
    final LevelsExpandOption levelOption = mock(LevelsExpandOption.class);
    when(expandItem.getLevelsOption()).thenReturn(levelOption);
    when(levelOption.getValue()).thenReturn(1);
    final ExpandOption expandOption = new ExpandOptionDouble("", Collections.singletonList(expandItem));

    cut = new JPAExpandWatchDog(annotatable);
    assertDoesNotThrow(() -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  @Test
  void testWatchChecksOneExpandWithLevelAboveMaxLevel() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, 2);
    final ExpandItem expandItem = createExpandItem("Parent");
    final LevelsExpandOption levelOption = mock(LevelsExpandOption.class);
    when(expandItem.getLevelsOption()).thenReturn(levelOption);
    when(levelOption.getValue()).thenReturn(3);
    final ExpandOption expandOption = new ExpandOptionDouble("", Collections.singletonList(expandItem));

    cut = new JPAExpandWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  @Test
  void testWatchChecksTwoExpandWithLevelAboveMaxLevel() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, 2);
    final ExpandItem expandItem1 = createExpandItem("Parent");
    final ExpandItem expandItem2 = createExpandItem("Child");
    final LevelsExpandOption levelOption = mock(LevelsExpandOption.class);
    when(expandItem1.getLevelsOption()).thenReturn(levelOption);
    when(levelOption.getValue()).thenReturn(3);
    final ExpandOption expandOption = new ExpandOptionDouble("", Arrays.asList(expandItem2, expandItem1, expandItem2));

    cut = new JPAExpandWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  @Test
  void testWatchChecksOneExpandWithMaxLevel() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, 2);
    final ExpandItem expandItem1 = createExpandItem("Parent");
    final LevelsExpandOption levelOption = mock(LevelsExpandOption.class);
    when(expandItem1.getLevelsOption()).thenReturn(levelOption);
    when(levelOption.getValue()).thenReturn(0);
    when(levelOption.isMax()).thenReturn(Boolean.TRUE);
    final ExpandOption expandOption = new ExpandOptionDouble("", Arrays.asList(expandItem1));

    cut = new JPAExpandWatchDog(annotatable);
    assertDoesNotThrow(() -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  @Test
  void testWatchChecksTwoExpandWithOneMaxLevel() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, 2);
    final ExpandItem expandItem1 = createExpandItem("Parent");
    final ExpandItem expandItem2 = createExpandItem("Child");
    final LevelsExpandOption levelOption = mock(LevelsExpandOption.class);
    when(expandItem1.getLevelsOption()).thenReturn(levelOption);
    when(levelOption.getValue()).thenReturn(0);
    when(levelOption.isMax()).thenReturn(Boolean.TRUE);

    final LevelsExpandOption levelOption2 = mock(LevelsExpandOption.class);
    when(expandItem2.getLevelsOption()).thenReturn(levelOption2);
    when(levelOption2.getValue()).thenReturn(1);

    final ExpandOption expandOption = new ExpandOptionDouble("", Arrays.asList(expandItem1, expandItem2));

    cut = new JPAExpandWatchDog(annotatable);
    assertDoesNotThrow(() -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  @Test
  void testWatchChecksTwoExpandWithOneMaxLevelOneAboveMaxLevel() throws ODataJPAProcessException {
    final Optional<JPAAnnotatable> annotatable = createAnnotatable(true, 2);
    final ExpandItem expandItem1 = createExpandItem("Parent");
    final ExpandItem expandItem2 = createExpandItem("Child");
    final LevelsExpandOption levelOption = mock(LevelsExpandOption.class);
    when(expandItem1.getLevelsOption()).thenReturn(levelOption);
    when(levelOption.getValue()).thenReturn(0);
    when(levelOption.isMax()).thenReturn(Boolean.TRUE);

    final LevelsExpandOption levelOption2 = mock(LevelsExpandOption.class);
    when(expandItem2.getLevelsOption()).thenReturn(levelOption2);
    when(levelOption2.getValue()).thenReturn(3);

    final ExpandOption expandOption = new ExpandOptionDouble("", Arrays.asList(expandItem1, expandItem2));

    cut = new JPAExpandWatchDog(annotatable);
    assertThrows(ODataJPAProcessException.class, () -> cut.watch(expandOption, queryPathOnlyRoot()));
  }

  private List<UriResource> queryPathOnlyRoot() {
    final UriResource esUriResource = mock(UriResource.class);
    when(esUriResource.getSegmentValue()).thenReturn(ES_EXTERNAL_NAME);
    return singletonList(esUriResource);
  }

  private ExpandItem createExpandItem(final String... expandPaths) {
    final ExpandItem expandItem = mock(ExpandItem.class);
    final UriInfoResource uriInfoResource = mock(UriInfoResource.class);
    when(expandItem.getResourcePath()).thenReturn(uriInfoResource);

    final List<UriResource> expandResources = new ArrayList<>();
    for (final String expandPath : expandPaths) {
      final UriResource uriResource = mock(UriResource.class);
      when(uriResource.getSegmentValue()).thenReturn(expandPath);
      expandResources.add(uriResource);
    }
    when(uriInfoResource.getUriResourceParts()).thenReturn(expandResources);
    return expandItem;
  }

  private static Optional<JPAAnnotatable> createAnnotatable(final Boolean isExpandable, final Integer levels,
      final String... nonExpandable) {

    final JPAAnnotatable annotatable = mock(JPAAnnotatable.class);
    final CsdlAnnotation annotation = mock(CsdlAnnotation.class);

    final CsdlDynamicExpression expression = mock(CsdlDynamicExpression.class);
    final CsdlRecord record = mock(CsdlRecord.class);

    final CsdlPropertyValue expandableValue = createConstantsExpression(JPAExpandWatchDog.EXPANDABLE,
        isExpandable == null ? null : isExpandable.toString());
    final CsdlPropertyValue levelsValue = createConstantsExpression(JPAExpandWatchDog.MAX_LEVELS,
        levels == null ? null : levels.toString());
    final CsdlPropertyValue nonExpandableValue = createCollectionExpression(JPAExpandWatchDog.NON_EXPANDABLE_PROPERTIES,
        asExpression(nonExpandable));

    final List<CsdlPropertyValue> propertyValues = Arrays.asList(expandableValue, levelsValue, nonExpandableValue);

    when(annotation.getExpression()).thenReturn(expression);
    when(expression.asDynamic()).thenReturn(expression);
    when(expression.asRecord()).thenReturn(record);
    when(record.getPropertyValues()).thenReturn(propertyValues);
    when(annotatable.getExternalName()).thenReturn(ES_EXTERNAL_NAME);

    try {
      when(annotatable.getAnnotation(JPAExpandWatchDog.VOCABULARY_ALIAS, JPAExpandWatchDog.TERM))
          .thenReturn(annotation);
    } catch (final ODataJPAModelException e) {
      fail();
    }

    return Optional.of(annotatable);
  }

}
