package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

/**
 * Provides an entity as tuple result. This is primarily done to reuse the existing tuple converter.
 * 
 * @author Oliver Grande
 *
 */
class JPAEntityResult extends JPACreateResult {
  private final Object jpaEntity;
  private final List<Tuple> result;

  JPAEntityResult(JPAEntityType et, Object jpaEntity, Map<String, List<String>> requestHeaders)
      throws ODataJPAModelException, ODataJPAProcessorException {

    super(et, requestHeaders);

    this.jpaEntity = jpaEntity;
    this.result = createResult();
  }

  @Override
  public List<Tuple> getResult(final String key) {
    return result;
  }

  private void convertPathToTuple(final JPATuple tuple, final Map<String, Object> getterMap, final JPAPath path,
      final int index) throws ODataJPAProcessorException {

    Object value = getterMap.get(path.getPath().get(index).getInternalName());
    if (path.getPath().size() == index + 1 || value == null) {
      addValueToTuple(tuple, path, index, value);
    } else {
      final Map<String, Object> embeddedGetterMap = helper.buildGetterMap(value);
      convertPathToTuple(tuple, embeddedGetterMap, path, index + 1);
    }
  }

  private List<Tuple> createResult() throws ODataJPAProcessorException {
    JPATuple tuple = new JPATuple();
    List<Tuple> tupleResult = new ArrayList<Tuple>();

    final Map<String, Object> getterMap = helper.buildGetterMap(jpaEntity);
    for (JPAPath path : pathList) {
      convertPathToTuple(tuple, getterMap, path, 0);
    }

    tupleResult.add(tuple);
    return tupleResult;
  }

}
