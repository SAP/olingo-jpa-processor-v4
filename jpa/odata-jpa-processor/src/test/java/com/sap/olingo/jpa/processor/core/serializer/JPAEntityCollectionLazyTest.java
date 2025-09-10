package com.sap.olingo.jpa.processor.core.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.persistence.Tuple;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPARowConverter;

class JPAEntityCollectionLazyTest {

  private JPAEntityCollectionLazy cut;
  private JPAExpandResult result;
  private JPARowConverter converter;
  private JPAEntityType jpaEntity;
  private List<Tuple> resultItems;
  private Tuple resultItem;

  @BeforeEach
  void setup() {
    resultItems = new ArrayList<>();
    resultItem = mock(Tuple.class);
    result = mock(JPAExpandResult.class);
    converter = mock(JPARowConverter.class);
    jpaEntity = mock(JPAEntityType.class);
    resultItems.add(resultItem);
    when(result.getEntityType()).thenReturn(jpaEntity);
    when(result.getResult(JPAExpandResult.ROOT_RESULT_KEY)).thenReturn(resultItems);

    cut = new JPAEntityCollectionLazy(result, converter);
  }

  @Test
  void testHashCodeReturnsValue() {
    cut.setCount(100);
    assertNotEquals(0, cut.hashCode());
  }

  @Test
  void testEqualsTrueForSameInstance() {
    cut.setCount(100);
    assertEquals(cut, cut);
  }

  @Test
  void testGetEntitiesReturnsEmptyList() {
    assertEquals(0, cut.getEntities().size());
  }

  @Test
  void testGetFirstResultThrowsExceptionIfConverterThrowsException() throws ODataApplicationException {
    when(converter.convertRow(eq(jpaEntity), any(), any(), any(), eq(result))).thenThrow(
        ODataApplicationException.class);
    final var act = assertThrows(IllegalStateException.class, () -> cut.getFirstResult());
    assertTrue(act.getCause() instanceof ODataApplicationException);
  }

  @Test
  void testIteratorThrowsExceptionOnTooManyNext() throws ODataApplicationException {
    final Entity odataEntity = mock(Entity.class);
    when(converter.convertRow(eq(jpaEntity), any(), any(), any(), eq(result)))
        .thenReturn(odataEntity);

    final var iterator = cut.iterator();
    assertEquals(odataEntity, iterator.next());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

  @Test
  void testIteratorReThrowsConverterException() throws ODataApplicationException {
    final Entity odataEntity = mock(Entity.class);
    final var secondItem = mock(Tuple.class);
    resultItems.add(secondItem);
    when(converter.convertRow(eq(jpaEntity), eq(resultItem), any(), any(), eq(result)))
        .thenReturn(odataEntity);
    when(converter.convertRow(eq(jpaEntity), eq(secondItem), any(), any(), eq(result)))
        .thenThrow(ODataApplicationException.class);
    final var iterator = cut.iterator();
    assertEquals(odataEntity, iterator.next());
    assertThrows(IllegalStateException.class, iterator::next);
  }
}
