package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ComparableByteArrayTest {

  private ComparableByteArray cut;

  @Test
  void testUnboxedArrayClass() {
    final Byte[] arr = { 0x00, 0x01, 0x05 };
    final byte[] act = ComparableByteArray.unboxedArray(arr);
    for (int i = 0; i < arr.length; i++)
      assertEquals(arr[i], act[i]);
  }

  @Test
  void testUnboxedArrayType() {
    final byte[] arr = { 0x00, 0x01, 0x05 };
    final byte[] act = ComparableByteArray.unboxedArray(arr);
    assertEquals(arr, act);
  }

  @Test
  void testUnboxedArrayThrowsExceptionWrongType() {
    final String[] arr = { "Hallo" };
    assertThrows(IllegalArgumentException.class, () -> ComparableByteArray.unboxedArray(arr));
  }

  @Test
  void testCompareToEqual() {
    final byte[] arr = { 0x40, 0x41, 0x42 };
    cut = new ComparableByteArray(arr);
    assertEquals(0, cut.compareTo(arr));
  }

  @Test
  void testCompareToLower() {
    final byte[] arr = { 0x40, 0x41, 0x42 };
    final byte[] other = { 0x41, 0x42, 0x43 };
    cut = new ComparableByteArray(arr);
    assertTrue(cut.compareTo(other) < 0);
  }

  @Test
  void testCompareToGreater() {
    final byte[] arr = { 0x41, 0x42, 0x43 };
    final byte[] other = { 0x40, 0x41, 0x42 };
    cut = new ComparableByteArray(arr);
    assertTrue(cut.compareTo(other) > 0);
  }

  @Test
  void testHashCodeNotZero() {
    final byte[] arr = { 0x41, 0x42, 0x43 };
    cut = new ComparableByteArray(arr);
    assertNotEquals(0, cut.hashCode());
  }

  @Test
  void testEqualTrue() {
    final byte[] arr = { 0x41, 0x42, 0x43 };
    cut = new ComparableByteArray(arr);
    assertEquals(new ComparableByteArray(arr), cut);
  }

  @Test
  void testEqualTrueSame() {
    final byte[] arr = { 0x41, 0x42, 0x43 };
    cut = new ComparableByteArray(arr);
    assertEquals(cut, cut);
  }

  @Test
  void testEqualFalse() {
    final byte[] arr = { 0x41, 0x42, 0x43 };
    final byte[] other = { 0x40, 0x41, 0x42 };
    cut = new ComparableByteArray(arr);
    assertNotEquals(new ComparableByteArray(other), cut);
  }

  @Test
  void testEqualFalseOtherType() {
    final byte[] arr = { 0x41, 0x42, 0x43 };
    cut = new ComparableByteArray(arr);
    assertNotEquals(cut, "Willi"); // NOSONAR
  }
}
