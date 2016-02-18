package org.apache.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;

public class JPAExpandItemInfoFactory {
  public List<JPAExpandItemInfo> buildExpandItemInfo(final ServicDocument sd, final List<UriResource> startResourceList,
      final ExpandOption expandOption, final List<JPANavigationProptertyInfo> grandParentHops)
          throws ODataApplicationException {

    // TODO $expand=*
    final List<JPAExpandItemInfo> itemList = new ArrayList<JPAExpandItemInfo>();
    final StringBuffer associationName = new StringBuffer();
    UriResource startResourceItem = null;

    List<JPANavigationProptertyInfo> parentHops = new ArrayList<JPANavigationProptertyInfo>();
    if (grandParentHops != null) {
      parentHops.addAll(grandParentHops);
      parentHops.addAll(Util.determineAssoziations(sd, startResourceList));
    } else
      parentHops = Util.determineAssoziations(sd, startResourceList);
    if (startResourceList != null && expandOption != null) {
      for (int i = startResourceList.size() - 1; i >= 0; i--) {
        startResourceItem = startResourceList.get(i);
        if (startResourceItem instanceof UriResourceEntitySet || startResourceItem instanceof UriResourceNavigation) {
          break;
        }
        associationName.insert(0, JPAAssociationPath.PATH_SEPERATOR);
        associationName.insert(0, ((UriResourceProperty) startResourceItem).getProperty().getName());
      }

      // Example1 : ?$expand=Created/User (Property/NavigationProperty)
      // Example2 : ?$expand=Parent/CodeID (NavigationProperty/Property)
      for (final ExpandItem item : expandOption.getExpandItems()) {
        final List<UriResource> targetResourceList = item.getResourcePath().getUriResourceParts();
        UriResource targetResourceItem = null;
        for (int i = 0; i < targetResourceList.size(); i++) {
          targetResourceItem = targetResourceList.get(i);
          if (targetResourceItem.getKind() != UriResourceKind.navigationProperty) {
            // if (i < targetResourceList.size() - 1) {
            associationName.append(((UriResourceProperty) targetResourceItem).getProperty().getName());
            associationName.append(JPAAssociationPath.PATH_SEPERATOR);
          } else {
            associationName.append(((UriResourceNavigation) targetResourceItem).getProperty().getName());
            break;
          }
        }
        itemList.add(new JPAExpandItemInfo(
            new JPAExpandItemWrapper(item),
            (UriResourcePartTyped) startResourceItem,
            Util.determineAssoziation(sd, ((UriResourcePartTyped) startResourceItem).getType(), associationName),
            parentHops));
      }
    }
    return itemList;
  }
}
