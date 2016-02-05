package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

class JPAOnConditionItemImpl implements JPAOnConditionItem {
  private final JPAPath jpaLeftAttribute;
  private final JPAPath jpaRightAttribute;

  JPAOnConditionItemImpl(JPAPath jpaLeftAttribute, JPAPath jpaRightAttribute) {
    super();
    this.jpaLeftAttribute = jpaLeftAttribute;
    this.jpaRightAttribute = jpaRightAttribute;
  }

  @Override
  public JPAPath getLeftPath() {
    return jpaLeftAttribute;
  }

  @Override
  public JPAPath getRightPath() {
    return jpaRightAttribute;
  }

}
