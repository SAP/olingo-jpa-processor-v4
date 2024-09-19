package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
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
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.query.JPAConvertibleResult;

abstract class JPANavigationLinkResult extends JPACreateResult implements JPAConvertibleResult {

  Map<String, EntityCollection> odataResult;
  final JPATupleChildConverter converter;
  final List<Tuple> result;

  JPANavigationLinkResult(final JPAEntityType et, final Map<String, List<String>> requestHeaders,
      final JPATupleChildConverter converter)
      throws ODataJPAModelException {
    super(et, requestHeaders);
    this.result = new ArrayList<>();
    this.converter = converter;
  }

  @Override
  public Map<String, EntityCollection> asEntityCollection(final JPATupleChildConverter converter)
      throws ODataApplicationException {
    convert(converter);
    return odataResult;
  }

  @Override
  public EntityCollection getEntityCollection(final String key) throws ODataApplicationException {
    if (odataResult == null)
      asEntityCollection(converter);
    return odataResult.containsKey(key) ? odataResult.get(key) : new EntityCollection();
  }

  @Override
  public EntityCollection getEntityCollection(final String key, final JPATupleChildConverter converter,
      final List<JPAODataPageExpandInfo> expandInfo) throws ODataApplicationException {
    return getEntityCollection(key);
  }

  @Override
  public void convert(final JPATupleChildConverter converter) throws ODataApplicationException {
    odataResult = converter.getResult(this, Collections.emptySet());
  }

  @Override
  public void putChildren(final Map<JPAAssociationPath, JPAExpandResult> childResults)
      throws ODataApplicationException {
    // Not needed for JPANavigationLinkResult
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
    // Not needed for JPAMapNavigationLinkResult
    return null;
  }

}
