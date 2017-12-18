package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

final class JPAEntityNavigationLinkResult extends JPACreateResult {
  private final List<Tuple> result;

  JPAEntityNavigationLinkResult(JPAEntityType et, Collection<?> value,
      Map<String, List<String>> requestHeaders) throws ODataJPAProcessorException, ODataJPAModelException {
    super(et, requestHeaders);

    result = new ArrayList<>();
    for (Object v : value) {
      result.add(new JPAEntityResult(et, v, requestHeaders).getResult("root").get(0));
    }
  }

  @Override
  public List<Tuple> getResult(String key) {
    return result;
  }

  @Override
  public Map<String, List<Tuple>> getResults() {
    final Map<String, List<Tuple>> results = new HashMap<>(1);
    results.put(ROOT_RESULT_KEY, result);
    return results;
  }
}
