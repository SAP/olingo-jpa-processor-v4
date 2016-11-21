package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

//TODO remove extension
public interface JPAAssociationAttribute extends JPAAttribute {

//  List<JPAOnConditionItem> getJoinColumns() throws ODataJPAModelException;

  public JPAStructuredType getTargetEntity() throws ODataJPAModelException;

  public boolean isCollection();

}
