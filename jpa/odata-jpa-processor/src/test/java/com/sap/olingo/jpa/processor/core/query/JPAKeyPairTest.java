package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.AttributeConverter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAKeyPairException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.testmodel.UUIDToBinaryConverter;
import com.sap.olingo.jpa.processor.core.testmodel.UUIDToStringConverter;

@SuppressWarnings("rawtypes")
class JPAKeyPairTest {
  private JPAKeyPair cut;
  private JPAAttribute attribute1;
  private JPAAttribute attribute2;
  private JPAAttribute attribute3;
  private Map<JPAAttribute, Comparable> key1;
  private Map<JPAAttribute, Comparable> key2;
  private Map<JPAAttribute, Comparable> key3;
  private List<JPAAttribute> keyDef;

  @BeforeEach
  void setup() {
    attribute1 = mock(JPAAttribute.class);
    attribute2 = mock(JPAAttribute.class);
    attribute3 = mock(JPAAttribute.class);
    key1 = new HashMap<>(3);
    key2 = new HashMap<>(3);
    key3 = new HashMap<>(3);
    keyDef = new ArrayList<>(3);
    keyDef.add(attribute1);
    cut = new JPAKeyPair(keyDef);
    key1.put(attribute1, Integer.valueOf(10));
    key2.put(attribute1, Integer.valueOf(100));
  }

  @Test
  void testCreatePairWithOnlyOneValue() {
    assertNotNull(cut);
  }

  @Test
  void testToStringContainsMinMax() throws ODataJPAKeyPairException {
    cut.setValue(key1);
    cut.setValue(key2);
    final String act = cut.toString();

    assertTrue(act.contains("10"));
    assertTrue(act.contains("100"));
  }

  @Test
  void testCreatePairWithOneValues() throws ODataJPAKeyPairException {
    cut.setValue(key1);
    assertFalse(cut.hasUpperBoundary());
    assertEquals(10, cut.getMin().get(attribute1));
  }

  @Test
  void testCreatePairWithTwoValues() throws ODataJPAQueryException, ODataJPAKeyPairException {
    cut.setValue(key1);
    cut.setValue(key2);
    assertEquals(10, cut.getMin().get(attribute1));
    assertEquals(100, cut.getMax().get(attribute1));
    assertTrue(cut.hasUpperBoundary());
  }

  @Test
  void testCreatePairWithTwoValuesSecondLower() throws ODataJPAQueryException, ODataJPAKeyPairException {
    cut.setValue(key2);
    cut.setValue(key1);
    assertEquals(10, cut.getMin().get(attribute1));
    assertEquals(100, cut.getMax().get(attribute1));
    assertTrue(cut.hasUpperBoundary());
  }

  @Test
  void testCreatePairWithThirdValuesHigher() throws ODataJPAQueryException, ODataJPAKeyPairException {
    key3.put(attribute1, Integer.valueOf(101));
    cut.setValue(key2);
    cut.setValue(key1);
    cut.setValue(key3);
    assertEquals(10, cut.getMin().get(attribute1));
    assertEquals(101, cut.getMax().get(attribute1));
  }

  @Test
  void testCreatePairWithThirdValuesLower() throws ODataJPAQueryException, ODataJPAKeyPairException {
    key3.put(attribute1, Integer.valueOf(9));
    cut.setValue(key2);
    cut.setValue(key1);
    cut.setValue(key3);
    assertEquals(9, cut.getMin().get(attribute1));
    assertEquals(100, cut.getMax().get(attribute1));
  }

  @Test
  void testCreatePairWithThirdValuesBetween() throws ODataJPAQueryException, ODataJPAKeyPairException {
    key3.put(attribute1, Integer.valueOf(50));
    cut.setValue(key2);
    cut.setValue(key1);
    cut.setValue(key3);
    assertEquals(10, cut.getMin().get(attribute1));
    assertEquals(100, cut.getMax().get(attribute1));
  }

  @Test
  void testCreatePairWithOneCompound() throws ODataJPAKeyPairException {
    fillKeyAttributes();
    cut.setValue(createCompoundKey("A", "B", "C"));
    assertFalse(cut.hasUpperBoundary());
    assertEquals("A", cut.getMin().get(attribute1));
  }

  @Test
  void testCreatePairWithTwoCompoundSame() throws ODataJPAKeyPairException {
    fillKeyAttributes();
    cut.setValue(createCompoundKey("A", "B", "C"));
    cut.setValue(createCompoundKey("A", "B", "C"));
    assertFalse(cut.hasUpperBoundary());
    assertEquals("A", cut.getMin().get(attribute1));
  }

