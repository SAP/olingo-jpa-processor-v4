package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

final class JPAOnConditionItemImpl implements JPAOnConditionItem {
  private final JPAPath jpaLeftAttribute;
  private final JPAPath jpaRightAttribute;

  JPAOnConditionItemImpl(@Nonnull final JPAPath jpaLeftAttribute, @Nonnull final JPAPath jpaRightAttribute) {

    super();
    Objects.requireNonNull(jpaLeftAttribute, new ODataJPAModelException(MessageKeys.ON_LEFT_ATTRIBUTE_NULL)
        .getMessage());
    Objects.requireNonNull(jpaRightAttribute, new ODataJPAModelException(MessageKeys.ON_RIGHT_ATTRIBUTE_NULL)
        .getMessage());
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
    return "JPAOnConditionItem [LeftAttribute=("
        + jpaLeftAttribute.getAlias() + "/"
        + jpaLeftAttribute.getDBFieldName() + "), RightAttribute=("
        + jpaRightAttribute.getAlias() + "/"
        + jpaRightAttribute.getDBFieldName()
        + ")]";
  }

}
