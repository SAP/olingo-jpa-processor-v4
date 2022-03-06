package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

class JPACollectionExpandWrapperTest {
  private JPACollectionExpandWrapper cut;
  private JPAEntityType jpaEntityType;
  private UriInfoResource uriInfo;
  private FilterOption filterOptions;
  private CountOption countOptions;
  private SelectOption selectOptions;
  private List<UriResource> parts;

  @BeforeEach
  void setup() {
    jpaEntityType = mock(JPAEntityType.class);
    uriInfo = mock(UriInfoResource.class);
    filterOptions = mock(FilterOption.class);
    countOptions = mock(CountOption.class);
    selectOptions = mock(SelectOption.class);
    cut = new JPACollectionExpandWrapper(jpaEntityType, uriInfo);

    when(uriInfo.getFilterOption()).thenReturn(filterOptions);
    when(uriInfo.getCountOption()).thenReturn(countOptions);
    when(uriInfo.getSelectOption()).thenReturn(selectOptions);
    when(uriInfo.getUriResourceParts()).thenReturn(parts);
  }

  @Test
  void testGetCustomQueryOptionsEmpty() {
    assertTrue(cut.getCustomQueryOptions().isEmpty());
  }

  @Test
  void testGetEntityType() {
    assertEquals(jpaEntityType, cut.getEntityType());
  }

  @Test
  void testGetCountOption() {
    assertEquals(countOptions, cut.getCountOption());
  }

  @Test
  void testGetFilterOptions() {
    assertEquals(filterOptions, cut.getFilterOption());
  }

  @Test
  void testGetSelectOptions() {
    assertEquals(selectOptions, cut.getSelectOption());
  }

  @Test
  void testGetUriResourceParts() {
    assertEquals(parts, cut.getUriResourceParts());
  }

  @Test
  void testGetValueForAliasReturnsNull() {
    assertNull(cut.getValueForAlias("Test"));
  }

  @TestFactory
  Stream<DynamicTest> testMethodsReturninfNull() {
    return Stream.of("getExpandOption", "getFormatOption", "getDeltaTokenOption", "getOrderByOption", "getSearchOption",
        "getSkipOption", "getSkipTokenOption", "getTopOption", "getApplyOption", "getIdOption")
        .map(method -> dynamicTest(method, () -> assertNull(executeMethod(method))));
  }

  private Object executeMethod(final String methodName) {
    final Class<?> clazz = cut.getClass();
    try {
      final Method method = clazz.getMethod(methodName);
      return method.invoke(cut);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      fail();
    }
    return "Test";
  }
}
