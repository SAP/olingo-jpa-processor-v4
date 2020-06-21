package com.sap.olingo.jpa.processor.cb.api;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.criteria.Selection;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

public interface ProcessorSelection<X> extends Selection<X>, SqlConvertible {
  /**
   *
   * @return a list of pairs of alias and path
   */
  List<Entry<String, JPAPath>> getResolvedSelection();

  /**
   * Immutable pair
   * @author Oliver Grande
   *
   */
  public static class SelectionItem implements Map.Entry<String, JPAPath> {

    private final String key;
    private final JPAPath value;

    public SelectionItem(final String key, final JPAPath value) {
      super();
      this.key = key;
      this.value = value;
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public JPAPath getValue() {
      return value;
    }

    @Override
    public JPAPath setValue(final JPAPath value) {
      throw new IllegalAccessError();
    }
  }

  public static class SelectionAttribute implements Map.Entry<String, JPAAttribute> {

    private final String key;
    private final JPAAttribute value;

    public SelectionAttribute(final String key, final JPAAttribute value) {
      super();
      this.key = key;
      this.value = value;
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public JPAAttribute getValue() {
      return value;
    }

    @Override
    public JPAAttribute setValue(final JPAAttribute value) {
      throw new IllegalAccessError();
    }
  }
}
