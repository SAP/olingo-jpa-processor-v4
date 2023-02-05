package com.sap.olingo.jpa.processor.core.query;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.persistence.Tuple;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

/**
 * Builds a hierarchy of expand results. One instance contains on the one hand of the result itself, a map which has the
 * join columns values of the parent as its key and on the other hand a map that point the results of the next expand.
 * The join columns are concatenated in the order they are stored in the corresponding Association Path.
 * @author Oliver Grande
 *
 */
public final class JPAExpandQueryResult implements JPAExpandResult, JPAConvertibleResult {
  private static final Map<String, List<Tuple>> EMPTY_RESULT;
  private final Map<JPAAssociationPath, JPAExpandResult> childrenResult;
  private final Map<String, List<Tuple>> jpaResult;
  private Map<String, EntityCollection> odataResult;
  private final Map<String, Long> counts;
  private final JPAEntityType jpaEntityType;
  private final Collection<JPAPath> requestedSelection;

  static {
    EMPTY_RESULT = new HashMap<>(1);
    putEmptyResult();
  }

  /**
   * Add an empty list as result for root to the EMPTY_RESULT. This is needed, as the conversion eats up the database
   * result.
   * @see JPATupleChildConverter
   * @return
   */
  private static Map<String, List<Tuple>> putEmptyResult() {
    EMPTY_RESULT.put(ROOT_RESULT_KEY, Collections.emptyList());
    return EMPTY_RESULT;
  }

  public JPAExpandQueryResult(final JPAEntityType jpaEntityType, final Collection<JPAPath> selectionPath) {
    this(putEmptyResult(), Collections.emptyMap(), jpaEntityType, selectionPath);
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
      for (final Entry<JPAAssociationPath, JPAExpandResult> childResult : childrenResult.entrySet()) {
        childResult.getValue().convert(converter);
      }
      odataResult = converter.getResult(this, requestedSelection);
    }
  }

  @Override
  public JPAExpandResult getChild(final JPAAssociationPath associationPath) {
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
    for (final Entry<String, List<Tuple>> result : jpaResult.entrySet()) {
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

  @Override
  public Optional<JPAKeyBoundary> getKeyBoundary(final JPAODataRequestContextAccess requestContext,
      final List<JPANavigationPropertyInfo> hops, final JPAODataPage page) throws ODataJPAProcessException {
    try {
      if (!jpaResult.get(ROOT_RESULT_KEY).isEmpty()
          && (requestContext.getUriInfo().getExpandOption() != null
              || collectionPropertyRequested(requestContext))
          && ((requestContext.getUriInfo().getTopOption() != null
              || requestContext.getUriInfo().getSkipOption() != null)
              || (page != null && (page.getSkip() != 0 || page.getTop() != Integer.MAX_VALUE)))) {
        final JPAKeyPair boundary = new JPAKeyPair(jpaEntityType.getKey());
        for (final Tuple tuple : jpaResult.get(ROOT_RESULT_KEY)) {
          @SuppressWarnings("rawtypes")
          final Map<JPAAttribute, Comparable> key = createKey(tuple);
          boundary.setValue(key);
        }
        return Optional.of(new JPAKeyBoundary(hops.size(), boundary));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    return JPAConvertibleResult.super.getKeyBoundary(requestContext, hops, page);
  }

  private boolean collectionPropertyRequested(final JPAODataRequestContextAccess requestContext)
      throws ODataJPAModelException {
    if (!jpaEntityType.getCollectionAttributesPath().isEmpty()) {
      final SelectOption selectOptions = requestContext.getUriInfo().getSelectOption();
      if (SelectOptionUtil.selectAll(selectOptions)) {
        return true;
      } else {
        for (final SelectItem item : selectOptions.getSelectItems()) {
          final String pathItem = item.getResourcePath().getUriResourceParts().stream().map(path -> (path
              .getSegmentValue())).collect(Collectors.joining(JPAPath.PATH_SEPARATOR));
          if (this.jpaEntityType.getCollectionAttribute(pathItem) != null) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @SuppressWarnings("rawtypes")
  private Map<JPAAttribute, Comparable> createKey(final Tuple tuple) throws ODataJPAModelException {
    final Map<JPAAttribute, Comparable> keyMap = new HashMap<>(jpaEntityType.getKey().size());
    for (final JPAAttribute key : jpaEntityType.getKey()) {
      keyMap.put(key, (Comparable) tuple.get(key.getExternalName()));
    }
    return keyMap;
  }
}
