package com.sap.olingo.jpa.processor.core.query;

import java.util.Arrays;

class ComparableByteArray implements Comparable<byte[]> {

  private final byte[] bytes;
  private final String value;

  static byte[] unboxedArray(final Object object) {
    if (object instanceof Byte[]) {
      final Byte[] input = (Byte[]) object;
      final byte[] result = new byte[input.length];
      for (int i = 0; i < input.length; i++) {
        result[i] = input[i]; // NOSONAR
      }
      return result;
    } else if (object instanceof byte[])
      return (byte[]) object;
    else
      throw new IllegalArgumentException("Method called with wrong Type");
  }

  ComparableByteArray(final byte[] bytes) {
    super();
    this.bytes = bytes;
    this.value = new String(unboxedArray(bytes));
  }

  @Override
  public int compareTo(final byte[] o) {
    return value.compareTo(new String(o));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(bytes);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof ComparableByteArray)) return false;
    final ComparableByteArray other = (ComparableByteArray) obj;
    return Arrays.equals(bytes, other.bytes);
  }
}
