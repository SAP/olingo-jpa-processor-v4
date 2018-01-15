package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;

public final class JPAExpandItemInfoFactory {

  public List<JPAExpandItemInfo> buildExpandItemInfo(JPAServiceDocument sd, UriInfoResource uriResourceInfo,
      List<JPANavigationProptertyInfo> grandParentHops) throws ODataApplicationException {

    final List<JPAExpandItemInfo> itemList = new ArrayList<>();
    final List<UriResource> startResourceList = uriResourceInfo.getUriResourceParts();
    final ExpandOption expandOption = uriResourceInfo.getExpandOption();

    if (startResourceList != null && expandOption != null) {
      final List<JPANavigationProptertyInfo> parentHops = grandParentHops;
      final Map<JPAExpandItem, JPAAssociationPath> expandPath = Util.determineAssoziations(sd, startResourceList,
          expandOption);
      for (final Entry<JPAExpandItem, JPAAssociationPath> item : expandPath.entrySet()) {
        itemList.add(new JPAExpandItemInfo(sd, item.getKey(), item.getValue(), parentHops));
      }
    }
    return itemList;
  }

}
