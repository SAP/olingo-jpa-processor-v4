package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

class JPAEntityNavigationLinkResult extends JPACreateResult {
  private final List<Tuple> result;

  JPAEntityNavigationLinkResult(JPAEntityType et, Collection<?> value,
      Map<String, List<String>> requestHeaders) throws ODataJPAProcessorException, ODataJPAModelException {
    super(et, requestHeaders);

    result = new ArrayList<Tuple>();
    for (Object v : value) {
      result.add(new JPAEntityResult(et, v, requestHeaders).getResult("root").get(0));
    }
  }

  @Override
  public List<Tuple> getResult(String key) {
    return result;
  }

}
