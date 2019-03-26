package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.query.JPAConvertableResult;

final class JPAEntityNavigationLinkResult extends JPACreateResult implements JPAConvertableResult {
  private final List<Tuple> result;
  private Map<String, EntityCollection> odataResult;
  private final JPATupleChildConverter converter;

  JPAEntityNavigationLinkResult(final JPAEntityType et, final Collection<?> value,
      final Map<String, List<String>> requestHeaders, final JPATupleChildConverter converter)
      throws ODataJPAModelException, ODataApplicationException {
    super(et, requestHeaders);
    this.converter = converter;
    this.result = new ArrayList<>();
    for (Object v : value) {
      result.add(new JPAEntityResult(et, v, requestHeaders, converter).getResult(ROOT_RESULT_KEY).get(0));
    }
  }

  @Override
  public List<Tuple> getResult(String key) {
    return result;
  }

  @Override
  public void convert(final JPATupleChildConverter converter) throws ODataApplicationException {
    odataResult = converter.getResult(this);
  }

  @Override
  public Map<String, List<Tuple>> getResults() {
    final Map<String, List<Tuple>> results = new HashMap<>(1);
    results.put(ROOT_RESULT_KEY, result);
    return results;
  }

  @Override
  public Map<String, EntityCollection> asEntityCollection(JPATupleChildConverter converter)
      throws ODataApplicationException {
    convert(new JPATupleChildConverter(converter));
    return odataResult;
  }

  @Override
  public void putChildren(Map<JPAAssociationPath, JPAExpandResult> childResults) throws ODataApplicationException {
    // Not needed for JPAEntityNavigationLinkResult
  }

  @Override
  public EntityCollection getEntityCollection(String key) throws ODataApplicationException {
    if (odataResult == null) asEntityCollection(converter);
    return odataResult.containsKey(key) ? odataResult.get(key) : new EntityCollection();
  }
}
