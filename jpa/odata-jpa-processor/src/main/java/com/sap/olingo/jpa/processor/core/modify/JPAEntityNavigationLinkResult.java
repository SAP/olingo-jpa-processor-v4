package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Tuple;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPAResultConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

final class JPAEntityNavigationLinkResult extends JPANavigationLinkResult {

  JPAEntityNavigationLinkResult(final JPAEntityType et, final Collection<?> values,
      final Map<String, List<String>> requestHeaders, final JPAResultConverter converter, final String foreignKey)
      throws ODataJPAModelException, ODataApplicationException {
    super(et, requestHeaders);

    for (final Object value : values) {
      final List<Tuple> part = result.computeIfAbsent(foreignKey, k -> new ArrayList<Tuple>());
      part.add(new JPAEntityResult(et, value, requestHeaders, converter).getResult(ROOT_RESULT_KEY).get(0));
    }
  }

  @Override
  protected Map<String, Object> entryAsMap(final Object entry) throws ODataJPAProcessorException {
    return helper.buildGetterMap(entry);
  }
}
