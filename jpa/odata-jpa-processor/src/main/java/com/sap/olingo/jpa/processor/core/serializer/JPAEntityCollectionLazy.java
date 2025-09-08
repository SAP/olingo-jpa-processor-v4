package com.sap.olingo.jpa.processor.core.serializer;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jakarta.persistence.Tuple;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCache;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCacheSupplier;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPARowConverter;
import com.sap.olingo.jpa.processor.core.query.JPAExpandQueryResult;

public class JPAEntityCollectionLazy extends EntityCollection implements JPAEntityCollectionExtension {

  private final JPAExpandResult jpaResult;
  private final JPAEntityType jpaEntity;
  private final JPARowConverter converter;
  private final InstanceCache<Entity> firstEntity;

  public JPAEntityCollectionLazy(final JPAExpandResult result, final JPARowConverter converter) {
    super();
    this.jpaResult = result;
    this.jpaEntity = jpaResult.getEntityType();
    this.converter = converter;
    this.firstEntity = new InstanceCacheSupplier<>(this::createFirstResult);
  }

  @Override
  public List<Entity> getEntities() {
    return List.of();
  }

  @Override
  public Iterator<Entity> iterator() {
    return new EntityIterator(this.jpaResult.getResult(JPAExpandResult.ROOT_RESULT_KEY), converter, getFirstResult());
  }

  @Override
  public boolean hasResults() {
    return !jpaResult.getResult(JPAExpandResult.ROOT_RESULT_KEY).isEmpty();
  }

  @Override
  public boolean hasSingleResult() {
    return jpaResult.getResult(JPAExpandResult.ROOT_RESULT_KEY).size() == 1;
  }

  @Override
  public Entity getFirstResult() {
    try {
      return firstEntity.get().orElse(null);
    } catch (final ODataJPAModelException e) {
      throw new IllegalStateException(e);
    }
  }

  private Entity createFirstResult() {
    if (!jpaResult.getResult(JPAExpandResult.ROOT_RESULT_KEY).isEmpty()) {
      try {
        return converter.convertRow(jpaEntity, jpaResult.getResult(JPAExpandResult.ROOT_RESULT_KEY).set(0, null),
            jpaResult.getRequestedSelection(), List
                .of(), (jpaResult));
      } catch (final ODataApplicationException e) {
        throw new IllegalStateException(e);
      }
    }
    return null;
  }

  private class EntityIterator implements Iterator<Entity> {
    private int cursor; // index of next element to return
    private final int size;
    private final List<Tuple> entities;
    private final JPARowConverter converter;
    private final Optional<Entity> firstResult;

    // prevent creating a synthetic constructor
    private EntityIterator(@Nonnull final List<Tuple> results, @Nonnull final JPARowConverter converter,
        @Nullable final Entity firstResult) {
      this.entities = results;
      this.size = results.size();
      this.converter = converter;
      this.firstResult = Optional.ofNullable(firstResult);
    }

    @Override
    public boolean hasNext() {
      return firstResult.isPresent() && cursor != size;
    }

    @Override
    public Entity next() {
      final int i = cursor;
      if (i >= size)
        throw new NoSuchElementException();
      cursor = i + 1;
      if (i == 0)
        return firstResult.get();
      final var row = entities.set(i, null);
      try {
        return converter.convertRow(jpaEntity, row, ((JPAExpandQueryResult) jpaResult).getRequestedSelection(), List
            .of(), (jpaResult));
      } catch (final ODataApplicationException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    return super.equals(other);
  }
}
