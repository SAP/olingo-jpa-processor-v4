package org.apache.olingo.jpa.processor.core.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.org.jpa.processor.core.converter.JPAExpandResult;

/**
 * Builds a hierarchy of expand results. One instance contains on the on hand of the result itself, a map which has the
 * join columns values of the parent as its key and on the other hand a map that point the results of the next expand.
 * The join columns are concatenated in the order they are stored in the corresponding Association Path.
 * @author Oliver Grande
 *
 */
public final class JPAExpandQueryResult implements JPAExpandResult {

  private final Map<JPAAssociationPath, JPAExpandResult> childrenResult;
  private final Map<String, List<Tuple>> result;
  private final Long count;
  private final JPAEntityType jpaEntityType;

  public JPAExpandQueryResult(final Map<String, List<Tuple>> result, final Long count,
      final JPAEntityType jpaEntityType) {
    super();
    assertNotNull(jpaEntityType);
    childrenResult = new HashMap<JPAAssociationPath, JPAExpandResult>();
    this.result = result;
    this.count = count;
    this.jpaEntityType = jpaEntityType;
  }

  private void assertNotNull(final Object instance) {
    if (instance == null)
      throw new NullPointerException();
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

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.org.jpa.processor.core.converter.JPAExpandResult#getResult(java.lang.String)
   */
  @Override
  public List<Tuple> getResult(final String key) {
    return result.get(key);
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
   * @see org.apache.org.jpa.processor.core.converter.JPAExpandResult#hasCount()
   */
  @Override
  public boolean hasCount() {
    return count != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.org.jpa.processor.core.converter.JPAExpandResult#getCount()
   */
  @Override
  public Integer getCount() {
    return count != null ? Integer.valueOf(count.intValue()) : null;
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
    return result.size();
  }

  public long getNoResultsDeep() {
    long count = 0;
    for (String key : result.keySet()) {
      count += result.get(key).size();
    }
    return count;
  }
}
