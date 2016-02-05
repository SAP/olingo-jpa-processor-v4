package org.apache.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAEntityType extends JPAStructuredType {

  public List<? extends JPAAttribute> getKey() throws ODataJPAModelException;

  public List<JPAPath> searchChildPath(JPAPath selectItemPath);

  public Class<?> getKeyType();

}
