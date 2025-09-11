package com.sap.olingo.jpa.processor.core.converter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;
import com.sap.olingo.jpa.processor.core.query.JPAExpandQueryResult;
import com.sap.olingo.jpa.processor.core.serializer.JPAEntityCollectionExtension;

public interface JPAResultConverter {

  public Object getResult(final JPAExpandResult jpaResult, final Collection<JPAPath> requestedSelection)
      throws ODataApplicationException;

  public Map<String, List<Object>> getCollectionResult(final JPACollectionResult jpaResult,
      final Collection<JPAPath> requestedSelection) throws ODataApplicationException;

  public JPAEntityCollectionExtension getResult(JPAExpandQueryResult jpaExpandQueryResult,
      Collection<JPAPath> requestedSelection,
      String parentKey, List<JPAODataPageExpandInfo> expandInfo) throws ODataApplicationException;

}
