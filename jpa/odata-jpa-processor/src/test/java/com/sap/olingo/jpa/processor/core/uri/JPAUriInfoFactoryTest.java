package com.sap.olingo.jpa.processor.core.uri;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmSingleton;
import org.apache.olingo.commons.api.edm.EdmStructuredType;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoKind;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceSingleton;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.FormatOption;
import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SkipTokenOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;

class JPAUriInfoFactoryTest {
  private JPAUriInfoFactory cut;
  private UriInfo original;
  private JPAODataPage page;

  @BeforeEach
  void setup() {
    original = mock(UriInfo.class);
  }

  @Test
  void testFactoryReturnsOriginalForDefaultPage() {
    page = new JPAODataPage(original, 0, Integer.MAX_VALUE, "Test");
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();
    assertNull(act.getTopOption());
    assertNull(act.getSkipOption());
  }

  @Test
  void testFactorySetSystemOptionForTop() {
    page = new JPAODataPage(original, 0, 10, "Test");
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertNotNull(act.getTopOption());
    assertEquals(10, act.getTopOption().getValue());
    assertNull(act.getSkipOption());
  }

  @Test
  void testFactorySetSystemOptionForSkip() {
    page = new JPAODataPage(original, 20, Integer.MAX_VALUE, "Test");
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertNull(act.getTopOption());
    assertNotNull(act.getSkipOption());
    assertEquals(20, act.getSkipOption().getValue());
  }

  @Test
  void testFactorySetSystemOptionForSkipToken() {
    page = new JPAODataPage(original, 20, Integer.MAX_VALUE, "Test");
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertNotNull(act.getSkipTokenOption());
    assertEquals("Test", act.getSkipTokenOption().getValue());
  }

  @Test
  void testFactoryRemoveSkipTokenIfNull() {
    createSkipToken();
    page = new JPAODataPage(original, 20, Integer.MAX_VALUE, null);
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertNull(act.getSkipTokenOption());
  }

  @Test
  void testFactoryReplaceSkipToken() {
    createSkipToken();
    page = new JPAODataPage(original, 20, Integer.MAX_VALUE, "New");
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertNotNull(act.getSkipTokenOption());
    assertEquals("New", act.getSkipTokenOption().getValue());
  }

