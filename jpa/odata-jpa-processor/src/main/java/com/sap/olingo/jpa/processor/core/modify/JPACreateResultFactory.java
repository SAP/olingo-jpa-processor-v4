package com.sap.olingo.jpa.processor.core.modify;

import java.util.List;
import java.util.Map;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.org.jpa.processor.core.converter.JPAExpandResult;

public class JPACreateResultFactory {

  @SuppressWarnings("unchecked")
  public JPAExpandResult getJPACreateResult(JPAEntityType et, Object result, Map<String, List<String>> requestHeaders)
      throws ODataJPAProcessorException, ODataJPAModelException {

    if (result instanceof Map<?, ?>)
      return new JPAMapResult(et, (Map<String, Object>) result, requestHeaders);
    else
      return new JPAEntityResult(et, result, requestHeaders);
  }
}
