package com.sap.olingo.jpa.processor.core.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

/**
 * Builds a hierarchy of expand results. One instance contains on the on hand of the result itself, a map which has the
 * join columns values of the parent as its key and on the other hand a map that point the results of the next expand.
 * The join columns are concatenated in the order they are stored in the corresponding Association Path.
 * @author Oliver Grande
 *
 */
public final class JPAExpandQueryResult implements JPAExpandResult {
  private final Map<JPAAssociationPath, JPAExpandResult> childrenResult;
  private final Map<String, List<Tuple>> jpaResult;
  private Map<String, EntityCollection> odataResult;
  private final Map<String, Long> counts;
  private final JPAEntityType jpaEntityType;

  public JPAExpandQueryResult(final Map<String, List<Tuple>> result, final Map<String, Long> counts,
      final JPAEntityType jpaEntityType) {
    super();
    assertNotNull(jpaEntityType);
    childrenResult = new HashMap<>();
    this.jpaResult = result;
    this.counts = counts;
    this.jpaEntityType = jpaEntityType;
  }

  public Map<String, EntityCollection> asEntityCollection(JPATupleChildConverter converter)
      throws ODataApplicationException {

    convert(new JPATupleChildConverter(converter));

    return odataResult;
  }

  private void convert(JPATupleChildConverter converter) throws ODataApplicationException {
    if (odataResult == null) {
      for (Entry<JPAAssociationPath, JPAExpandResult> childResult : childrenResult.entrySet()) {
        ((JPAExpandQueryResult) childResult.getValue()).convert(converter);
      }
      odataResult = converter.getResult(this);
    }
  }

  @Override
  public JPAExpandResult getChild(JPAAssociationPath associationPath) {
    return childrenResult.get(associationPath);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.org.jpa.processor.core.converter.JPAExpandResult#getChildren()
   */
  @Override
  public Map<JPAAssociationPath, JPAExpandResult> getChildren() {
    return childrenResult;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.org.jpa.processor.core.converter.JPAExpandResult#getCount()
   */
  @Override
  public Long getCount(final String key) {
    return counts != null ? counts.get(key) : null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.org.jpa.processor.core.converter.JPAExpandResult#getEntityType()
   */
  @Override
  public JPAEntityType getEntityType() {
    return jpaEntityType;
  }

  public long getNoResults() {
    return jpaResult.size();
  }

  public long getNoResultsDeep() {
    long count = 0;
    for (String key : jpaResult.keySet()) {
      count += jpaResult.get(key).size();
    }
    return count;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.org.jpa.processor.core.converter.JPAExpandResult#getResult(java.lang.String)
   */
  @Override
  public List<Tuple> getResult(final String key) {
    return jpaResult.get(key);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.org.jpa.processor.core.converter.JPAExpandResult#hasCount()
   */
  @Override
  public boolean hasCount() {
    return counts != null;
  }

  public void putChildren(final Map<JPAAssociationPath, JPAExpandQueryResult> childResults)
      throws ODataApplicationException {

    for (final JPAAssociationPath child : childResults.keySet()) {
      if (childrenResult.get(child) != null)
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_EXPAND_ERROR,
            HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    childrenResult.putAll(childResults);
  }

  private void assertNotNull(final Object instance) {
    if (instance == null)
      throw new NullPointerException();
  }

  @Override
  public Map<String, List<Tuple>> getResults() {
    return jpaResult;
  }

  /**
   * no key --> empty collection
   * @param key
   * @return
   */
  public EntityCollection getEntityCollection(final String key) {
    return odataResult.containsKey(key) ? odataResult.get(key) : new EntityCollection();
  }

}