  @Test
  void testCreatePairWithTwoCompoundLastBigger() throws ODataJPAKeyPairException {
    fillKeyAttributes();
    cut.setValue(createCompoundKey("A", "B", "C"));
    cut.setValue(createCompoundKey("A", "B", "D"));
    assertTrue(cut.hasUpperBoundary());
    assertEquals("C", cut.getMin().get(attribute3));
    assertEquals("D", cut.getMax().get(attribute3));
  }

  @Test
  void testCreatePairWithTwoCompoundFirstBigger() throws ODataJPAKeyPairException {
    fillKeyAttributes();
    cut.setValue(createCompoundKey("A", "B", "C"));
    cut.setValue(createCompoundKey("B", "B", "C"));
    assertTrue(cut.hasUpperBoundary());
    assertEquals("C", cut.getMin().get(attribute3));
    assertEquals("C", cut.getMax().get(attribute3));
    assertEquals("A", cut.getMin().get(attribute1));
    assertEquals("B", cut.getMax().get(attribute1));
  }

  @Test
  void testCreatePairWithThreeCompoundLastKeyBigger() throws ODataJPAKeyPairException {
    fillKeyAttributes();
    cut.setValue(createCompoundKey("A", "B", "C"));
    cut.setValue(createCompoundKey("B", "A", "D"));
    cut.setValue(createCompoundKey("C", "B", "C"));
    assertTrue(cut.hasUpperBoundary());
    assertEquals("C", cut.getMin().get(attribute3));
    assertEquals("C", cut.getMax().get(attribute3));
    assertEquals("A", cut.getMin().get(attribute1));
    assertEquals("C", cut.getMax().get(attribute1));
  }

  @Test
  void testCreatePairWithThreeCompoundSecondBigger() throws ODataJPAKeyPairException {
    fillKeyAttributes();
    cut.setValue(createCompoundKey("A", "B", "C"));
    cut.setValue(createCompoundKey("C", "B", "C"));
    cut.setValue(createCompoundKey("B", "A", "D"));
    assertTrue(cut.hasUpperBoundary());
    assertEquals("C", cut.getMin().get(attribute3));
    assertEquals("C", cut.getMax().get(attribute3));
    assertEquals("A", cut.getMin().get(attribute1));
    assertEquals("C", cut.getMax().get(attribute1));
  }

  @Test
  void testCreatePairWithThreeCompoundLastKeySmallest() throws ODataJPAKeyPairException {
    fillKeyAttributes();
    cut.setValue(createCompoundKey("B", "A", "D"));
    cut.setValue(createCompoundKey("C", "B", "C"));
    cut.setValue(createCompoundKey("A", "B", "C"));
    assertTrue(cut.hasUpperBoundary());
    assertEquals("C", cut.getMin().get(attribute3));
    assertEquals("C", cut.getMax().get(attribute3));
    assertEquals("A", cut.getMin().get(attribute1));
    assertEquals("C", cut.getMax().get(attribute1));
  }

  @Test
  void testCreatePairConversionString() {
    final JPAAttribute attributeUUID = mock(JPAAttribute.class);
    when(attributeUUID.getConverter()).thenAnswer(new Answer<UUIDToStringConverter>() {
      @Override
      public UUIDToStringConverter answer(final InvocationOnMock invocation) throws Throwable {
        return new UUIDToStringConverter();
      }
    });
    when(attributeUUID.getRawConverter()).thenAnswer(new Answer<UUIDToStringConverter>() {
      @Override
      public UUIDToStringConverter answer(final InvocationOnMock invocation) throws Throwable {
        return new UUIDToStringConverter();
      }
    });

    cut = new JPAKeyPair(Collections.singletonList(attributeUUID));

    Arrays.asList("400d7044-1e84-4e63-b2d9-0f58f4ca5bd0", "52a4eb6d-ab9d-4bc8-8405-5255d9607441",
        "59ce6d1c-0770-48ae-b9ea-47c4ce9994c1", "9768b78c-e010-4e62-bada-8a138be7334d",
        "e5406bb9-7166-4c0a-928c-f6deed6325bc").forEach(u -> addUUID(attributeUUID, u));
    assertTrue(cut.hasUpperBoundary());
    assertEquals("400d7044-1e84-4e63-b2d9-0f58f4ca5bd0", cut.getMinElement(attributeUUID).toString());
    assertEquals("400d7044-1e84-4e63-b2d9-0f58f4ca5bd0", cut.getMin().get(attributeUUID).toString());
    assertEquals("e5406bb9-7166-4c0a-928c-f6deed6325bc", cut.getMaxElement(attributeUUID).toString());
    assertEquals("e5406bb9-7166-4c0a-928c-f6deed6325bc", cut.getMax().get(attributeUUID).toString());
  }

