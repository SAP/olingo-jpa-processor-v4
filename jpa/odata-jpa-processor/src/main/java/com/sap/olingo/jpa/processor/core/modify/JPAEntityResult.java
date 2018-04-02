package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

/**
 * Provides an entity as tuple result. This is primarily done to reuse the existing tuple converter.
 * 
 * @author Oliver Grande
 *
 */
final class JPAEntityResult extends JPAEntityBasedResult {
  private final Map<String, Object> valuePairedResult;

  JPAEntityResult(final JPAEntityType et, final Object jpaEntity, final Map<String, List<String>> requestHeaders,
      final JPATupleChildConverter converter) throws ODataJPAModelException, ODataApplicationException {

    super(et, requestHeaders);

    this.valuePairedResult = helper.buildGetterMap(jpaEntity);
    this.result = createResult();

    createChildren(converter);
  }

  private void createChildren(final JPATupleChildConverter converter) throws ODataJPAModelException,
      ODataApplicationException {
    for (final JPAAssociationPath path : et.getAssociationPathList()) {
      final String pathPropertyName = path.getPath().get(0).getInternalName();
      final Object value = valuePairedResult.get(pathPropertyName);
      if (value instanceof Collection && !((Collection<?>) value).isEmpty()) {
        children.put(path, new JPAEntityNavigationLinkResult((JPAEntityType) path.getTargetType(),
            (Collection<?>) value, requestHeaders, converter));
      }
    }
    for (final JPAPath path : et.getCollectionAttributesPath()) {
      Map<String, Object> embeddedGetterMap = valuePairedResult;
      for (JPAElement e : path.getPath()) {
        final Object value = embeddedGetterMap.get(e.getInternalName());
        if (e instanceof JPAAttribute && ((JPAAttribute) e).isComplex() && !(((JPAAttribute) e).isCollection())
            && value != null) {
          embeddedGetterMap = helper.buildGetterMap(value);
          continue;
        }
        if (e instanceof JPACollectionAttribute && value != null) {
          final JPAAssociationPath assPath = ((JPACollectionAttribute) e).asAssociation();
          final JPAExpandResult child = new JPAEntityCollectionResult(et, (Collection<?>) value, requestHeaders,
              (JPACollectionAttribute) e);
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

}
