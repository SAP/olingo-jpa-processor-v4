package com.sap.olingo.jpa.processor.core.test_udf;

import java.math.BigInteger;

public class UserDefinedFunctions {

  private UserDefinedFunctions() {
    // Suppress instantiation
  }

  public static boolean isPrime(final int value) {
    return new BigInteger(String.valueOf(value)).isProbablePrime(100);
  }
}
