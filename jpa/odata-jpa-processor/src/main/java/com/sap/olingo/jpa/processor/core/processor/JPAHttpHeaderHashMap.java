package com.sap.olingo.jpa.processor.core.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;

class JPAHttpHeaderHashMap extends HashMap<String, List<String>> implements JPAHttpHeaderMap {
  private static final long serialVersionUID = 7678027573813050132L;

  public JPAHttpHeaderHashMap(final Map<String, List<String>> map) {
    super(map);
  }

  public JPAHttpHeaderHashMap() {
    super();
  }
}
