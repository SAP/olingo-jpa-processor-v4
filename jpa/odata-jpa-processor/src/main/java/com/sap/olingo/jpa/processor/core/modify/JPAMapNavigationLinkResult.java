package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.processor.JPARequestEntity;

public class JPAMapNavigationLinkResult extends JPACreateResult {
  private final List<Tuple> result;

  public JPAMapNavigationLinkResult(JPAEntityType targetType, List<JPARequestEntity> entities,
      Map<String, List<String>> requestHeaders) throws ODataJPAProcessorException, ODataJPAModelException {
    super(targetType, requestHeaders);
    result = new ArrayList<Tuple>();

    for (JPARequestEntity entity : entities) {
      result.add(new JPAMapResult(entity.getEntityType(), entity.getData(), requestHeaders).getResult("root").get(0));
    }
  }

  @Override
  public List<Tuple> getResult(String key) {
    return result;
  }

}
