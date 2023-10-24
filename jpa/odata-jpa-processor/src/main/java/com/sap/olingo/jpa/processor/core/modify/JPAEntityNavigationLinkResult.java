package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Tuple;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.query.JPAConvertibleResult;

final class JPAEntityNavigationLinkResult extends JPACreateResult implements JPAConvertibleResult {
  private final List<Tuple> result;
  private Map<String, EntityCollection> odataResult;
  private final JPATupleChildConverter converter;

  JPAEntityNavigationLinkResult(final JPAEntityType et, final Collection<?> value,
      final Map<String, List<String>> requestHeaders, final JPATupleChildConverter converter)
      throws ODataJPAModelException, ODataApplicationException {
    super(et, requestHeaders);
    this.converter = converter;
    this.result = new ArrayList<>();
    for (final Object v : value) {
      result.add(new JPAEntityResult(et, v, requestHeaders, converter).getResult(ROOT_RESULT_KEY).get(0));
    }
  }

  @Override
  public Map<String, EntityCollection> asEntityCollection(final JPATupleChildConverter converter)
      throws ODataApplicationException {
    convert(new JPATupleChildConverter(converter));
    return odataResult;
  }

  @Override
  public void convert(final JPATupleChildConverter converter) throws ODataApplicationException {
    odataResult = converter.getResult(this, Collections.emptySet());
  }

  @Override
  public EntityCollection getEntityCollection(final String key) throws ODataApplicationException {
    if (odataResult == null) asEntityCollection(converter);
    return odataResult.containsKey(ROOT_RESULT_KEY) ? odataResult.get(ROOT_RESULT_KEY) : new EntityCollection();
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
  public void putChildren(final Map<JPAAssociationPath, JPAExpandResult> childResults)
      throws ODataApplicationException {
    // Not needed for JPAEntityNavigationLinkResult
  }

  @Override
  protected String determineLocale(final Map<String, Object> descGetterMap, final JPAPath localeAttribute,
      final int index) throws ODataJPAProcessorException {
    // Not needed for JPAEntityNavigationLinkResult
    return null;
  }

  @Override
  protected Map<String, Object> entryAsMap(final Object entry) throws ODataJPAProcessorException {
    return helper.buildGetterMap(entry);
  }

}
