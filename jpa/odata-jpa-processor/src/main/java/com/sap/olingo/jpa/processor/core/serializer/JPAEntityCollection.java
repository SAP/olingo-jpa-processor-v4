package com.sap.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;

public class JPAEntityCollection extends EntityCollection implements JPAEntityCollectionExtension {

  @Override
  public boolean hasResults() {
    return !getEntities().isEmpty();
  }

  @Override
  public Entity getFirstResult() {
    if (hasResults())
      return getEntities().get(0);
    return null;
  }

  @Override
  public boolean hasSingleResult() {
    return getEntities().size() == 1;
  }

}
