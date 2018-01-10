package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;

public class JPAExpandItemInfo {
  private final JPAExpandItem uriInfo;
  private final JPAAssociationPath expandAssociation;
  private final List<JPANavigationProptertyInfo> hops;

  JPAExpandItemInfo(final JPAServiceDocument sd, final JPAExpandItem uriInfo,
      final JPAAssociationPath expandAssociation, final List<JPANavigationProptertyInfo> hops)
      throws ODataApplicationException {

    super();
    this.uriInfo = uriInfo;
    this.expandAssociation = expandAssociation;
    this.hops = new ArrayList<>();
    for (JPANavigationProptertyInfo predecessor : hops)
      this.hops.add(new JPANavigationProptertyInfo(predecessor));
    this.hops.get(this.hops.size() - 1).setAssociationPath(expandAssociation);
    if (!uriInfo.getUriResourceParts().isEmpty())
      this.hops.addAll(Util.determineNavigationPath(sd, uriInfo.getUriResourceParts(), uriInfo));
    else
      this.hops.add(new JPANavigationProptertyInfo(sd, expandAssociation, uriInfo, uriInfo.getEntityType()));
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
