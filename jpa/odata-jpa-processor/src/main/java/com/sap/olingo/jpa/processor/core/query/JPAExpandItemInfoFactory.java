package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.ServiceDocument;

public class JPAExpandItemInfoFactory {
//  public List<JPAExpandItemInfo> buildExpandItemInfo(final ServiceDocument sd, final List<UriResource> startResourceList,
//      final ExpandOption expandOption, final List<JPANavigationProptertyInfo> grandParentHops)
//      throws ODataApplicationException {
  public List<JPAExpandItemInfo> buildExpandItemInfo(ServiceDocument sd, UriInfoResource uriResourceInfo,
      List<JPANavigationProptertyInfo> grandParentHops) throws ODataApplicationException {

    final List<JPAExpandItemInfo> itemList = new ArrayList<JPAExpandItemInfo>();
    final List<UriResource> startResourceList = uriResourceInfo.getUriResourceParts();
    final ExpandOption expandOption = uriResourceInfo.getExpandOption();
    final Expression filterExpression;
    if (uriResourceInfo.getFilterOption() != null)
      filterExpression = uriResourceInfo.getFilterOption().getExpression();
    else
      filterExpression = null;

    if (startResourceList != null && expandOption != null) {
      final List<JPANavigationProptertyInfo> parentHops = determineParentHops(sd, startResourceList, grandParentHops);
      final UriResource startResourceItem = determineStartResourceItem(startResourceList);
      final Map<JPAExpandItem, JPAAssociationPath> expandPath = Util.determineAssoziations(sd, startResourceList,
          expandOption);
      for (final JPAExpandItem item : expandPath.keySet()) {
        itemList.add(new JPAExpandItemInfo(item, (UriResourcePartTyped) startResourceItem, filterExpression,
            expandPath.get(item), parentHops));
      }
    }
    return itemList;
  }

  private UriResource determineStartResourceItem(final List<UriResource> startResourceList) {
    UriResource startResourceItem = null;
    for (int i = startResourceList.size() - 1; i >= 0; i--) {
      startResourceItem = startResourceList.get(i);
      if (startResourceItem instanceof UriResourceEntitySet || startResourceItem instanceof UriResourceNavigation) {
        break;
      }
    }
    return startResourceItem;
  }

  private List<JPANavigationProptertyInfo> determineParentHops(final ServiceDocument sd,
      final List<UriResource> startResourceList, final List<JPANavigationProptertyInfo> grandParentHops)
      throws ODataApplicationException {
    List<JPANavigationProptertyInfo> parentHops = new ArrayList<JPANavigationProptertyInfo>();

    if (grandParentHops != null) {
      parentHops.addAll(grandParentHops);
      parentHops.addAll(Util.determineAssoziations(sd, startResourceList));
    } else
      parentHops = Util.determineAssoziations(sd, startResourceList);
    return parentHops;
  }
}
