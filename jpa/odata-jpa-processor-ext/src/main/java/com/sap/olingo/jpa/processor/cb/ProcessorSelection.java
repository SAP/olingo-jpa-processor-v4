package com.sap.olingo.jpa.processor.cb;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.persistence.criteria.Selection;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

public interface ProcessorSelection<X> extends Selection<X> {
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
  public static record SelectionItem(String key, JPAPath value) implements Map.Entry<String, JPAPath> {

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

  public static record SelectionAttribute(String key, JPAAttribute value) implements Map.Entry<String, JPAAttribute> {

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
