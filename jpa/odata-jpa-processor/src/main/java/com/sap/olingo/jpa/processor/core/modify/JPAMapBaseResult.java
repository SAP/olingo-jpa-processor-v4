package com.sap.olingo.jpa.processor.core.modify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

public abstract class JPAMapBaseResult extends JPACreateResult {

  protected Map<String, Object> valuePairedResult;
  protected List<Tuple> result;

  public JPAMapBaseResult(JPAEntityType et, Map<String, List<String>> requestHeaders) throws ODataJPAModelException {
    super(et, requestHeaders);
  }

  @Override
  public List<Tuple> getResult(final String key) {
    return result;
  }

  @Override
  public Map<String, List<Tuple>> getResults() {
    final Map<String, List<Tuple>> results = new HashMap<>(1);
    results.put(ROOT_RESULT_KEY, result);
    return results;
  }

  @SuppressWarnings("unchecked")
  protected void convertPathToTuple(final JPATuple tuple, final Map<String, Object> jpaEntity, final JPAPath path,
      final int index) throws ODataJPAProcessorException {

    Object value = jpaEntity.get(path.getPath().get(index).getInternalName());
    if (path.getPath().size() == index + 1 || value == null) {
      tuple.addElement(path.getAlias(), path.getLeaf().getType(), value);
    } else {
      convertPathToTuple(tuple, (Map<String, Object>) value, path, index + 1);
    }
  }

}