package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;

class JPAExpandLevelWrapperTest {

  private static final String TEXT = "This is a text";
  private static final String NAME = "Name";
  private JPAExpandLevelWrapper cut;
  private JPAServiceDocument sd;
  private ExpandOption option;
  private LevelsExpandOption levelOption;
  private ExpandItem item;
  private FilterOption filterOption;
  private CountOption countOption;
  private OrderByOption orderByOption;
  private SearchOption searchOption;
  private SelectOption selectOption;
  private SkipOption skipOption;
  private TopOption topOption;
  private List<ExpandItem> itemList;
  private ExpandOption expandOption;

  // return item.getResourcePath() != null ? item.getResourcePath().getUriResourceParts() : Collections.emptyList();
  @BeforeEach
  void setup() throws ODataApplicationException {

    sd = mock(JPAServiceDocument.class);
    option = mock(ExpandOption.class);
    levelOption = mock(LevelsExpandOption.class);
    filterOption = mock(FilterOption.class);
    countOption = mock(CountOption.class);
    orderByOption = mock(OrderByOption.class);
    searchOption = mock(SearchOption.class);
    selectOption = mock(SelectOption.class);
    skipOption = mock(SkipOption.class);
    topOption = mock(TopOption.class);
    expandOption = mock(ExpandOption.class);
    itemList = new ArrayList<>();
    item = buildItem(null);
    itemList.add(item);

    cut = new JPAExpandLevelWrapper(sd, option, item);
  }

  private ExpandItem buildItem(final UriInfoResource infoResource) {
    final ExpandItem item = mock(ExpandItem.class);
    when(option.getExpandItems()).thenReturn(itemList);
    when(item.getLevelsOption()).thenReturn(levelOption);
    when(item.getResourcePath()).thenReturn(infoResource);
    when(item.getFilterOption()).thenReturn(filterOption);
    when(item.getCountOption()).thenReturn(countOption);
    when(item.getOrderByOption()).thenReturn(orderByOption);
    when(item.getSearchOption()).thenReturn(searchOption);
    when(item.getSelectOption()).thenReturn(selectOption);
    when(item.getSkipOption()).thenReturn(skipOption);
    when(item.getTopOption()).thenReturn(topOption);
    when(item.getExpandOption()).thenReturn(expandOption);
    return item;
  }

  @Test
  void checkConstructor() {
    assertNotNull(cut);
  }

  @TestFactory
  Iterable<DynamicTest> checkReturnsNull() {
    return Arrays.asList(
        dynamicTest("FormatOption returns null", () -> assertNull(cut.getFormatOption())),
        dynamicTest("IdOption returns null", () -> assertNull(cut.getIdOption())),
        dynamicTest("ValueForAlias returns null", () -> assertNull(cut.getValueForAlias(null))),
        dynamicTest("ApplyOption returns null", () -> assertNull(cut.getApplyOption())),
        dynamicTest("SkipTokenOption returns null", () -> assertNull(cut.getSkipTokenOption())),
        dynamicTest("DeltaTokenOption returns null", () -> assertNull(cut.getDeltaTokenOption())),
        dynamicTest("CustomQueryOptions empty", () -> assertEquals(0, cut.getCustomQueryOptions().size())),
        dynamicTest("ExpandOption returns null if level is < 2", () -> {
          when(levelOption.getValue()).thenReturn(1);
          assertNull(cut.getExpandOption());
        }));
  }

  @TestFactory
  Iterable<DynamicTest> checkReturnsInputProperty() {
    return Arrays.asList(
        dynamicTest("FilterOption returned", () -> assertEquals(filterOption, cut.getFilterOption())),
        dynamicTest("CountOption returned", () -> assertEquals(countOption, cut.getCountOption())),
        dynamicTest("OrderByOption returned", () -> assertEquals(orderByOption, cut.getOrderByOption())),
        dynamicTest("SearchOption returned", () -> assertEquals(searchOption, cut.getSearchOption())),
        dynamicTest("SelectOption returned", () -> assertEquals(selectOption, cut.getSelectOption())),
        dynamicTest("SkipOption returned", () -> assertEquals(skipOption, cut.getSkipOption())),
        dynamicTest("TopOption returned", () -> assertEquals(topOption, cut.getTopOption())));
  }

  @Test
  void checkLevelOption1() throws ODataApplicationException {
    when(levelOption.getValue()).thenReturn(1);
    assertNull(cut.getExpandOption());
  }

  @Test
  void checkLevelOption2() throws ODataApplicationException {
    when(levelOption.getValue()).thenReturn(2);
    final ExpandOption act = cut.getExpandOption();
    assertNotNull(act);
    assertEquals(1, act.getExpandItems().size());
  }

