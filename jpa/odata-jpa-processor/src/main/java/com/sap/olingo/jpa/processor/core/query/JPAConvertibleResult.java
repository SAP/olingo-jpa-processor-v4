package com.sap.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPAResultConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.serializer.JPAEntityCollectionExtension;

public interface JPAConvertibleResult {
  /**
   *
   * @param converter
   * @return
   * @throws ODataApplicationException
   */
  Map<String, JPAEntityCollectionExtension> asEntityCollection(final JPAResultConverter converter)
      throws ODataApplicationException;

  void putChildren(final Map<JPAAssociationPath, JPAExpandResult> childResults) throws ODataApplicationException;

  default JPAEntityCollectionExtension getEntityCollection(final String key, final JPAResultConverter converter,
      final JPAAssociationPath association, final List<JPAODataPageExpandInfo> expandInfo)
      throws ODataApplicationException {
    throw new IllegalAccessError("Not supported");
  }

  /**
   * Returns a key pair if the query had $top and/or $skip and the key of the entity implements {@link Comparable}.
   * @param <T>
   * @param requestContext
   * @param hops
   * @return
   * @throws ODataJPAQueryException
   */
  default Optional<JPAKeyBoundary> getKeyBoundary(final JPAODataRequestContextAccess requestContext,
      final List<JPANavigationPropertyInfo> hops) throws ODataJPAProcessException {

    return Optional.empty();
  }

}
