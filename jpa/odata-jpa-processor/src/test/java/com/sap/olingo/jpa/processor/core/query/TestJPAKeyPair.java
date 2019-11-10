package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

@SuppressWarnings("rawtypes")
public class TestJPAKeyPair {
  private JPAKeyPair cut;
  private JPAAttribute attribute1;
  private JPAAttribute attribute2;
  private JPAAttribute attribute3;
  private Map<JPAAttribute, Comparable> key1;
  private Map<JPAAttribute, Comparable> key2;
  private Map<JPAAttribute, Comparable> key3;
  private List<JPAAttribute> keyDef;

  @BeforeEach
  public void setup() {
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
  public void testCreatePairWithOnlyOneValue() {
    assertNotNull(cut);
  }

  @Test
  public void testCreatePairWithOneValues() {
    cut.setValue(key1);
    assertFalse(cut.hasUpperBoundary());
    assertEquals(10, cut.getMin().get(attribute1));
  }

  @Test
  public void testCreatePairWithTwoValues() throws ODataJPAQueryException {
    cut.setValue(key1);
    cut.setValue(key2);
    assertEquals(10, cut.getMin().get(attribute1));
    assertEquals(100, cut.getMax().get(attribute1));
    assertTrue(cut.hasUpperBoundary());
  }

  @Test
  public void testCreatePairWithTwoValuesSecondLower() throws ODataJPAQueryException {
    cut.setValue(key2);
    cut.setValue(key1);
    assertEquals(10, cut.getMin().get(attribute1));
    assertEquals(100, cut.getMax().get(attribute1));
    assertTrue(cut.hasUpperBoundary());
  }

  @Test
  public void testCreatePairWithThirdValuesHigher() throws ODataJPAQueryException {
    key3.put(attribute1, Integer.valueOf(101));
    cut.setValue(key2);
    cut.setValue(key1);
    cut.setValue(key3);
    assertEquals(10, cut.getMin().get(attribute1));
    assertEquals(101, cut.getMax().get(attribute1));
  }

  @Test
  public void testCreatePairWithThirdValuesLower() throws ODataJPAQueryException {
    key3.put(attribute1, Integer.valueOf(9));
    cut.setValue(key2);
    cut.setValue(key1);
    cut.setValue(key3);
    assertEquals(9, cut.getMin().get(attribute1));
    assertEquals(100, cut.getMax().get(attribute1));
  }

  @Test
  public void testCreatePairWithThirdValuesBetween() throws ODataJPAQueryException {
    key3.put(attribute1, Integer.valueOf(50));
    cut.setValue(key2);
    cut.setValue(key1);
    cut.setValue(key3);
    assertEquals(10, cut.getMin().get(attribute1));
    assertEquals(100, cut.getMax().get(attribute1));
  }

  @Test
  public void testCreatePairWithOneCompound() {
    fillKeyAttributes();
    cut.setValue(createCompoundKey("A", "B", "C"));
    assertFalse(cut.hasUpperBoundary());
    assertEquals("A", cut.getMin().get(attribute1));
  }

  @Test
  public void testCreatePairWithTwoCompoundSame() {
    fillKeyAttributes();
    cut.setValue(createCompoundKey("A", "B", "C"));
    cut.setValue(createCompoundKey("A", "B", "C"));
    assertFalse(cut.hasUpperBoundary());
    assertEquals("A", cut.getMin().get(attribute1));
  }

  @Test
  public void testCreatePairWithTwoCompoundLastBigger() {
    fillKeyAttributes();
    cut.setValue(createCompoundKey("A", "B", "C"));
    cut.setValue(createCompoundKey("A", "B", "D"));
    assertTrue(cut.hasUpperBoundary());
    assertEquals("C", cut.getMin().get(attribute3));
    assertEquals("D", cut.getMax().get(attribute3));
  }

  @Test
  public void testCreatePairWithTwoCompoundFirstBigger() {
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
  public void testCreatePairWithThreeCompoundLastKeyBigger() {
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
  public void testCreatePairWithThreeCompoundSecondBigger() {
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
  public void testCreatePairWithThreeCompoundLastKeySmallest() {
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
}
