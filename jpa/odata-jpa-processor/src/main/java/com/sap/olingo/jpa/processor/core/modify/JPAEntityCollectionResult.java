package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPACollectionResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

final class JPAEntityCollectionResult extends JPAEntityBasedResult implements JPACollectionResult { // JPACollectionQueryResult

  private Map<String, List<Object>> converted;
  private final JPAAssociationPath path;

  JPAEntityCollectionResult(final JPAEntityType et, final Collection<?> values,
      final Map<String, List<String>> requestHeaders, final JPACollectionAttribute attribute)
      throws ODataJPAModelException, ODataJPAProcessorException {
    super(et, requestHeaders);
    this.path = attribute.asAssociation();
    result = convertToTuple(et, values, attribute);

  }

  private List<Tuple> convertToTuple(final JPAEntityType et, final Collection<?> values,
      final JPACollectionAttribute attribute) throws ODataJPAProcessorException, ODataJPAModelException {

    final List<Tuple> tupleList = new ArrayList<>();

    for (final Object value : values) {
      final JPATuple tuple = new JPATuple();
      if (attribute.isComplex()) {
        final Map<String, Object> embeddedGetterMap = helper.buildGetterMap(value);
        for (JPAPath p : attribute.getStructuredType().getPathList())
          convertPathToTuple(tuple, embeddedGetterMap, et.getPath(this.path.getAlias()
              + JPAPath.PATH_SEPERATOR + p.getAlias()), 1);
      } else {
        tuple.addElement(path.getAlias(), attribute.getType(), value);
      }
      tupleList.add(tuple);
    }
    return tupleList;
  }

  @Override
  public void convert(final JPATupleChildConverter converter) throws ODataApplicationException {
    converted = converter.getCollectionResult(this);
  }

  @Override
  public List<Object> getPropertyCollection(String key) {
    return converted.get(ROOT_RESULT_KEY);
  }

  @Override
  public JPAAssociationPath getAssoziation() {
    return path;
  }
}
