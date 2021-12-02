package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAJoinTable {

  public String getTableName();

  public String getAlias(String dbFieldName);

  public String getInverseAlias(String dbFieldName);

  public JPAEntityType getEntityType();

  public List<JPAOnConditionItem> getJoinColumns() throws ODataJPAModelException;

  /**
   * Returns the list of inverse join columns with exchanged left/right order.
   * @return
   * @throws ODataJPAModelException
   */
  public List<JPAOnConditionItem> getInverseJoinColumns() throws ODataJPAModelException;

  public <T extends JPAJoinColumn> List<T> getRawJoinInformation();

  public <T extends JPAJoinColumn> List<T> getRawInverseJoinInformation() throws ODataJPAModelException;
}
