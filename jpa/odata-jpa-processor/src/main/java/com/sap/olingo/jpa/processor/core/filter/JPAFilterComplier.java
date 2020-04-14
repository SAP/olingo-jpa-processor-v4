package com.sap.olingo.jpa.processor.core.filter;

import java.util.List;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

public interface JPAFilterComplier {

  Expression<Boolean> compile() throws ExpressionVisitException, ODataApplicationException;

  /**
   * Returns a list of all filter elements of type Member. This could be used e.g. to determine if a join is required
   * @throws ODataApplicationException
   */
  List<JPAPath> getMember() throws ODataApplicationException;

}