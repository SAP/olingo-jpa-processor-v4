package com.sap.olingo.jpa.processor.core.modify;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Tuple;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPAResultConverter;
import com.sap.olingo.jpa.processor.core.processor.JPARequestEntity;

final class JPAMapNavigationLinkResult extends JPANavigationLinkResult {

  public JPAMapNavigationLinkResult(final JPAEntityType targetType, final List<JPARequestEntity> entities,
      final Map<String, List<String>> requestHeaders, final JPAResultConverter converter,
      final List<JPAPath> foreignKey) throws ODataJPAModelException, ODataApplicationException {

    super(targetType, requestHeaders);

    for (final JPARequestEntity entity : entities) {
      final var key = buildConcatenatedForeignKey(foreignKey, entity.getData());
      final List<Tuple> part = result.computeIfAbsent(key, k -> new ArrayList<Tuple>());
      part.add(new JPAMapResult(entity.getEntityType(), entity.getData(), requestHeaders, converter)
          .getResult(ROOT_RESULT_KEY).get(0));
    }
  }

  private String buildConcatenatedForeignKey(final List<JPAPath> foreignKey, final Map<String, Object> properties) {
    return foreignKey.stream()
        .map(column -> (properties.get(column.getLeaf().getInternalName())).toString())
        .collect(joining(JPAPath.PATH_SEPARATOR));
  }
}
