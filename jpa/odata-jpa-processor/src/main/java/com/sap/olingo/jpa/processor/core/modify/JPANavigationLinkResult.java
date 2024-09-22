package com.sap.olingo.jpa.processor.core.modify;

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
import com.sap.olingo.jpa.processor.core.converter.JPAResultConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.query.JPAConvertibleResult;

abstract class JPANavigationLinkResult extends JPACreateResult implements JPAConvertibleResult {

  Map<String, EntityCollection> odataResult;
  final Map<String, List<Tuple>> result;

  JPANavigationLinkResult(final JPAEntityType et, final Map<String, List<String>> requestHeaders)
      throws ODataJPAModelException {
    super(et, requestHeaders);
    this.result = new HashMap<>();
  }

  @Override
  public Map<String, EntityCollection> asEntityCollection(final JPAResultConverter converter)
      throws ODataApplicationException {
    convert(converter);
    return odataResult;
  }

  @Override
  public EntityCollection getEntityCollection(final String key, final JPAResultConverter converter,
      final List<JPAODataPageExpandInfo> expandInfo) throws ODataApplicationException {
    if (odataResult == null)
      asEntityCollection(converter);
    return odataResult.containsKey(key) ? odataResult.get(key) : new EntityCollection();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void convert(final JPAResultConverter converter) throws ODataApplicationException {
    odataResult = (Map<String, EntityCollection>) converter.getResult(this, Collections.emptySet());
  }

  @Override
  public void putChildren(final Map<JPAAssociationPath, JPAExpandResult> childResults)
      throws ODataApplicationException {
    // Not needed for JPANavigationLinkResult
  }

  @Override
  public List<Tuple> getResult(final String key) {
    return result.get(key);
  }

  @Override
  public Map<String, List<Tuple>> getResults() {
    return result;
  }

  @Override
  protected String determineLocale(final Map<String, Object> descGetterMap, final JPAPath localeAttribute,
      final int index) throws ODataJPAProcessorException {
    // Not needed for JPAMapNavigationLinkResult
    return null;
  }

}
