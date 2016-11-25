package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

class JPAMapResult extends JPACreateResult {
  private final Map<String, Object> jpaEntity;
  private final List<Tuple> result;

  JPAMapResult(final JPAEntityType et, final Map<String, Object> jpaEntity,
      final Map<String, List<String>> requestHeaders) throws ODataJPAModelException, ODataJPAProcessorException {

    super(et, requestHeaders);

    this.jpaEntity = jpaEntity;
    this.result = createResult();
  }

  @Override
  public List<Tuple> getResult(final String key) {
    return result;
  }

  private List<Tuple> createResult() throws ODataJPAProcessorException {
    JPATuple tuple = new JPATuple();
    List<Tuple> tupleResult = new ArrayList<Tuple>();

    for (JPAPath path : pathList) {
      convertPathToTuple(tuple, jpaEntity, path, 0);
    }
    tupleResult.add(tuple);
    return tupleResult;
  }

  @SuppressWarnings("unchecked")
  private void convertPathToTuple(JPATuple tuple, Map<String, Object> jpaEntity, JPAPath path, int index)
      throws ODataJPAProcessorException {

    Object value = jpaEntity.get(path.getPath().get(index).getInternalName());
    if (path.getPath().size() == index + 1 || value == null) {
      addValueToTuple(tuple, path, index, value);
    } else {
      convertPathToTuple(tuple, (Map<String, Object>) value, path, index + 1);
    }
  }
}
