package org.apache.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.server.api.uri.UriInfoResource;

public class JPAExpandItemInfo {
  private final UriInfoResource uriInfo;
  private final JPAAssociationPath expandAssociation;
  private final List<JPANavigationProptertyInfo> hops;

  JPAExpandItemInfo(UriInfoResource uriInfo, JPAAssociationPath expandAssociation,
      List<JPANavigationProptertyInfo> hops) {
    super();
    this.uriInfo = uriInfo;
    this.expandAssociation = expandAssociation;
    this.hops = hops;
  }

  public UriInfoResource getUriInfo() {
    return uriInfo;
  }

  public JPAAssociationPath getExpandAssociation() {
    return expandAssociation;
  }

  public List<JPANavigationProptertyInfo> getHops() {
    return hops;
  }
}
