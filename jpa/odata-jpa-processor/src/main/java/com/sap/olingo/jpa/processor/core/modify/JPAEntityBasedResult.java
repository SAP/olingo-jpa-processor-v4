package com.sap.olingo.jpa.processor.core.modify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

public abstract class JPAEntityBasedResult extends JPACreateResult {

  protected List<Tuple> result;

  protected JPAEntityBasedResult(final JPAEntityType et, final Map<String, List<String>> requestHeaders)
      throws ODataJPAModelException {
    super(et, requestHeaders);
  }

  @Override
  public List<Tuple> getResult(final String key) {
    return result;
  }

  @Override
  public Map<String, List<Tuple>> getResults() {
    final Map<String, List<Tuple>> results = new HashMap<>(1);
    results.put(ROOT_RESULT_KEY, result);
    return results;
  }

  @Override
  protected String determineLocale(final Map<String, Object> descGetterMap, final JPAPath localeAttribute,
      final int index) throws ODataJPAProcessorException {
    final Object value = descGetterMap.get(localeAttribute.getPath().get(index).getInternalName());
    if (localeAttribute.getPath().size() == index + 1 || value == null) {
      return (String) value;
    } else {
      final Map<String, Object> embeddedGetterMap = helper.buildGetterMap(value);
      return determineLocale(embeddedGetterMap, localeAttribute, index + 1);
    }
  }

  @Override
  protected Map<String, Object> entryAsMap(final Object entry) throws ODataJPAProcessorException {
    return helper.buildGetterMap(entry);
  }
}