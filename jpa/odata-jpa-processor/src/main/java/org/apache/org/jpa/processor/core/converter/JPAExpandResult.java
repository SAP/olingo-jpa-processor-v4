package org.apache.org.jpa.processor.core.converter;

import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;

public interface JPAExpandResult {

  List<Tuple> getResult(String key);

  Map<JPAAssociationPath, JPAExpandResult> getChildren();

  boolean hasCount();

  Integer getCount();

  JPAEntityType getEntityType();

}