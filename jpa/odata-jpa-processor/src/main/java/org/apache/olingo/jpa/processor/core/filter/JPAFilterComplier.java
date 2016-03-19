package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

public interface JPAFilterComplier {

  Expression<Boolean> compile() throws ExpressionVisitException, ODataApplicationException;

}