  @Test
  void testCreatePairConversionByteArray() {
    final JPAAttribute attributeUUID = mock(JPAAttribute.class);
    when(attributeUUID.getDbType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return byte[].class;
      }
    });
    when(attributeUUID.getConverter()).thenAnswer(new Answer<UUIDToBinaryConverter>() {
      @Override
      public UUIDToBinaryConverter answer(final InvocationOnMock invocation) throws Throwable {
        return new UUIDToBinaryConverter();
      }
    });

    when(attributeUUID.getRawConverter()).thenAnswer(new Answer<UUIDToBinaryConverter>() {
      @Override
      public UUIDToBinaryConverter answer(final InvocationOnMock invocation) throws Throwable {
        return new UUIDToBinaryConverter();
      }
    });

    cut = new JPAKeyPair(Collections.singletonList(attributeUUID));

    Arrays.asList("400d7044-1e84-4e63-b2d9-0f58f4ca5bd0", "52a4eb6d-ab9d-4bc8-8405-5255d9607441",
        "59ce6d1c-0770-48ae-b9ea-47c4ce9994c1", "9768b78c-e010-4e62-bada-8a138be7334d",
        "e5406bb9-7166-4c0a-928c-f6deed6325bc").forEach(u -> addUUID(attributeUUID, u));
    assertTrue(cut.hasUpperBoundary());
    assertEquals("400d7044-1e84-4e63-b2d9-0f58f4ca5bd0", cut.getMinElement(attributeUUID).toString());
    assertEquals("400d7044-1e84-4e63-b2d9-0f58f4ca5bd0", cut.getMin().get(attributeUUID).toString());
    assertEquals("9768b78c-e010-4e62-bada-8a138be7334d", cut.getMaxElement(attributeUUID).toString());
    assertEquals("9768b78c-e010-4e62-bada-8a138be7334d", cut.getMax().get(attributeUUID).toString());
  }

  @Test
  void testCreatePairConversionTargetNotComparable() throws ODataJPAKeyPairException {
    final JPAAttribute attribute = mock(JPAAttribute.class);
    when(attribute.getDbType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return NotComparable.class;
      }
    });
    when(attribute.getConverter()).thenAnswer(new Answer<NotComparableConverter>() {
      @Override
      public NotComparableConverter answer(final InvocationOnMock invocation) throws Throwable {
        return new NotComparableConverter();
      }
    });

    when(attribute.getRawConverter()).thenAnswer(new Answer<NotComparableConverter>() {
      @Override
      public NotComparableConverter answer(final InvocationOnMock invocation) throws Throwable {
        return new NotComparableConverter();
      }
    });

    cut = new JPAKeyPair(Collections.singletonList(attribute));
    final Map<JPAAttribute, Comparable> key = new HashMap<>(3);
    key.put(attribute, "Hallo");
    cut.setValue(key);
    assertThrows(ODataJPAKeyPairException.class, () -> cut.setValue(key));
  }

  private void addUUID(final JPAAttribute attributeUUID, final String uuid) {
    final UUID id = UUID.fromString(uuid);
    try {
      cut.setValue(Collections.singletonMap(attributeUUID, id));
    } catch (final ODataJPAKeyPairException e) {
      fail();
    }
  }

  private void fillKeyAttributes() {
    keyDef.add(attribute2);
    keyDef.add(attribute3);
  }

  private Map<JPAAttribute, Comparable> createCompoundKey(final String first, final String second,
      final String third) {

    final Map<JPAAttribute, Comparable> key = new HashMap<>(3);
    key.put(attribute1, first);
    key.put(attribute2, second);
    key.put(attribute3, third);
    return key;
  }

  private static class NotComparable {

  }

  private static class NotComparableConverter implements AttributeConverter<String, NotComparable> {

    @Override
    public NotComparable convertToDatabaseColumn(final String attribute) {
      return new NotComparable();
    }

    @Override
    public String convertToEntityAttribute(final NotComparable dbData) {
      return "Test";
    }
  }
}
