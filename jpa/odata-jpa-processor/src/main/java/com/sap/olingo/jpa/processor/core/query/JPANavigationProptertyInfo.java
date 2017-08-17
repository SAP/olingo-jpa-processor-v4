package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;

public final class JPANavigationProptertyInfo {
  private final UriResourcePartTyped navigationTarget;
  private final JPAAssociationPath associationPath;
  private final List<UriParameter> keyPredicates;
  private final VisitableExpression expression;

  public JPANavigationProptertyInfo(final UriResourcePartTyped uriResource, final JPAAssociationPath associationPath,
      VisitableExpression expression) throws ODataApplicationException {
    super();
    this.navigationTarget = uriResource;
    this.associationPath = associationPath;
    this.keyPredicates = Util.determineKeyPredicates(uriResource);
    this.expression = expression; // uriResource.getFilterOption().getExpression();
  }

  public JPANavigationProptertyInfo(final UriResourcePartTyped uriResiource, final JPAAssociationPath associationPath,
      final List<UriParameter> keyPredicates, VisitableExpression expression) {
    super();
    this.navigationTarget = uriResiource;
    this.associationPath = associationPath;
    this.keyPredicates = keyPredicates;
    this.expression = expression;
  }

  public UriResourcePartTyped getUriResiource() {
    return navigationTarget;
  }

  public JPAAssociationPath getAssociationPath() {
    return associationPath;
  }

  List<UriParameter> getKeyPredicates() {
    return keyPredicates;
  }

  VisitableExpression getExpression() {
    return expression;
  }

}
