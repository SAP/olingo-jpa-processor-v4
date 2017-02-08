package com.sap.olingo.jpa.processor.core.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
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
  private final List<Tuple> result;
  private final Map<String, Object> getterMap;

  JPAEntityResult(JPAEntityType et, Object jpaEntity, Map<String, List<String>> requestHeaders)
      throws ODataJPAModelException, ODataJPAProcessorException {

    super(et, requestHeaders);

    this.getterMap = helper.buildGetterMap(jpaEntity);
    this.result = createResult();

    createChildren();
  }

  private void createChildren() throws ODataJPAModelException, ODataJPAProcessorException {
    for (JPAAssociationPath path : et.getAssociationPathList()) {
      String pathPropertyName = path.getPath().get(0).getInternalName();
      Object value = getterMap.get(pathPropertyName);
      if (value instanceof Collection) {
        if (!((Collection<?>) value).isEmpty()) {
          children.put(path, new JPAEntityNavigationLinkResult((JPAEntityType) path.getTargetType(),
              (Collection<?>) value, requestHeaders));
        }
      }
    }

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

    for (JPAPath path : pathList) {
      convertPathToTuple(tuple, getterMap, path, 0);
    }

    tupleResult.add(tuple);
    return tupleResult;
  }

}
