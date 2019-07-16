package com.sap.olingo.jpa.processor.core.query;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Tuple;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
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
public final class JPAExpandQueryResult implements JPAExpandResult, JPAConvertableResult {
  private static final Map<String, List<Tuple>> EMPTY_RESULT;
  private final Map<JPAAssociationPath, JPAExpandResult> childrenResult;
  private final Map<String, List<Tuple>> jpaResult;
  private Map<String, EntityCollection> odataResult;
  private final Map<String, Long> counts;
  private final JPAEntityType jpaEntityType;
  private final Collection<JPAPath> requestedSelection;

  static {
    EMPTY_RESULT = new HashMap<>(1);
    EMPTY_RESULT.put(JPAExpandResult.ROOT_RESULT_KEY, Collections.emptyList());
  }

  public JPAExpandQueryResult(final JPAEntityType jpaEntityType, final Collection<JPAPath> selectionPath) {
    this(EMPTY_RESULT, Collections.emptyMap(), jpaEntityType, selectionPath);
  }

  public JPAExpandQueryResult(final Map<String, List<Tuple>> result, final Map<String, Long> counts,
      @Nonnull final JPAEntityType jpaEntityType, final Collection<JPAPath> selectionPath) {

    Objects.requireNonNull(jpaEntityType);
    childrenResult = new HashMap<>();
    this.jpaResult = result;
    this.counts = counts;
    this.jpaEntityType = jpaEntityType;
    this.requestedSelection = selectionPath;
  }

  @Override
  public Map<String, EntityCollection> asEntityCollection(final JPATupleChildConverter converter)
      throws ODataApplicationException {

    convert(new JPATupleChildConverter(converter));

    return odataResult;
  }

  @Override
  public void convert(final JPATupleChildConverter converter) throws ODataApplicationException {
    if (odataResult == null) {
      for (Entry<JPAAssociationPath, JPAExpandResult> childResult : childrenResult.entrySet()) {
        childResult.getValue().convert(converter);
      }
      odataResult = converter.getResult(this, requestedSelection);
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
    for (Entry<String, List<Tuple>> result : jpaResult.entrySet()) {
      count += result.getValue().size();
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

  @Override
  public void putChildren(final Map<JPAAssociationPath, JPAExpandResult> childResults)
      throws ODataApplicationException {

    for (final JPAAssociationPath child : childResults.keySet()) {
      if (childrenResult.get(child) != null)
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_EXPAND_ERROR,
            HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    childrenResult.putAll(childResults);
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
  @Override
  public EntityCollection getEntityCollection(final String key) {
    return odataResult.containsKey(key) ? odataResult.get(key) : new EntityCollection();
  }

}
