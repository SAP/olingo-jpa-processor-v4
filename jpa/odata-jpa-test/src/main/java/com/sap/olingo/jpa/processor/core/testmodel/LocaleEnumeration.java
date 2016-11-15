package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class LocaleEnumeration implements Enumeration<Locale> {
  private final Iterator<Locale> keys;

  public LocaleEnumeration(final List<Locale> keySet) {
    keys = keySet.iterator();
  }

  @Override
  public boolean hasMoreElements() {
    return keys.hasNext();
  }

  @Override
  public Locale nextElement() {
    return keys.next();
  }

}