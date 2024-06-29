package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.processor.cb.ProcessorSelection;
import com.sap.olingo.jpa.processor.cb.testobjects.UserType;
import com.sap.olingo.jpa.processor.core.testmodel.DateTimeConverter;

class TupleImplTest {
  private static final int NO_ELEMENTS = 7;
  private static final String SECOND_VALUE = "Second";
  private static final String THIRD_VALUE = "Third";
  private static final String FIRST_VALUE = "First";
  private static final String TIME_VALUE = "Timestamp";
  private static final String ENUM_VALUE_STRING = "EnumeratedString";
  private static final String ENUM_VALUE_ORDINAL = "EnumeratedOrdinal";
  private static final String ENUM_VALUE_ERROR = "EnumeratedError";
  private Tuple cut;
  private final Object[] values = { "Hello", "World", 3, Timestamp.valueOf("2019-01-25 14:00:25"), "INTERACTIVE", 0,
      "2019-01-25" };
  private Map<String, Integer> selectionIndex;
  private List<Entry<String, JPAAttribute>> selPath;

  @BeforeEach
  void setup() {

    selPath = new ArrayList<>(NO_ELEMENTS);
    selectionIndex = new HashMap<>(NO_ELEMENTS);
    selectionIndex.put(FIRST_VALUE, 0);
    selectionIndex.put(SECOND_VALUE, 1);
    selectionIndex.put(THIRD_VALUE, 2);
    selectionIndex.put(TIME_VALUE, 3);
    selectionIndex.put(ENUM_VALUE_STRING, 4);
    selectionIndex.put(ENUM_VALUE_ORDINAL, 5);
    selectionIndex.put(ENUM_VALUE_ERROR, 6);

    selPath.add(new ProcessorSelection.SelectionAttribute(FIRST_VALUE, mockAttribute(FIRST_VALUE, String.class)));
    selPath.add(new ProcessorSelection.SelectionAttribute(SECOND_VALUE, mockAttribute(SECOND_VALUE, String.class)));
    selPath.add(new ProcessorSelection.SelectionAttribute(THIRD_VALUE, mockAttribute(THIRD_VALUE, Integer.class)));
    selPath.add(new ProcessorSelection.SelectionAttribute(TIME_VALUE, mockAttributeWithConverter(TIME_VALUE)));
    selPath.add(new ProcessorSelection.SelectionAttribute(ENUM_VALUE_STRING, mockAttributeEnumerated(ENUM_VALUE_STRING,
        String.class)));
    selPath.add(new ProcessorSelection.SelectionAttribute(ENUM_VALUE_ORDINAL, mockAttributeEnumerated(
        ENUM_VALUE_ORDINAL, Integer.class)));
    selPath.add(new ProcessorSelection.SelectionAttribute(ENUM_VALUE_ERROR, mockAttributeEnumerated(ENUM_VALUE_ERROR,
        LocalDate.class)));
    cut = new TupleImpl(values, selPath, selectionIndex);
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
    assertThrows(IllegalArgumentException.class, () -> cut.get(NO_ELEMENTS));
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
    assertThrows(IllegalArgumentException.class, () -> cut.get(NO_ELEMENTS, Number.class));
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
    assertEquals(NO_ELEMENTS, act.size());
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

  @Test
  void testTupleReturnsEnumeratedStringConvertedValue() {
    cut = new TupleImpl(values, selPath, selectionIndex);
    final var act = cut.get(ENUM_VALUE_STRING);
    assertTrue(act instanceof UserType);
    assertEquals("INTERACTIVE", ((UserType) act).toString());
  }

  @Test
  void testTupleReturnsEnumeratedOrdinalConvertedValue() {
    cut = new TupleImpl(values, selPath, selectionIndex);
    final var act = cut.get(ENUM_VALUE_ORDINAL);
    assertTrue(act instanceof UserType);
    assertEquals("BATCH", ((UserType) act).toString());
  }

  @Test
  void testTupleThrowsExceptionEnumeratedWrongType() {
    cut = new TupleImpl(values, selPath, selectionIndex);
    assertThrows(IllegalArgumentException.class, () -> cut.get(ENUM_VALUE_ERROR));
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
    doReturn(new DateTimeConverter()).when(attribute).getConverter();
    doReturn(new DateTimeConverter()).when(attribute).getRawConverter();
    return attribute;
  }

  private JPAAttribute mockAttributeEnumerated(final String alias, final Class<?> dbType) {
    final JPAAttribute attribute = mockAttribute(alias, UserType.class);
    when(attribute.isEnum()).thenReturn(true);
    doReturn(dbType).when(attribute).getDbType();
    return attribute;
  }
}
