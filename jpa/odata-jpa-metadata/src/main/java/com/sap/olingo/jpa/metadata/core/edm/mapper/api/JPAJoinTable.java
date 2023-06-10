package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAJoinTable {
  /**
   * Returns the name of the join table including the schema name, using the following pattern: {schema}.{table}
   * @return
   */
  public String getTableName();

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

  List<JPAPath> getRightColumnsList() throws ODataJPAModelException;

  List<JPAPath> getLeftColumnsList() throws ODataJPAModelException;

}
