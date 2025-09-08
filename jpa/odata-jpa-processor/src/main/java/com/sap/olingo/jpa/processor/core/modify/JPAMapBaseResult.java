package com.sap.olingo.jpa.processor.core.modify;

import java.util.List;
import java.util.Map;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

public abstract class JPAMapBaseResult extends JPATupleBasedResult {

  protected Map<String, Object> valuePairedResult;
  protected JPAMapBaseResult(final JPAEntityType et, final Map<String, List<String>> requestHeaders)
      throws ODataJPAModelException {
    super(et, requestHeaders);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected String determineLocale(final Map<String, Object> descGetterMap, final JPAPath localeAttribute,
      final int index) throws ODataJPAProcessorException {

    final Object value = descGetterMap.get(localeAttribute.getPath().get(index).getInternalName());
    if (localeAttribute.getPath().size() == index + 1 || value == null) {
      return (String) value;
    } else {
      return determineLocale((Map<String, Object>) value, localeAttribute, index + 1);
    }
  }
}