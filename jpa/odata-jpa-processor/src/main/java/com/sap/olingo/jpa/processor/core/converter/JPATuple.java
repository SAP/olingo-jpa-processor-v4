package com.sap.olingo.jpa.processor.core.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;

public final class JPATuple implements Tuple {

  private final List<TupleElement<?>> elements = new ArrayList<>();
  private final Map<String, Object> values = new HashMap<>();

  public void addElement(final String alias, final Class<?> javaType, final Object value) {
    elements.add(new JPATupleElement<>(alias, javaType));
    values.put(alias, value);

  }

  @Override
  public Object get(final int arg0) {
    assert 1 == 2;
    return null;
  }

  @Override
  public <X> X get(final int arg0, final Class<X> arg1) {
    assert 1 == 2;
    return null;
  }

  /**
   * Get the value of the tuple element to which the
   * specified alias has been assigned.
   * @param alias alias assigned to tuple element
   * @return value of the tuple element
   * @throws IllegalArgumentException if alias
   * does not correspond to an element in the
   * query result tuple
   */
  @Override
  public Object get(final String alias) {
    return values.get(alias);
  }

  @Override
  public <X> X get(final String arg0, final Class<X> arg1) {
    assert 1 == 2;
    return null;
  }

  @Override
  public <X> X get(final TupleElement<X> arg0) {
    return null;
  }

  @Override
  public List<TupleElement<?>> getElements() {
    return elements;
  }

  @Override
  public Object[] toArray() {
    return new Object[] {};
  }

  private static record JPATupleElement<X>(String alias, Class<? extends X> javaType) implements TupleElement<X> {

    @Override
    public String getAlias() {
      return alias;
    }

    @Override
    public Class<? extends X> getJavaType() {
      return javaType;
    }

  }
}
