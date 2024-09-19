package com.sap.olingo.jpa.processor.core.modify;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

final class JPAEntityNavigationLinkResult extends JPANavigationLinkResult {

  JPAEntityNavigationLinkResult(final JPAEntityType et, final Collection<?> value,
      final Map<String, List<String>> requestHeaders, final JPATupleChildConverter converter)
      throws ODataJPAModelException, ODataApplicationException {
    super(et, requestHeaders, converter);

    for (final Object v : value) {
      result.add(new JPAEntityResult(et, v, requestHeaders, converter).getResult(ROOT_RESULT_KEY).get(0));
    }
  }

  @Override
  protected Map<String, Object> entryAsMap(final Object entry) throws ODataJPAProcessorException {
    return helper.buildGetterMap(entry);
  }

}
