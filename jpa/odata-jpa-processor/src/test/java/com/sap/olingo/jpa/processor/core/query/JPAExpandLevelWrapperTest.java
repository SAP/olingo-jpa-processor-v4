package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataApplicationException;
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
    item = mock(ExpandItem.class);
    itemList = Arrays.asList(item);

    when(option.getExpandItems()).thenReturn(itemList);
    when(item.getLevelsOption()).thenReturn(levelOption);
    when(item.getResourcePath()).thenReturn(null);
    when(item.getFilterOption()).thenReturn(filterOption);
    when(item.getCountOption()).thenReturn(countOption);
    when(item.getOrderByOption()).thenReturn(orderByOption);
    when(item.getSearchOption()).thenReturn(searchOption);
    when(item.getSelectOption()).thenReturn(selectOption);
    when(item.getSkipOption()).thenReturn(skipOption);
    when(item.getTopOption()).thenReturn(topOption);
    cut = new JPAExpandLevelWrapper(sd, option);
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
        dynamicTest("CustomQueryOptions empty", () -> assertEquals(0, cut.getCustomQueryOptions().size())));
  }

  @Test
  void checkFilterOption() {
    assertEquals(filterOption, cut.getFilterOption());
  }

  @Test
  void checkCountOption() {
    assertEquals(countOption, cut.getCountOption());
  }

  @Test
  void checkOrderByOption() {
    assertEquals(orderByOption, cut.getOrderByOption());
  }

  @Test
  void checkSearchOption() {
    assertEquals(searchOption, cut.getSearchOption());
  }

  @Test
  void checkSelectOption() {
    assertEquals(selectOption, cut.getSelectOption());
  }

  @Test
  void checkSkipOption() {
    assertEquals(skipOption, cut.getSkipOption());
  }

  @Test
  void checkTopOption() {
    assertEquals(topOption, cut.getTopOption());
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
    assertEquals(0, actLevelOptions.getValue());
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
}
