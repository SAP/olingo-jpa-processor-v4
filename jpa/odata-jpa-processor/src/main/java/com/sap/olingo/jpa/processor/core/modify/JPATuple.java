package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

class JPATuple implements Tuple {

  private List<TupleElement<?>> elements = new ArrayList<TupleElement<?>>();
  private Map<String, Object> values = new HashMap<String, Object>();

  public void addElement(String alias, Class<?> javaType, Object value) {
    elements.add(new JPATupleElement<Object>(alias, javaType));
    values.put(alias, value);

  }

  @Override
  public Object get(int arg0) {
    assert 1 == 2;
    return null;
  }

  @Override
  public <X> X get(int arg0, Class<X> arg1) {
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
  public Object get(String alias) {
    return values.get(alias);
  }

  @Override
  public <X> X get(String arg0, Class<X> arg1) {
    assert 1 == 2;
    return null;
  }

  @Override
  public <X> X get(TupleElement<X> arg0) {
    return null;
  }

  @Override
  public List<TupleElement<?>> getElements() {
    return elements;
  }

  @Override
  public Object[] toArray() {
    return null;
  }

  private class JPATupleElement<X> implements TupleElement<X> {

    private final String alias;
    private final Class<? extends X> javaType;

    public JPATupleElement(String alias, Class<? extends X> javaType) {
      this.alias = alias;
      this.javaType = javaType;
    }

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
