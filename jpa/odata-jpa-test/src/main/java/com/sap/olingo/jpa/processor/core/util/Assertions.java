package com.sap.olingo.jpa.processor.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.function.Executable;

public class Assertions {
  public static final String CB_ONLY_TEST = "CB_ONLY";

  public static <T extends Throwable> void assertException(final Class<T> expectedType, final Executable executable,
      final String expMessageKey) {

    assertException(expectedType, executable, expMessageKey, null);
  }

  public static <T extends Throwable> void assertException(final Class<T> expectedType, final Executable executable,
      final String expMessageKey, final Integer expStatusCode) {

    try {
      final T act = assertThrows(expectedType, executable); // NOSONAR
      final List<Method> methods = Arrays.asList(act.getClass().getMethods());

      if (hasMethod(methods, "getId")) {
        final Method getId = act.getClass().getMethod("getId");
        assertEquals(expMessageKey, getId.invoke(act));
      }
      if (hasMethod(methods, "getStatusCode")) {
        final Method statusCode = act.getClass().getMethod("getStatusCode");
        assertEquals(expStatusCode, statusCode.invoke(act));
      }
      assertNotNull(act.getMessage());
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e); // NOSONAR
    }
  }

  private static boolean hasMethod(final List<Method> methods, final String name) {
    return methods.stream().filter(m -> m.getName().equals(name)).count() > 0;
  }

  public static <T> void assertListEquals(final List<T> exp, final List<T> act, final Class<T> reflection) {
    assertEquals(exp.size(), act.size());
    boolean found;
    for (final T expItem : exp) {
      found = false;
      for (final T actItem : act) {
        found = EqualsBuilder.reflectionEquals(expItem, actItem, true, reflection);
        if (found) {
          break;
        }
      }
      assertTrue(found, "Could not find " + expItem.toString());
    }
  }

  private Assertions() {
    super();
  }
}
