package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.processor.cb.ProcessorSelection;
import com.sap.olingo.jpa.processor.core.testmodel.DateTimeConverter;

class TupleImplTest {
  private static final String SECOND_VALUE = "Second";
  private static final String THIRD_VALUE = "Third";
  private static final String FIRST_VALUE = "First";
  private static final String TIME_VALUE = "Timestamp";
  private Tuple cut;
  private final Object[] values = { "Hello", "World", 3, Timestamp.valueOf("2019-01-25 14:00:25") };
  private Map<String, Integer> selectionIndex;
  private List<Entry<String, JPAAttribute>> selPath;

  @BeforeEach
  void setup() {

    selPath = new ArrayList<>(3);
    selectionIndex = new HashMap<>(3);
    selectionIndex.put(FIRST_VALUE, 0);
    selectionIndex.put(SECOND_VALUE, 1);
    selectionIndex.put(THIRD_VALUE, 2);
    selectionIndex.put(TIME_VALUE, 3);

    selPath.add(new ProcessorSelection.SelectionAttribute(FIRST_VALUE, mockAttribute(FIRST_VALUE, String.class)));
    selPath.add(new ProcessorSelection.SelectionAttribute(SECOND_VALUE, mockAttribute(SECOND_VALUE, String.class)));
    selPath.add(new ProcessorSelection.SelectionAttribute(THIRD_VALUE, mockAttribute(THIRD_VALUE, Integer.class)));
    selPath.add(new ProcessorSelection.SelectionAttribute(TIME_VALUE, mockAttributeWithConverter(TIME_VALUE)));
    cut = new TupleImpl(values, selPath, selectionIndex);
  }

  private JPAAttribute mockAttribute(final String alias, final Class<?> clazz) {
    final JPAAttribute a = mock(JPAAttribute.class);
    when(a.getType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return clazz;
      }
    });
    return a;
  }

  private JPAAttribute mockAttributeWithConverter(final String alias) {
    final JPAAttribute attribute = mockAttribute(alias, Timestamp.class);
    when(attribute.getConverter()).thenAnswer(new Answer<AttributeConverter<LocalDateTime, Timestamp>>() {
      @Override
      public AttributeConverter<LocalDateTime, Timestamp> answer(final InvocationOnMock invocation) throws Throwable {
        return new DateTimeConverter();
      }
    });
    when(attribute.getRawConverter()).thenAnswer(new Answer<AttributeConverter<LocalDateTime, Timestamp>>() {
      @Override
      public AttributeConverter<LocalDateTime, Timestamp> answer(final InvocationOnMock invocation) throws Throwable {
        return new DateTimeConverter();
      }
    });
    return attribute;
  }

  @Test
  void testToArrayReturnsCopyOf() {
    assertArrayEquals(values, cut.toArray());
    assertNotEquals(values, cut.toArray());
  }

  @Test
  void testGetByIndexReturnsCorrectValue() {
    assertEquals("World", cut.get(1));
    assertEquals("Hello", cut.get(0));
  }

  @Test
  void testGetByIndexThrowsExceptionOnInvalidIndex() {
    assertThrows(IllegalArgumentException.class, () -> cut.get(5));
    assertThrows(IllegalArgumentException.class, () -> cut.get(-1));
  }

  @Test
  void testGetByAliasReturnsCorrectValue() {
    assertEquals("World", cut.get(SECOND_VALUE));
    assertEquals("Hello", cut.get(FIRST_VALUE));
  }

  @Test
  void testGetByAliasReturnsConverted() {
    final Double act = cut.get(THIRD_VALUE, Double.class);
    assertNotNull(act);
  }

  @Test
  void testGetByAliasThrowsExceptionOnInvalidValue() {
    assertThrows(IllegalArgumentException.class, () -> cut.get("Willi"));
  }

  @Test
  void testGetByIndexWithCastReturnsCorrectValue() {
    assertEquals(3, cut.get(2, Number.class));
  }

  @Test
  void testGetByIndexWithCastThrowsExceptionOnInvalidIndex() {
    assertThrows(IllegalArgumentException.class, () -> cut.get(5, Number.class));
    assertThrows(IllegalArgumentException.class, () -> cut.get(-1, Number.class));
  }

  @Test
  void testGetByAliasWithCastReturnsCorrectValue() {
    assertEquals(3, cut.get(THIRD_VALUE, Number.class));
  }

  @Test
  void testGetByAliasWithCastThrowsExceptionOnInvalidValue() {
    assertThrows(IllegalArgumentException.class, () -> cut.get("Willi", Number.class));
  }

  @Test
  void testGetByAliasWithCastThrowsExceptionOnInvalidCast() {
    assertThrows(IllegalArgumentException.class, () -> cut.get(FIRST_VALUE, Double.class));
  }

  @Test
  void testGetTupleElements() {
    final List<TupleElement<?>> act = cut.getElements();
    boolean secondFound = false;
    assertEquals(4, act.size());
    for (final TupleElement<?> t : act) {
      if (SECOND_VALUE.equals(t.getAlias())) {
        assertEquals(String.class, t.getJavaType());
        assertEquals("World", cut.get(t));
        secondFound = true;
      }
    }
    assertTrue(secondFound);
  }

  @Test
  void testTupleReturnsConvertedValue() {
    cut = new TupleImpl(values, selPath, selectionIndex);
    assertTrue(cut.get(TIME_VALUE) instanceof LocalDateTime);
  }
}
