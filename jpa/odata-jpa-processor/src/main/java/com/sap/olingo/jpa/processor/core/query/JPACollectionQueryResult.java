package com.sap.olingo.jpa.processor.core.query;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.core.converter.JPACollectionResult;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;

public class JPACollectionQueryResult implements JPACollectionResult, JPAConvertibleResult {
  private static final Map<String, List<Tuple>> EMPTY_RESULT;

  private final Map<JPAAssociationPath, JPAExpandResult> childrenResult;
  private final Map<String, List<Tuple>> jpaResult;
  private Map<String, List<Object>> collectionResult;
  private final Map<String, Long> counts;
  private final JPAEntityType jpaEntityType;
  private final JPAAssociationPath association;
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

  public JPACollectionQueryResult(final JPAEntityType jpaEntityType, final JPAAssociationPath association,
      final Collection<JPAPath> selectionPath) {

    this(putEmptyResult(), Collections.emptyMap(), jpaEntityType, association, selectionPath);
  }

  public JPACollectionQueryResult(final Map<String, List<Tuple>> result, final Map<String, Long> counts,
      final JPAEntityType jpaEntityType, final JPAAssociationPath association,
      final Collection<JPAPath> selectionPath) {
    super();
    this.childrenResult = new HashMap<>(1);
    this.jpaResult = result;
    this.counts = counts;
    this.jpaEntityType = jpaEntityType;
    this.association = association;
    this.requestedSelection = selectionPath;
  }

  @Override
  public Map<String, EntityCollection> asEntityCollection(final JPATupleChildConverter converter)
      throws ODataApplicationException {
    this.collectionResult = converter.getCollectionResult(this, requestedSelection);
    final Map<String, EntityCollection> result = new HashMap<>(1);
    final EntityCollection collection = new EntityCollection();
    final Entity odataEntity = new Entity();
    final JPAAttribute leaf = (JPAAttribute) association.getPath().get(association.getPath().size() - 1);

    odataEntity.getProperties().add(new Property(
        null,
        leaf.getExternalName(),
        leaf.isComplex() ? ValueType.COLLECTION_COMPLEX : ValueType.COLLECTION_PRIMITIVE,
        collectionResult.get(ROOT_RESULT_KEY) != null ? collectionResult.get(ROOT_RESULT_KEY) : Collections
            .emptyList()));
    collection.getEntities().add(odataEntity);
    result.put(ROOT_RESULT_KEY, collection);

    return result;
  }

  @Override
  public void convert(final JPATupleChildConverter converter) throws ODataApplicationException {
    this.collectionResult = converter.getCollectionResult(this, requestedSelection);
  }

  @Override
  public JPAAssociationPath getAssociation() {
    return association;
  }

  @Override
  public JPAExpandResult getChild(final JPAAssociationPath associationPath) {
    return null;
  }

  @Override
  public Map<JPAAssociationPath, JPAExpandResult> getChildren() {
    return childrenResult;
  }

  @Override
  public Long getCount(final String key) {
    return counts != null ? counts.get(key) : null;
  }

  @Override
  public EntityCollection getEntityCollection(final String key) {
    // Not needed yet. Collections with navigation properties not supported
    return new EntityCollection();
  }

  @Override
  public JPAEntityType getEntityType() {
    return jpaEntityType;
  }

  @Override
  public List<Object> getPropertyCollection(final String key) {
    return collectionResult.containsKey(key) ? collectionResult.get(key) : Collections.emptyList();
  }

  @Override
  public List<Tuple> getResult(final String key) {
    return jpaResult.get(key);
  }

  @Override
  public Map<String, List<Tuple>> getResults() {
    return jpaResult;
  }

  @Override
  public boolean hasCount() {
    return counts != null;
  }

  @Override
  public void putChildren(final Map<JPAAssociationPath, JPAExpandResult> childResults)
      throws ODataApplicationException {
    // Not needed yet. Collections with navigation properties not supported
  }
}
