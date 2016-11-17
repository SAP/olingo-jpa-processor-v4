package com.sap.olingo.jpa.processor.core.test_udf;

import java.math.BigInteger;

public class UserDefinedFunctions {
  public static boolean isPrime(int value) {
    return new BigInteger(String.valueOf(value)).isProbablePrime(100);
  }
}
