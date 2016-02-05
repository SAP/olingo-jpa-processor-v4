package org.apache.olingo.jpa.processor.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

public class TupleDouble implements Tuple {
  public final HashMap<String, Object> elementMap;

  public TupleDouble(HashMap<String, Object> elementList) {
    super();
    this.elementMap = elementList;
  }

  @Override
  public <X> X get(TupleElement<X> tupleElement) {
    return null;
  }

  @Override
  public Object get(String alias) {
    return elementMap.get(alias);
  }

  @Override
  public Object get(int i) {
    return null;
  }

  @Override
  public <X> X get(String alias, Class<X> type) {
    return null;
  }

  @Override
  public <X> X get(int i, Class<X> type) {
    return null;
  }

  @Override
  public List<TupleElement<?>> getElements() {
    List<TupleElement<?>> elementList = new ArrayList<TupleElement<?>>();
    for (String alias : elementMap.keySet())
      elementList.add(new TupleElementDouble(alias, elementMap.get(alias)));
    return elementList;
  }

  @Override
  public Object[] toArray() {
    List<Object> elementList = new ArrayList<Object>();
    for (String alias : elementMap.keySet())
      elementList.add(elementMap.get(alias));
    return elementList.toArray();
  }

}
