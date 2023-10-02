package com.sap.olingo.jpa.processor.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;

public class TupleDouble implements Tuple {
  public final Map<String, Object> elementMap;

  public TupleDouble(final Map<String, Object> elementList) {
    super();
    this.elementMap = elementList;
  }

  @Override
  public <X> X get(final TupleElement<X> tupleElement) {
    return null;
  }

  @Override
  public Object get(final String alias) {
    return elementMap.get(alias);
  }

  @Override
  public Object get(final int i) {
    return null;
  }

  @Override
  public <X> X get(final String alias, final Class<X> type) {
    return null;
  }

  @Override
  public <X> X get(final int i, final Class<X> type) {
    return null;
  }

  @Override
  public List<TupleElement<?>> getElements() {
    final List<TupleElement<?>> elementList = new ArrayList<>();
    for (final String alias : elementMap.keySet())
      elementList.add(new TupleElementDouble(alias, elementMap.get(alias)));
    return elementList;
  }

  @Override
  public Object[] toArray() {
    final List<Object> elementList = new ArrayList<>();
    for (final String alias : elementMap.keySet())
      elementList.add(elementMap.get(alias));
    return elementList.toArray();
  }

}
