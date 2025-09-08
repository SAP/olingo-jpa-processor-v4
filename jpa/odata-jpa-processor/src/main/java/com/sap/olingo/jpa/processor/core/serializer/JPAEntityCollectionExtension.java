package com.sap.olingo.jpa.processor.core.serializer;

import java.net.URI;
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;

public interface JPAEntityCollectionExtension extends Iterable<Entity> {

  boolean hasResults();

  Entity getFirstResult();

  void setNext(final URI nextLink);

  URI getNext();

  void setCount(final Integer count);

  Integer getCount();

  List<Entity> getEntities();

  boolean hasSingleResult();
}