  @Test
  void checkExpandOptionProperties() throws ODataApplicationException {
    when(levelOption.getValue()).thenReturn(2);
    when(option.getKind()).thenReturn(SystemQueryOptionKind.APPLY);
    when(option.getName()).thenReturn(NAME);
    when(option.getText()).thenReturn(TEXT);
    final ExpandOption act = cut.getExpandOption();
    assertEquals(SystemQueryOptionKind.APPLY, act.getKind());
    assertEquals(NAME, act.getName());
    assertEquals(TEXT, act.getText());
  }

  @Test
  void checkExpandItemProperties() throws ODataApplicationException {
    when(levelOption.getValue()).thenReturn(2);

    final ExpandOption itemExpandOption = mock(ExpandOption.class);
    final ExpandItem itemSecondLevel = mock(ExpandItem.class);
    final LevelsExpandOption secondLevelOptions = mock(LevelsExpandOption.class);
    final EdmType startTypeFilter = mock(EdmType.class);
    when(item.getExpandOption()).thenReturn(itemExpandOption);
    when(item.getStartTypeFilter()).thenReturn(startTypeFilter);
    when(itemExpandOption.getExpandItems()).thenReturn(Arrays.asList(itemSecondLevel));
    when(itemSecondLevel.getLevelsOption()).thenReturn(secondLevelOptions);
    when(secondLevelOptions.getValue()).thenReturn(0);
    when(secondLevelOptions.isMax()).thenReturn(false);
    final ExpandItem act = cut.getExpandOption().getExpandItems().get(0);

    assertNull(act.getSearchOption());
    assertNull(act.getApplyOption());
    assertFalse(act.isRef());
    assertFalse(act.hasCountPath());
    assertNotNull(act.getExpandOption());
    assertEquals(startTypeFilter, act.getStartTypeFilter());
    final LevelsExpandOption actLevelOptions = act.getExpandOption().getExpandItems().get(0).getLevelsOption();
    assertEquals(1, actLevelOptions.getValue());
    assertFalse(actLevelOptions.isMax());

  }

  @Test
  void checkLevelsExpandOptionProperties() throws ODataApplicationException {
    when(levelOption.getValue()).thenReturn(2);
    when(levelOption.isMax()).thenReturn(true);
    final LevelsExpandOption act = cut.getExpandOption().getExpandItems().get(0).getLevelsOption();

    assertEquals(1, act.getValue());
    assertTrue(act.isMax());
  }

  @Test
  void checkExpandOptionContainsInnerExpandOption() {
    when(levelOption.getValue()).thenReturn(2);

    final ExpandOption act = cut.getExpandOption().getExpandItems().get(0).getExpandOption();
    assertNotNull(act.getExpandItems().get(0).getExpandOption());
  }

  @Test
  void checkEntityTypeViaConstructor() throws ODataApplicationException {
    final JPAEntityType et = mock(JPAEntityType.class);
    cut = new JPAExpandLevelWrapper(option, et, null, item);

    when(levelOption.getValue()).thenReturn(2);
    when(levelOption.isMax()).thenReturn(false);
    final LevelsExpandOption act = cut.getExpandOption().getExpandItems().get(0).getLevelsOption();

    assertEquals(1, act.getValue());
    assertFalse(act.isMax());
    assertEquals(et, cut.getEntityType());
  }

  @Test
  void checkTwoLevelItems() throws ODataApplicationException {
    final UriInfoResource infoResource = mock(UriInfoResource.class);
    final UriResource resource = mock(UriResource.class);
    final List<UriResource> resourceList = Collections.singletonList(resource);
    when(infoResource.getUriResourceParts()).thenReturn(resourceList);
    final ExpandItem secondItem = buildItem(infoResource);
    itemList.add(secondItem);
    final JPAEntityType et = mock(JPAEntityType.class);
    when(levelOption.getValue()).thenReturn(2);
    when(levelOption.isMax()).thenReturn(false);
    cut = new JPAExpandLevelWrapper(option, et, null, secondItem);

    final List<ExpandItem> act = cut.getExpandOption().getExpandItems();
    assertEquals(1, act.size());
    UriInfoResource resourcePath = act.get(0).getResourcePath();
    assertEquals(resourceList, resourcePath.getUriResourceParts());

    cut = new JPAExpandLevelWrapper(option, et, null, item);
    final List<ExpandItem> act2 = cut.getExpandOption().getExpandItems();
    assertEquals(1, act2.size());
    resourcePath = act2.get(0).getResourcePath();
    assertEquals(Collections.emptyList(), resourcePath.getUriResourceParts());

  }
}
