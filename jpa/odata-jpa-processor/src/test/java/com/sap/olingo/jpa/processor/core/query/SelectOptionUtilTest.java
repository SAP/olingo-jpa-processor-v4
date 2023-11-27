package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Stream;

import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

class SelectOptionUtilTest {
  private JPAStructuredType jpaEntity;
  private SelectItem sItem;
  private UriInfoResource uriInfo;

  @TestFactory
  Stream<DynamicTest> testSelectAllTrue() {

    final SelectOption selectNull = mock(SelectOption.class);
    when(selectNull.getSelectItems()).thenReturn(null);

    final SelectOption selectEmpty = mock(SelectOption.class);
    when(selectEmpty.getSelectItems()).thenReturn(Collections.emptyList());

    final SelectOption selectStar = mock(SelectOption.class);
    final SelectItem starItem = mock(SelectItem.class);
    when(selectStar.getSelectItems()).thenReturn(Collections.singletonList(starItem));
    when(starItem.isStar()).thenReturn(true);

    return Stream.of(
        dynamicTest("Empty Items", () -> assertTrue(SelectOptionUtil.selectAll(selectStar))),
        dynamicTest("Empty Items", () -> assertTrue(SelectOptionUtil.selectAll(selectEmpty))),
        dynamicTest("Empty Items", () -> assertTrue(SelectOptionUtil.selectAll(selectNull))),
        dynamicTest("Empty Items", () -> assertTrue(SelectOptionUtil.selectAll(null))));
  }

  @Test
  void testSelectAllFalse() {
    final SelectOption select = mock(SelectOption.class);
    final SelectItem starItem = mock(SelectItem.class);
    when(select.getSelectItems()).thenReturn(Collections.singletonList(starItem));
    when(starItem.isStar()).thenReturn(false);

    assertFalse(SelectOptionUtil.selectAll(select));
  }

  @Test
  void testThrowsBadRequestPathNotFound() throws ODataJPAModelException {
    jpaEntity = mock(JPAStructuredType.class);
    when(jpaEntity.getPath(anyString())).thenReturn(null);

    sItem = mock(SelectItem.class);
    uriInfo = mock(UriInfoResource.class);
    when(sItem.getResourcePath()).thenReturn(uriInfo);
    when(uriInfo.getUriResourceParts()).thenReturn(Collections.emptyList());

    final ODataJPAQueryException act = assertThrows(ODataJPAQueryException.class, () -> SelectOptionUtil
        .selectItemAsPath(jpaEntity, "", sItem));
    assertEquals(400, act.getStatusCode());
  }

  @Test
  void testThrowsInternalServerPathThrowsException() throws ODataJPAModelException {
    jpaEntity = mock(JPAStructuredType.class);
    when(jpaEntity.getPath(anyString())).thenThrow(ODataJPAModelException.class);

    sItem = mock(SelectItem.class);
    uriInfo = mock(UriInfoResource.class);
    when(sItem.getResourcePath()).thenReturn(uriInfo);
    when(uriInfo.getUriResourceParts()).thenReturn(Collections.emptyList());

    final ODataJPAQueryException act = assertThrows(ODataJPAQueryException.class, () -> SelectOptionUtil
        .selectItemAsPath(jpaEntity, "", sItem));
    assertEquals(500, act.getStatusCode());
  }

}