  @Test
  void testFactoryConvertsOneExpand() {
    createUriInfoEntitySetOneExpand("Children");
    page = new JPAODataPage(original, 20, 10, "New",
        singletonList(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertFirstPagedResourceParts(act);
  }

  @Test
  void testFactoryConvertsSingletonOneExpand() {
    createUriInfoSingletonOneExpand("Children");
    page = new JPAODataPage(original, 20, 10, "New",
        singletonList(new JPAODataPageExpandInfo("Children", "")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertNotNull(act.getOrderByOption());
    assertEquals(2, act.getUriResourceParts().size());
    assertInstanceOf(UriResourceSingleton.class, act.getUriResourceParts().get(0));
    assertFirstPagedNavigation(act);
  }

  @Test
  void testFactoryConvertsTwoExpandsFirstPaged() {
    final var expandFirstLevel = createUriInfoEntitySetOneExpand("Children");
    final var expandSecondLevel = createExpandItem("Children");
    final var expandOption = mock(ExpandOption.class);
    when(expandOption.getExpandItems()).thenReturn(singletonList(expandSecondLevel));
    when(expandOption.getKind()).thenReturn(SystemQueryOptionKind.EXPAND);
    when(expandFirstLevel.getExpandOption()).thenReturn(expandOption);

    page = new JPAODataPage(original, 20, 10, "New",
        List.of(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertFirstPagedResourceParts(act);

    assertFirstPagedNavigation(act);

    final var expand = act.getExpandOption();
    assertNotNull(expand);
    assertEquals(1, expand.getExpandItems().size());
  }

  @Test
  void testFactoryConvertsTwoExpandsLastPaged() {
    final var expandFirstLevel = createUriInfoEntitySetOneExpand("Children");
    final var expandSecondLevel = createExpandItem("Children");
    final var expandOption = mock(ExpandOption.class);
    when(expandOption.getExpandItems()).thenReturn(singletonList(expandSecondLevel));
    when(expandOption.getKind()).thenReturn(SystemQueryOptionKind.EXPAND);
    when(expandFirstLevel.getExpandOption()).thenReturn(expandOption);

    page = new JPAODataPage(original, 20, 10, "New",
        asList(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2"),
            new JPAODataPageExpandInfo("Children", "Eurostat/NUTS2/BE22")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertSecondPagedResourceParts(act);
    assertSecondPagedNavigation(act);

    assertInstanceOf(UriResourceNavigation.class, act.getUriResourceParts().get(2));
    assertEquals(20, act.getSkipOption().getValue());
    assertEquals(10, act.getTopOption().getValue());
  }

  @Test
  void testFactoryConvertsExpandLevels2FirstPaged() {
    // ...Es?$expand=Navigation1($levels=2) => ...Es(key)/Navigation1?$expand=Navigation1
    final var expandFirstLevel = createUriInfoEntitySetOneExpand("Children");
    final var levelsOption = mock(LevelsExpandOption.class);
    when(expandFirstLevel.getLevelsOption()).thenReturn(levelsOption);
    when(levelsOption.getValue()).thenReturn(2);

    page = new JPAODataPage(original, 20, 10, "New",
        List.of(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertFirstPagedResourceParts(act);
    assertFirstPagedNavigation(act);

    final var expand = act.getExpandOption();
    assertNotNull(expand);
    final var item = expand.getExpandItems().get(0);
    final var part = (UriResourceNavigation) item.getResourcePath().getUriResourceParts().get(0);
    assertEquals("Children", part.getProperty().getName());
    assertEquals(1, item.getLevelsOption().getValue());

  }

  @Test
  void testFactoryConvertsExpandLevels2SecondPaged() {
    // ...Es?$expand=Navigation1($levels=2) => ...Es(key)/Navigation1?$expand=Navigation1
    final var levelsOption = mock(LevelsExpandOption.class);
    final var expandFirstLevel = createUriInfoEntitySetOneExpand("Children");
    when(expandFirstLevel.getLevelsOption()).thenReturn(levelsOption);
    when(levelsOption.getValue()).thenReturn(2);

    page = new JPAODataPage(original, 20, 10, "New",
        asList(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2"),
            new JPAODataPageExpandInfo("Children", "Eurostat/NUTS2/BE22")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertSecondPagedResourceParts(act);
    assertSecondPagedNavigation(act);

    final var expand = act.getExpandOption();
    assertNull(expand);

  }

  @Test
  void testFactoryConvertsExpandLevelsMaxFirstPaged() {
    // ...Es?$expand=Navigation1($levels=max) => ...Es(key)/Navigation1?$expand=Navigation1($levels=max)
    final var levelsOption = mock(LevelsExpandOption.class);
    final var expandFirstLevel = createUriInfoEntitySetOneExpand("Children");
    when(expandFirstLevel.getLevelsOption()).thenReturn(levelsOption);
    when(levelsOption.getValue()).thenReturn(0);
    when(levelsOption.isMax()).thenReturn(true);

    page = new JPAODataPage(original, 20, 10, "New",
        List.of(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertFirstPagedResourceParts(act);
    assertFirstPagedNavigation(act);

    final var expand = act.getExpandOption();
    assertNotNull(expand);
    final var item = expand.getExpandItems().get(0);
    final var part = (UriResourceNavigation) item.getResourcePath().getUriResourceParts().get(0);
    assertEquals("Children", part.getProperty().getName());
    assertTrue(item.getLevelsOption().isMax());
  }

  @Test
  void testFactoryConvertsExpandFirstStar() {
    // ...Es?$expand=* => ...Es(key)/Navigation1
    final var expandItem = createUriInfoEntitySetOneExpand("Children");
    final UriInfoResource resourceInfo = mock(UriInfoResource.class);
    when(expandItem.getResourcePath()).thenReturn(resourceInfo);
    when(expandItem.isStar()).thenReturn(true);

    page = new JPAODataPage(original, 20, 10, "New",
        List.of(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertFirstPagedResourceParts(act);
    assertFirstPagedNavigation(act);
    assertNull(act.getExpandOption());
  }

  @Test
  void testFactoryConvertsExpandSecondStarFirstPaged() {
    // ...Es?$expand=Navigation1($expand=*) => ...Es(key)/Navigation1?$expand=*
    final var expandSecondLevel = createBaseUriInfoOneExpand("Children");
    final UriInfoResource resourceInfo = mock(UriInfoResource.class);
    when(expandSecondLevel.getResourcePath()).thenReturn(resourceInfo);
    final var expandFirstLevel = createUriInfoEntitySetOneExpand("Children");
    createSecondStar(expandSecondLevel, expandFirstLevel);

    page = new JPAODataPage(original, 20, 10, "New",
        List.of(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertFirstPagedResourceParts(act);
    assertFirstPagedNavigation(act);

    final var expand = act.getExpandOption();
    assertNotNull(expand);
    assertEquals(1, expand.getExpandItems().size());
    assertTrue(expand.getExpandItems().get(0).isStar());
  }

  @Test
  void testFactoryConvertsExpandSecondStarSecondPaged() {
    // ...Es?$expand=Navigation1($expand=*) => ...Es(key)/Navigation1?($expand=Navigation1_2)
    final var expandSecondLevel = createBaseUriInfoOneExpand("Children");
    final UriInfoResource resourceInfo = mock(UriInfoResource.class);
    when(expandSecondLevel.getResourcePath()).thenReturn(resourceInfo);

    final var expandFirstLevel = createUriInfoEntitySetOneExpand("Children");
    createSecondStar(expandSecondLevel, expandFirstLevel);

    page = new JPAODataPage(original, 20, 10, "New",
        asList(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2"),
            new JPAODataPageExpandInfo("Children", "Eurostat/NUTS2/BE22")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertSecondPagedResourceParts(act);
    assertSecondPagedNavigation(act);

    final var expand = act.getExpandOption();
    assertNull(expand);
  }

  @Test
  void testFactoryConvertsExpandFirstStarLevels2FirstPaged() {
    // ...Es?$expand=*($levels=2) => ...Es(key)/Navigation1?$expand=*
    final var expandItem = createUriInfoEntitySetOneExpand("Children");

    final UriInfoResource resourceInfo = mock(UriInfoResource.class);
    when(expandItem.getResourcePath()).thenReturn(resourceInfo);

    when(expandItem.isStar()).thenReturn(true);
    final var levelsOption = mock(LevelsExpandOption.class);
    when(expandItem.getLevelsOption()).thenReturn(levelsOption);
    when(levelsOption.getValue()).thenReturn(2);

    page = new JPAODataPage(original, 20, 10, "New",
        List.of(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertFirstPagedResourceParts(act);
    assertFirstPagedNavigation(act);
    final var expand = act.getExpandOption();
    assertNotNull(expand);
    assertEquals(1, expand.getExpandItems().size());
    assertNotNull(expand.getExpandItems().get(0).getLevelsOption());
    assertTrue(expand.getExpandItems().get(0).isStar());
    assertEquals(1, expand.getExpandItems().get(0).getLevelsOption().getValue());
  }

  @Test
  void testFactoryConvertsExpandFirstStarLevels2SecondPaged() {
    // ...Es?$expand=*($levels=2) => ...Es(key)/Navigation1(key1)/Navigation1_1
    final var expandItem = createUriInfoEntitySetOneExpand("Children");

    final UriInfoResource resourceInfo = mock(UriInfoResource.class);
    when(expandItem.getResourcePath()).thenReturn(resourceInfo);

    when(expandItem.isStar()).thenReturn(true);
    final var levelsOption = mock(LevelsExpandOption.class);
    when(expandItem.getLevelsOption()).thenReturn(levelsOption);
    when(levelsOption.getValue()).thenReturn(2);

    page = new JPAODataPage(original, 20, 10, "New",
        asList(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2"),
            new JPAODataPageExpandInfo("Children", "Eurostat/NUTS2/BE22")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertEquals(3, act.getUriResourceParts().size());
    final UriResourceEntitySet es = (UriResourceEntitySet) act.getUriResourceParts().get(0);
    final var keys = es.getKeyPredicates();
    assertEquals(3, keys.size());
    assertSecondPagedNavigation(act);

    final var expand = act.getExpandOption();
    assertNull(expand);
  }

  @Test
  void testFactoryConvertsExpandFirstStarLevelsMaxSecondPaged() {
    // ...Es?$expand=*($levels=max) => ...Es(key)/Navigation1(key1)/Navigation1_1?$expand=*($levels=max)
    final var expandItem = createUriInfoEntitySetOneExpand("Children");

    final UriInfoResource resourceInfo = mock(UriInfoResource.class);
    when(expandItem.getResourcePath()).thenReturn(resourceInfo);

    when(expandItem.isStar()).thenReturn(true);
    final var levelsOption = mock(LevelsExpandOption.class);
    when(expandItem.getLevelsOption()).thenReturn(levelsOption);
    when(levelsOption.isMax()).thenReturn(true);
    when(levelsOption.getValue()).thenReturn(0);

    page = new JPAODataPage(original, 20, 10, "New",
        asList(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2"),
            new JPAODataPageExpandInfo("Children", "Eurostat/NUTS2/BE22")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertEquals(3, act.getUriResourceParts().size());
    final UriResourceEntitySet es = (UriResourceEntitySet) act.getUriResourceParts().get(0);
    final var keys = es.getKeyPredicates();
    assertEquals(3, keys.size());
    assertSecondPagedNavigation(act);

    final var expand = act.getExpandOption();
    assertNotNull(expand);
    assertEquals(1, expand.getExpandItems().size());
    final var item = expand.getExpandItems().get(0);
    assertTrue(item.isStar());
    assertTrue(item.getLevelsOption().isMax());

  }

  @Test
  void testFactoryConvertsExpandStarViaComplexType() {
    // ...Es(key)/ComplexType?$expand=* => ...Es(key)/ComplexType/Navigation1

    final var expandItem = createBaseUriInfoOneExpand("Children");
    final var es = createEntitySet();
    final EdmEntityType edmType = createEntityType();
    final var complex = mock(UriResourceComplexProperty.class);
    final var complexType = mock(EdmComplexType.class);
    when(complex.getComplexType()).thenReturn(complexType);
    when(complex.getType()).thenReturn(complexType);
    when(complex.getKind()).thenReturn(UriResourceKind.complexProperty);
    when(complex.getSegmentValue()).thenReturn("Complex");
    when(complexType.getName()).thenReturn("Complex");
    addNavigationPropertiesToType(complexType, edmType);

    when(original.getUriResourceParts()).thenReturn(List.of(es, complex));

    final UriInfoResource resourceInfo = mock(UriInfoResource.class);
    when(expandItem.getResourcePath()).thenReturn(resourceInfo);
    when(expandItem.isStar()).thenReturn(true);

    page = new JPAODataPage(original, 20, 10, "New",
        asList(new JPAODataPageExpandInfo("Complex/Children", "Eurostat/NUTS1/BE2")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertEquals(3, act.getUriResourceParts().size());
    final UriResourceEntitySet actEs = (UriResourceEntitySet) act.getUriResourceParts().get(0);
    final var keys = actEs.getKeyPredicates();
    assertEquals(3, keys.size());
    assertTrue(act.getUriResourceParts().get(1) instanceof UriResourceComplexProperty);
    assertTrue(act.getUriResourceParts().get(2) instanceof UriResourceNavigation);
  }

  @Test
  void testFactoryConvertsExpandStarViaComplexWithIntermediateType() {
    // ...Es(key)/ComplexType1?$expand=* => ...Es(key)/ComplexType1/ComplexType2/Navigation1

    final var expandItem = createBaseUriInfoOneExpand("Children");
    final var es = createEntitySet();
    final EdmEntityType edmType = createEntityType();
    final var complex = mock(UriResourceComplexProperty.class);
    final var complexType = mock(EdmComplexType.class);
    when(complex.getComplexType()).thenReturn(complexType);
    when(complex.getType()).thenReturn(complexType);
    when(complex.getSegmentValue()).thenReturn("Complex");
    when(complex.getKind()).thenReturn(UriResourceKind.complexProperty);
    when(complexType.getNavigationPropertyNames()).thenReturn(Collections.emptyList());
    when(complexType.getPropertyNames()).thenReturn(List.of("Left", "Right"));

    final var left = createNavigationViaComplexType(edmType, "Left");
    when(complexType.getProperty("Left")).thenReturn(left);
    final var right = createNavigationViaComplexType(edmType, "Right");
    when(complexType.getProperty("Right")).thenReturn(right);

    when(original.getUriResourceParts()).thenReturn(List.of(es, complex));

    final UriInfoResource resourceInfo = mock(UriInfoResource.class);
    when(expandItem.getResourcePath()).thenReturn(resourceInfo);
    when(expandItem.isStar()).thenReturn(true);

    page = new JPAODataPage(original, 20, 10, "New",
        asList(new JPAODataPageExpandInfo("Complex/Left/Children", "Eurostat/NUTS1/BE2")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertEquals(4, act.getUriResourceParts().size());
    final UriResourceEntitySet actEs = (UriResourceEntitySet) act.getUriResourceParts().get(0);
    final var keys = actEs.getKeyPredicates();
    assertEquals(3, keys.size());
    assertTrue(act.getUriResourceParts().get(1) instanceof UriResourceComplexProperty);
    assertTrue(act.getUriResourceParts().get(2) instanceof UriResourceComplexProperty);
    assertTrue(act.getUriResourceParts().get(3) instanceof UriResourceNavigation);

  }

  @Test
  void testFactoryConvertsExpandStarViaToOneWithIntermediateType() {
    // ...Es(key)/Navigation1?$expand=* => ...Es(key)/Navigation1/Navigation2

    final var expandItem = createBaseUriInfoOneExpand("Children");
    final var es = createEntitySet();
    final EdmEntityType edmType = createEntityType();
    final var navigation = createUriNavigationProperty("Parent", edmType);

    final UriInfoResource resourceInfo = mock(UriInfoResource.class);
    when(expandItem.getResourcePath()).thenReturn(resourceInfo);

    when(expandItem.isStar()).thenReturn(true);
    final var levelsOption = mock(LevelsExpandOption.class);
    when(expandItem.getLevelsOption()).thenReturn(levelsOption);
    when(levelsOption.getValue()).thenReturn(2);
    when(original.getUriResourceParts()).thenReturn(List.of(es, navigation));

    page = new JPAODataPage(original, 20, 10, "New",
        asList(new JPAODataPageExpandInfo("Children", "Eurostat/NUTS1/BE2")));
    cut = new JPAUriInfoFactory(page);
    final var act = cut.build();

    assertEquals(3, act.getUriResourceParts().size());
    final UriResourceEntitySet actEs = (UriResourceEntitySet) act.getUriResourceParts().get(0);
    final var keys = actEs.getKeyPredicates();
    assertEquals(3, keys.size());
    assertTrue(act.getUriResourceParts().get(1) instanceof UriResourceNavigation);
    assertTrue(act.getUriResourceParts().get(2) instanceof UriResourceNavigation);

  }

  private EdmProperty createNavigationViaComplexType(final EdmEntityType edmType, final String name) {
    final var property = mock(EdmProperty.class);
    final var propertyType = mock(EdmStructuredType.class);
    when(property.getType()).thenReturn(propertyType);
    when(property.getName()).thenReturn(name);
    addNavigationPropertiesToType(propertyType, edmType);
    return property;
  }

  private static void createSecondStar(final ExpandItem expandSecondLevel, final ExpandItem expandFirstLevel) {
    final var expandOption = mock(ExpandOption.class);
    when(expandOption.getExpandItems()).thenReturn(singletonList(expandSecondLevel));
    when(expandOption.getKind()).thenReturn(SystemQueryOptionKind.EXPAND);
    when(expandFirstLevel.getExpandOption()).thenReturn(expandOption);
    when(expandSecondLevel.isStar()).thenReturn(true);
  }

  private void assertFirstPagedNavigation(final JPAUriInfoResource act) {
    assertInstanceOf(UriResourceNavigation.class, act.getUriResourceParts().get(1));
    final UriResourceNavigation navigation = (UriResourceNavigation) act.getUriResourceParts().get(1);
    assertEquals(20, act.getSkipOption().getValue());
    assertEquals(10, act.getTopOption().getValue());
    assertFalse(navigation.isCollection());
  }

  private void assertFirstPagedResourceParts(final JPAUriInfoResource act) {
    assertNotNull(act.getOrderByOption());
    assertEquals(2, act.getUriResourceParts().size());
    final UriResourceEntitySet es = (UriResourceEntitySet) act.getUriResourceParts().get(0);
    final var keys = es.getKeyPredicates();
    assertEquals(3, keys.size());
  }

  private SkipTokenOption createSkipToken() {
    final SkipTokenOption skipToken = mock(SkipTokenOption.class);
    when(original.getSkipTokenOption()).thenReturn(skipToken);
    when(skipToken.getValue()).thenReturn("Old");
    when(skipToken.getKind()).thenReturn(SystemQueryOptionKind.SKIPTOKEN);
    when(original.getSystemQueryOptions()).thenReturn(singletonList(skipToken));
    return skipToken;
  }

  private ExpandItem createUriInfoEntitySetOneExpand(final String name) {

    final var item = createBaseUriInfoOneExpand(name);
    final UriResourceEntitySet es = createEntitySet();
    when(original.getUriResourceParts()).thenReturn(singletonList(es));
    return item;
  }

  private UriResourceEntitySet createEntitySet() {
    final UriResourceEntitySet es = mock(UriResourceEntitySet.class);
    final EdmEntitySet edmEs = mock(EdmEntitySet.class);
    final EdmEntityType edmType = createEntityType();
    final var keyReferences = createKeyReferences();
    when(es.getKind()).thenReturn(UriResourceKind.entitySet);
    when(es.getEntitySet()).thenReturn(edmEs);
    when(es.getEntityType()).thenReturn(edmType);
    when(es.getType()).thenReturn(edmType);
    when(edmType.getKeyPropertyRefs()).thenReturn(keyReferences);
    return es;
  }

  private void createUriInfoSingletonOneExpand(final String name) {
    createBaseUriInfoOneExpand(name);

    final var singleton = mock(UriResourceSingleton.class);
    final var edmSingleton = mock(EdmSingleton.class);
    final var edmType = mock(EdmEntityType.class);
    final var keyReferences = createKeyReferences();
    when(original.getUriResourceParts()).thenReturn(singletonList(singleton));
    when(singleton.getKind()).thenReturn(UriResourceKind.singleton);
    when(singleton.getSingleton()).thenReturn(edmSingleton);

    when(singleton.getEntityType()).thenReturn(edmType);
    when(edmType.getKeyPropertyRefs()).thenReturn(keyReferences);

  }

  private ExpandItem createBaseUriInfoOneExpand(final String name) {
    final var skipToken = createSkipToken();
    final TopOption topOption = mock(TopOption.class);
    final SkipOption skipOption = mock(SkipOption.class);
    final FilterOption filterOption = mock(FilterOption.class);
    final FormatOption formatOption = mock(FormatOption.class);
    final ExpandOption expandOption = mock(ExpandOption.class);
    final var item = createExpandItem(name);

    when(topOption.getKind()).thenReturn(SystemQueryOptionKind.TOP);
    when(skipOption.getKind()).thenReturn(SystemQueryOptionKind.SKIP);
    when(filterOption.getKind()).thenReturn(SystemQueryOptionKind.FILTER);
    when(formatOption.getKind()).thenReturn(SystemQueryOptionKind.FORMAT);
    when(expandOption.getKind()).thenReturn(SystemQueryOptionKind.EXPAND);
    when(original.getKind()).thenReturn(UriInfoKind.resource);
    when(original.getSystemQueryOptions()).thenReturn(Arrays.asList(skipToken, topOption, skipOption, filterOption,
        formatOption, expandOption));
    when(original.getExpandOption()).thenReturn(expandOption);
    when(original.getTopOption()).thenReturn(topOption);
    when(original.getSkipOption()).thenReturn(skipOption);
    when(original.getFilterOption()).thenReturn(filterOption);
    when(original.getFormatOption()).thenReturn(formatOption);

    when(expandOption.getExpandItems()).thenReturn(List.of(item));
    return item;
  }

  private List<EdmKeyPropertyRef> createKeyReferences() {
    final var codePublisher = mock(EdmKeyPropertyRef.class);
    final var codeId = mock(EdmKeyPropertyRef.class);
    final var divisionCode = mock(EdmKeyPropertyRef.class);
    when(codePublisher.getName()).thenReturn("CodePublisher");
    when(codeId.getName()).thenReturn("CodeId");
    when(divisionCode.getName()).thenReturn("DivisionCode");
    return Arrays.asList(codePublisher, codeId, divisionCode);
  }

  private ExpandItem createExpandItem(final String name) {
    final ExpandItem item = mock(ExpandItem.class);
    final UriInfoResource resourceInfo = mock(UriInfoResource.class);
    final TopOption topOption = mock(TopOption.class);
    final SkipOption skipOption = mock(SkipOption.class);
    final FilterOption filterOption = mock(FilterOption.class);
    final SelectOption selectOption = mock(SelectOption.class);
    final SearchOption searchOption = mock(SearchOption.class);
    final OrderByOption orderByOption = mock(OrderByOption.class);

    when(topOption.getKind()).thenReturn(SystemQueryOptionKind.TOP);
    when(skipOption.getKind()).thenReturn(SystemQueryOptionKind.SKIP);
    when(filterOption.getKind()).thenReturn(SystemQueryOptionKind.FILTER);
    when(selectOption.getKind()).thenReturn(SystemQueryOptionKind.SELECT);
    when(searchOption.getKind()).thenReturn(SystemQueryOptionKind.SEARCH);
    when(orderByOption.getKind()).thenReturn(SystemQueryOptionKind.ORDERBY);

    when(item.getTopOption()).thenReturn(topOption);
    when(item.getSkipOption()).thenReturn(skipOption);
    when(item.getFilterOption()).thenReturn(filterOption);
    when(item.getSelectOption()).thenReturn(selectOption);
    when(item.getSearchOption()).thenReturn(searchOption);
    when(item.getOrderByOption()).thenReturn(orderByOption);
    when(item.getResourcePath()).thenReturn(resourceInfo);

    final EdmEntityType edmType = createEntityType();
    final UriResourceNavigation navigation = createUriNavigationProperty(name, edmType);
    when(resourceInfo.getUriResourceParts()).thenReturn(List.of(navigation));

    return item;
  }

  private EdmEntityType createEntityType() {
    final EdmEntityType edmType = mock(EdmEntityType.class);
    final var keyReferences = createKeyReferences();
    when(edmType.getKeyPropertyRefs()).thenReturn(keyReferences);
    addNavigationPropertiesToType(edmType, edmType);
    when(edmType.getPropertyNames()).thenReturn(List.of());
    return edmType;
  }

  private static UriResourceNavigation createUriNavigationProperty(final String name, final EdmEntityType edmType) {
    final UriResourceNavigation navigation = mock(UriResourceNavigation.class);
    final EdmNavigationProperty property = createNavigationProperty(name, edmType);
    when(navigation.getSegmentValue()).thenReturn(name);
    when(navigation.getProperty()).thenReturn(property);
    when(navigation.getType()).thenReturn(edmType);
    when(navigation.getKind()).thenReturn(UriResourceKind.navigationProperty);
    return navigation;
  }

  private static EdmNavigationProperty createNavigationProperty(final String name, final EdmEntityType edmType) {
    final EdmNavigationProperty property = mock(EdmNavigationProperty.class);
    when(property.getName()).thenReturn(name);
    when(property.getType()).thenReturn(edmType);
    return property;
  }

  private void assertSecondPagedNavigation(final JPAUriInfoResource act) {
    final var navigation = (UriResourceNavigation) act.getUriResourceParts().get(1);
    final var navigationKeys = navigation.getKeyPredicates();
    assertEquals(3, navigationKeys.size());
    assertFalse(navigation.isCollection());
  }

  private void assertSecondPagedResourceParts(final JPAUriInfoResource act) {
    assertNotNull(act.getOrderByOption());
    assertEquals(3, act.getUriResourceParts().size());
    final UriResourceEntitySet es = (UriResourceEntitySet) act.getUriResourceParts().get(0);
    final var keys = es.getKeyPredicates();
    assertEquals(3, keys.size());
  }

  private void addNavigationPropertiesToType(final EdmStructuredType source, final EdmEntityType target) {
    when(source.getNavigationPropertyNames()).thenReturn(Arrays.asList("Children", "Parent"));
    final EdmNavigationProperty children = createNavigationProperty("Children", target);
    final EdmNavigationProperty parent = createNavigationProperty("Parent", target);
    when(source.getNavigationProperty("Children")).thenReturn(children);
    when(source.getNavigationProperty("Parent")).thenReturn(parent);
  }

}
