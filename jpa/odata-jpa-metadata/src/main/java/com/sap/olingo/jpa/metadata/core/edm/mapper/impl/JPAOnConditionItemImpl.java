package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

final class JPAOnConditionItemImpl implements JPAOnConditionItem {
  private final JPAPath jpaLeftAttribute;
  private final JPAPath jpaRightAttribute;

  JPAOnConditionItemImpl(final JPAPath jpaLeftAttribute, final JPAPath jpaRightAttribute)
      throws ODataJPAModelException {
    super();
    if (jpaLeftAttribute == null) {
      throw new ODataJPAModelException(MessageKeys.ON_LEFT_ATTRIBUTE_NULL);
    }
    if (jpaRightAttribute == null) {
      throw new ODataJPAModelException(MessageKeys.ON_RIGHT_ATTRIBUTE_NULL);
    }
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

  @Override
  public String toString() {
    return "JPAOnConditionItem [LeftAttribute=" + jpaLeftAttribute + ", RightAttribute=" + jpaRightAttribute + "]";
  }

}
