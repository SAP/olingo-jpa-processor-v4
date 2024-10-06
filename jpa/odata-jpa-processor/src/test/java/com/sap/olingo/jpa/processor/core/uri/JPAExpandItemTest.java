package com.sap.olingo.jpa.processor.core.uri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class JPAExpandItemTest {
  private JPAExpandItem cut;
  private LevelsExpandOption levels;
  private ExpandItem item;
  private EdmType typeFilter;
  private ApplyOption apply;
  private TopOption top;
  private SkipOption skip;
  private CountOption count;

  @BeforeEach
  void setup() {
    levels = mock(LevelsExpandOption.class);
    item = mock(ExpandItem.class);
    typeFilter = mock(EdmType.class);
    apply = mock(ApplyOption.class);
    top = mock(TopOption.class);
    skip = mock(SkipOption.class);
    count = mock(CountOption.class);

    when(item.hasCountPath()).thenReturn(true);
    when(item.isRef()).thenReturn(true);
    when(item.getStartTypeFilter()).thenReturn(typeFilter);
    when(item.getApplyOption()).thenReturn(apply);
    when(item.getTopOption()).thenReturn(top);
    when(item.getSkipOption()).thenReturn(skip);
    when(item.getCountOption()).thenReturn(count);
  }

  @TestFactory
  Collection<DynamicTest> dynamicTestsNoItemProvided() {
    cut = new JPAExpandItem(levels);
    return Arrays.asList(
        dynamicTest("isRef returns false", () -> assertFalse(cut.isRef())),
        dynamicTest("hasCountPath returns false", () -> assertFalse(cut.hasCountPath())),
        dynamicTest("getStartTypeFilter returns null", () -> assertNull(cut.getStartTypeFilter())),
        dynamicTest("getApplyOption returns null", () -> assertNull(cut.getApplyOption())),
        dynamicTest("getTopOption returns null", () -> assertNull(cut.getTopOption())),
        dynamicTest("getSkipOption returns null", () -> assertNull(cut.getSkipOption())),
        dynamicTest("getCountOption returns null", () -> assertNull(cut.getCountOption())));
  }

  @TestFactory
  Collection<DynamicTest> dynamicTestsItemProvided() {
    cut = new JPAExpandItem(item, levels);
    return Arrays.asList(
        dynamicTest("isRef returns original value", () -> assertTrue(cut.isRef())),
        dynamicTest("hasCountPath returns original value", () -> assertTrue(cut.hasCountPath())),
        dynamicTest("getStartTypeFilter returns original value", () -> assertEquals(typeFilter, cut
            .getStartTypeFilter())),
        dynamicTest("getApplyOption returns original value", () -> assertEquals(apply, cut.getApplyOption())),
        dynamicTest("getTopOption returns original value", () -> assertEquals(top, cut.getTopOption())),
        dynamicTest("getSkipOption returns original value", () -> assertEquals(skip, cut.getSkipOption())),
        dynamicTest("getCountOption returns original value", () -> assertEquals(count, cut.getCountOption())));
  }
}
