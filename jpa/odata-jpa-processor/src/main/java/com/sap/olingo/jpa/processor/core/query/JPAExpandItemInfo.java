package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;

public class JPAExpandItemInfo {
  private final JPAExpandItemWrapper uriInfo;
  private final JPAAssociationPath expandAssociation;
  private final List<JPANavigationProptertyInfo> hops;

  JPAExpandItemInfo(final JPAExpandItemWrapper uriInfo, final UriResourcePartTyped startResourceItem,
      Expression filterExpression, final JPAAssociationPath expandAssociation,
      final List<JPANavigationProptertyInfo> hops)
      throws ODataApplicationException {

    super();
    this.uriInfo = uriInfo;
    this.expandAssociation = expandAssociation;
    this.hops = new ArrayList<JPANavigationProptertyInfo>(hops);
    this.hops.add(0, new JPANavigationProptertyInfo(startResourceItem, expandAssociation, filterExpression));
  }

  public UriInfoResource getUriInfo() {
    return uriInfo;
  }

  public JPAAssociationPath getExpandAssociation() {
    return expandAssociation;
  }

  public List<JPANavigationProptertyInfo> getHops() {
    return Collections.unmodifiableList(hops);
  }

  public JPAEntityType getEntityType() {
    return uriInfo.getEntityType();
  }
}
