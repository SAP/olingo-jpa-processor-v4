package com.sap.olingo.jpa.processor.core.modify;

import java.util.List;
import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.processor.JPARequestEntity;

final class JPAMapNavigationLinkResult extends JPANavigationLinkResult {

  public JPAMapNavigationLinkResult(final JPAEntityType targetType, final List<JPARequestEntity> entities,
      final Map<String, List<String>> requestHeaders, final JPATupleChildConverter converter)
      throws ODataJPAModelException, ODataApplicationException {

    super(targetType, requestHeaders, converter);

    for (final JPARequestEntity entity : entities) {
      result.add(new JPAMapResult(entity.getEntityType(), entity.getData(), requestHeaders, converter).getResult(
          ROOT_RESULT_KEY).get(0));
    }
  }
}
