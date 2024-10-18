package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Tuple;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPAResultConverter;
import com.sap.olingo.jpa.processor.core.converter.JPATuple;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.processor.JPARequestEntity;

final class JPAMapResult extends JPAMapBaseResult {
  JPAMapResult(final JPAEntityType et, final Map<String, Object> jpaEntity,
      final Map<String, List<String>> requestHeaders, final JPAResultConverter converter)
      throws ODataJPAModelException, ODataApplicationException {

    super(et, requestHeaders);

    this.valuePairedResult = jpaEntity;
    this.result = createResult();
    createChildren(converter);
  }

  @SuppressWarnings("unchecked")
  private void createChildren(final JPAResultConverter converter) throws ODataJPAModelException,
      ODataApplicationException {
    for (final JPAAssociationPath path : et.getAssociationPathList()) {
      final String pathPropertyName = path.getPath().get(0).getInternalName();
      if (valuePairedResult.get(pathPropertyName) instanceof List) {
        children.put(path,
            new JPAMapNavigationLinkResult((JPAEntityType) path.getTargetType(),
                (List<JPARequestEntity>) valuePairedResult.get(
                    pathPropertyName), requestHeaders, converter, getForeignKeyColumns(path)));
      }
    }
    for (final JPAPath path : et.getCollectionAttributesPath()) {
      Map<String, Object> attributes = valuePairedResult;
      for (final JPAElement element : path.getPath()) {
        final Object value = attributes.get(element.getInternalName());
        if (element instanceof final JPAAttribute attribute && attribute.isComplex() && !(attribute.isCollection())
            && value != null) {
          attributes = (Map<String, Object>) value;
          continue;
        }
        if (element instanceof final JPACollectionAttribute attribute && value != null) {
          final JPAAssociationPath assPath = attribute.asAssociation();
          final JPAExpandResult child = new JPAMapCollectionResult(et, (Collection<?>) value, requestHeaders,
              attribute);
          child.convert(converter);
          children.put(assPath, child);
        }
      }
    }

  }

  private List<Tuple> createResult() throws ODataJPAProcessorException {
    final JPATuple tuple = new JPATuple();
    final List<Tuple> tupleResult = new ArrayList<>();

    for (final JPAPath path : pathList) {
      if (notContainsCollection(path))
        convertPathToTuple(tuple, valuePairedResult, path, 0);
    }
    tupleResult.add(tuple);
    return tupleResult;
  }

  private List<JPAPath> getForeignKeyColumns(final JPAAssociationPath association) throws ODataJPAProcessorException {
    try {
      return association.getForeignKeyColumns();
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.ATTRIBUTE_NOT_FOUND,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }
}
