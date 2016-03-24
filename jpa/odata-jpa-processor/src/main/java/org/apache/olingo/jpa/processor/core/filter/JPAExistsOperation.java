package org.apache.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.query.JPAAbstractQuery;
import org.apache.olingo.jpa.processor.core.query.JPANavigationProptertyInfo;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;

abstract class JPAExistsOperation implements JPAOperator {

  protected final JPAOperationConverter converter;
  protected final List<UriResource> uriResourceParts;
  protected final JPAAbstractQuery root;
  protected final ServicDocument sd;
  protected final EntityManager em;
  protected final OData odata;

  JPAExistsOperation(final OData odata, final ServicDocument sd, final EntityManager em,
      final List<UriResource> uriResourceParts, final JPAOperationConverter converter, final JPAAbstractQuery root) {
    this.uriResourceParts = uriResourceParts;
    this.root = root;
    this.sd = sd;
    this.em = em;
    this.converter = converter;
    this.odata = odata;
  }

  public static boolean hasNavigation(final List<UriResource> uriResourceParts) {
    if (uriResourceParts != null) {
      for (int i = uriResourceParts.size() - 1; i >= 0; i--) {
        if (uriResourceParts.get(i) instanceof UriResourceNavigation)
          return true;
      }
    }
    return false;
  }

  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    return converter.convert(this);
  }

  abstract Subquery<?> getExistsQuery() throws ODataApplicationException;

  protected List<JPANavigationProptertyInfo> determineAssoziations(final ServicDocument sd,
      final List<UriResource> resourceParts) throws ODataApplicationException {
    final List<JPANavigationProptertyInfo> pathList = new ArrayList<JPANavigationProptertyInfo>();

    StringBuffer associationName = null;
    UriResourceNavigation navigation = null;
    if (resourceParts != null && hasNavigation(resourceParts)) {
      // for (int i = 0; i < resourceParts.size(); i++) {
      for (int i = resourceParts.size() - 1; i >= 0; i--) {
        final UriResource resourcePart = resourceParts.get(i);
        if (resourcePart instanceof UriResourceNavigation) {
          if (navigation != null)
            pathList.add(new JPANavigationProptertyInfo(navigation,
                Util.determineAssoziationPath(sd, ((UriResourcePartTyped) resourceParts.get(i)), associationName)));
          navigation = (UriResourceNavigation) resourceParts.get(i);
          associationName = new StringBuffer();
          associationName.insert(0, navigation.getProperty().getName());
        }
        if (navigation != null) {
          if (resourceParts.get(i) instanceof UriResourceComplexProperty) {
            associationName.insert(0, JPAPath.PATH_SEPERATOR);
            associationName.insert(0, ((UriResourceComplexProperty) resourceParts.get(i)).getProperty().getName());
          }
          if (resourcePart instanceof UriResourceEntitySet)
            pathList.add(new JPANavigationProptertyInfo(navigation,
                Util.determineAssoziationPath(sd, ((UriResourcePartTyped) resourceParts.get(i)), associationName)));
        }
      }
    }
    return pathList;
  }
}