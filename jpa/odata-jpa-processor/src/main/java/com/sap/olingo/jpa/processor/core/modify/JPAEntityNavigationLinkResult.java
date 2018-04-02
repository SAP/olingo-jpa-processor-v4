package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;

final class JPAEntityNavigationLinkResult extends JPACreateResult {
  private final List<Tuple> result;

  JPAEntityNavigationLinkResult(final JPAEntityType et, final Collection<?> value,
      final Map<String, List<String>> requestHeaders, final JPATupleChildConverter converter)
      throws ODataJPAModelException, ODataApplicationException {
    super(et, requestHeaders);

    result = new ArrayList<>();
    for (Object v : value) {
      result.add(new JPAEntityResult(et, v, requestHeaders, converter).getResult(ROOT_RESULT_KEY).get(0));
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
