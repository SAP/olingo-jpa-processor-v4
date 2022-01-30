/**
 *
 */
package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type.PersistenceType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Oliver Grande
 * Created: 11.11.2019
 *
 */
abstract class TestEqualHashCodeMethods {

  protected static Metamodel model;

  @SuppressWarnings({ "rawtypes" })
  public static Stream<Entry<Object, List<Object>>> equalInstances() {
    final Map<Object, List<Object>> instances = new HashMap<>();
    for (final EntityType<?> et : model.getEntities()) {
      if (hasOwnId(et)) {
        final Set<SingularAttribute> keyElements = getKeyAttributes(et);
        final Class<?> keyClass = getKeyClass(et);
        Integer counter = 0;
        try {
          final Object a = keyClass.getConstructor().newInstance();
          final Object b = keyClass.getConstructor().newInstance();

          for (final SingularAttribute keyElement : keyElements) {
            final Method setter = getSetter(keyClass, keyElement);
            setter.invoke(a, getValue(keyElement.getJavaType(), counter));
            setter.invoke(b, getValue(keyElement.getJavaType(), counter));
            counter = counter + 1;
          }
          instances.put(a, Arrays.asList(a, b));
        } catch (SecurityException | InstantiationException | IllegalAccessException
            | IllegalArgumentException | InvocationTargetException e) {
          continue;
        } catch (final NoSuchMethodException e) {
          System.out.println(e.toString());
        }
      }
    }
    return instances.entrySet().stream();
  }

  @SuppressWarnings("rawtypes")
  public static Stream<Entry<Object, List<Object>>> notEqualInstances() {
    final Map<Object, List<Object>> instances = new HashMap<>();
    for (final EntityType<?> et : model.getEntities()) {
      if (hasOwnId(et)) {
        final Set<SingularAttribute> keyElements = getKeyAttributes(et);
        final Class<?> keyClass = getKeyClass(et);
        try {
          for (int i = 0; i < keyElements.size(); i++) {
            Integer counter = 0;
            final Object a = keyClass.getConstructor().newInstance();
            final Object b = keyClass.getConstructor().newInstance();
            final Object c = keyClass.getConstructor().newInstance();

            for (final SingularAttribute keyElement : keyElements) {
              final Method setter = getSetter(keyClass, keyElement);
              setter.invoke(a, getValue(keyElement.getJavaType(), counter));
              if (counter != i) {
                setter.invoke(b, getValue(keyElement.getJavaType(), counter));
                setter.invoke(c, getValue(keyElement.getJavaType(), counter));
              } else {
                setter.invoke(b, getValue(keyElement.getJavaType(), 10 * (i + 1) + counter));
              }
              counter = counter + 1;
            }
            if (instances.containsKey(a))
              instances.get(a).addAll(Arrays.asList(b, c));
            else
              instances.put(a, new ArrayList<>(Arrays.asList(b, c)));
            instances.put(c, Arrays.asList(a, null, LocalTime.now()));
          }
        } catch (SecurityException | InstantiationException | IllegalAccessException
            | IllegalArgumentException | InvocationTargetException e) {
          continue;
        } catch (final NoSuchMethodException e) {
          System.out.println(e.toString());
        }
      }
    }
    return instances.entrySet().stream();
  }

  /**
   * @param javaType
   * @param counter
   * @return
   */
  private static Object getValue(final Class<?> javaType, final Integer counter) {
    if (javaType == String.class)
      return counter.toString();
    if (javaType == Integer.class)
      return counter;
    return null;
  }

  @SuppressWarnings("rawtypes")
  private static Set<SingularAttribute> getKeyAttributes(final EntityType<?> et) {
    final Set<SingularAttribute> keyElements = new HashSet<>();
    try {
      Set<?> attributes = Collections.emptySet();
      if (et.getIdType().getPersistenceType() == PersistenceType.EMBEDDABLE) {
        attributes = model.embeddable(et.getIdType().getJavaType()).getSingularAttributes();
      } else {
        attributes = et.getIdClassAttributes();
      }
      for (final Object keyElement : attributes) {
        keyElements.add((SingularAttribute) keyElement);
      }
    } catch (final IllegalArgumentException e) {
      final SingularAttribute<?, ?> id = et.getId(et.getIdType().getJavaType());
      keyElements.add(id);
    }
    return keyElements;
  }

  private static Class<?> getKeyClass(final EntityType<?> et) {
    if (et.getIdType().getPersistenceType() == PersistenceType.EMBEDDABLE) {
      return et.getIdType().getJavaType();
    } else {
      return et.getJavaType();
    }
  }

  private static boolean hasOwnId(final EntityType<?> et) {
    try {
      et.getJavaType().getMethod("equals", Object.class);
      return !Modifier.isAbstract(et.getJavaType().getModifiers());
      // && et.getIdType().getPersistenceType() == PersistenceType.BASIC;
    } catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
      return false;
    }
  }

  @SuppressWarnings("rawtypes")
  private static Method getSetter(final Class<?> keyClass, final SingularAttribute keyElement)
      throws NoSuchMethodException {
    final StringBuilder setterName = new StringBuilder();
    setterName
        .append("set")
        .append(keyElement.getName().substring(0, 1).toUpperCase())
        .append(keyElement.getName().substring(1));
    final Method setter = keyClass.getMethod(setterName.toString(), keyElement.getJavaType());
    return setter;
  }

  @ParameterizedTest
  @MethodSource("equalInstances")
  void testEqualsReturnsTrue(final Entry<Object, List<Object>> instance) {
    for (final Object comparator : instance.getValue()) {
      assertEquals(comparator, instance.getKey());
    }

  }

  @ParameterizedTest
  @MethodSource("notEqualInstances")
  void testEqualsReturnsFalse(final Entry<Object, List<Object>> instance) {

    for (final Object comparator : instance.getValue()) {
      assertNotEquals(comparator, instance.getKey());
    }

  }

  @ParameterizedTest
  @MethodSource("equalInstances")
  void tesHashCodeReturnsValue(final Entry<Object, List<Object>> instance) {

    assertNotEquals(0, instance.getKey().hashCode());
  }
}
