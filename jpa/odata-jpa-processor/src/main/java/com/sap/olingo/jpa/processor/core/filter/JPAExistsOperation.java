package com.sap.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceLambdaVariable;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractQuery;
import com.sap.olingo.jpa.processor.core.query.JPANavigationPropertyInfo;
import com.sap.olingo.jpa.processor.core.query.Utility;

abstract class JPAExistsOperation implements JPAExpressionOperator {

  protected final JPAOperationConverter converter;
  protected final List<UriResource> uriResourceParts;
  protected final JPAAbstractQuery root;
  protected final JPAServiceDocument sd;
  protected final EntityManager em;
  protected final OData odata;
  protected final From<?, ?> from;
  protected final Optional<JPAODataClaimProvider> claimsProvider;
  protected final List<String> groups;

  JPAExistsOperation(final JPAFilterComplierAccess jpaComplier) {

    this.uriResourceParts = jpaComplier.getUriResourceParts();
    this.root = jpaComplier.getParent();
    this.sd = jpaComplier.getSd();
    this.em = jpaComplier.getEntityManager();
    this.converter = jpaComplier.getConverter();
    this.odata = jpaComplier.getOData();
    this.from = jpaComplier.getRoot();
    this.claimsProvider = jpaComplier.getClaimsProvider();
    this.groups = jpaComplier.getGroups();
  }

  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    try {
      return converter.cb.exists(getExistsQuery().query());
    } catch (final ODataJPAIllegalAccessException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  abstract SubQueryItem getExistsQuery() throws ODataApplicationException, ODataJPAIllegalAccessException;

  protected List<JPANavigationPropertyInfo> determineAssociations(final JPAServiceDocument sd,
      final List<UriResource> resourceParts) throws ODataApplicationException {
    final List<JPANavigationPropertyInfo> pathList = new ArrayList<>();

    StringBuilder associationName = null;
    UriResourcePartTyped navigation = null;
    if (Utility.hasNavigation(resourceParts) || Utility.hasCollection(resourceParts)) {
      for (int i = resourceParts.size() - 1; i >= 0; i--) {
        final UriResource resourcePart = resourceParts.get(i);
        if (resourcePart instanceof final UriResourceNavigation nextNavigation) {
          if (navigation != null)
            pathList.add(new JPANavigationPropertyInfo(sd, navigation, Utility.determineAssociationPath(sd,
                nextNavigation, associationName), null));
          navigation = nextNavigation;
          associationName = new StringBuilder();
          associationName.insert(0, nextNavigation.getProperty().getName());
        }
        if (navigation != null) {
          if (resourceParts.get(i) instanceof final UriResourceComplexProperty complexProperty) {
            associationName.insert(0, JPAPath.PATH_SEPARATOR);
            associationName.insert(0, complexProperty.getProperty().getName());
          } else if (resourcePart instanceof final UriResourceEntitySet entitySet)
            pathList.add(new JPANavigationPropertyInfo(sd, navigation, Utility.determineAssociationPath(sd,
                entitySet, associationName), null));
          else if (resourcePart instanceof final UriResourceLambdaVariable lambdaVariable)
            pathList.add(new JPANavigationPropertyInfo(sd, navigation, Utility.determineAssociation(sd,
                lambdaVariable.getType(), associationName), null));
        }
        if (Utility.isCollection(resourcePart)) {
          navigation = (UriResourcePartTyped) resourcePart;
          associationName = new StringBuilder();
          associationName.insert(0, ((UriResourceProperty) navigation).getProperty().getName());
        }
      }
    }
    return pathList;
  }

  protected record SubQueryItem(List<Path<Comparable<?>>> jpaPath, Subquery<List<Comparable<?>>> query) {}
}