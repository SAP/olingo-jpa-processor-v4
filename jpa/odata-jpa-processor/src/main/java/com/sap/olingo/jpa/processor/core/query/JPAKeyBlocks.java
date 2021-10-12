package com.sap.olingo.jpa.processor.core.query;

import java.util.Iterator;
import java.util.List;

class JPAKeyBlocks<T> implements Iterable<List<T>> {
  public static int BLOCK_SIZE = 1;
  private Iterable<List<T>> keys;

  @Override
  public Iterator<List<T>> iterator() {
    return new iterator<>();
  }

  private static class iterator<T> implements Iterator<List<T>> {

    @Override
    public boolean hasNext() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public List<T> next() {
      // TODO Auto-generated method stub
      return null;
    }

  }
}
