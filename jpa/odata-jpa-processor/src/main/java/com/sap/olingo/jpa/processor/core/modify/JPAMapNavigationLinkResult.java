package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.processor.JPARequestEntity;

final class JPAMapNavigationLinkResult extends JPACreateResult {
  private final List<Tuple> result;

  public JPAMapNavigationLinkResult(final JPAEntityType targetType, final List<JPARequestEntity> entities,
      final Map<String, List<String>> requestHeaders, final JPATupleChildConverter converter)
      throws ODataJPAModelException, ODataApplicationException {

    super(targetType, requestHeaders);
    result = new ArrayList<>();

    for (JPARequestEntity entity : entities) {
      result.add(new JPAMapResult(entity.getEntityType(), entity.getData(), requestHeaders, converter).getResult(
          ROOT_RESULT_KEY)
          .get(0));
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
