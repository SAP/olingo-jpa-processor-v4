package org.apache.olingo.jpa.processor.core.api;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;

public class Util {

  public static EdmEntitySet determineTargetEntitySet(List<UriResource> resources) {
    EdmEntitySet targetEdmEntitySet = null;
    StringBuffer naviPropertyName = new StringBuffer();

    for (UriResource resourceItem : resources) {
      if (resourceItem.getKind() == UriResourceKind.entitySet) {
        targetEdmEntitySet = ((UriResourceEntitySet) resourceItem).getEntitySet();
      }
      if (resourceItem.getKind() == UriResourceKind.complexProperty) {
        naviPropertyName.append(((UriResourceComplexProperty) resourceItem).getProperty().getName());
        naviPropertyName.append(JPAPath.PATH_SEPERATOR);
      }
      if (resourceItem.getKind() == UriResourceKind.navigationProperty) {
        naviPropertyName.append(((UriResourceNavigation) resourceItem).getProperty().getName());
        EdmBindingTarget edmBindingTarget = targetEdmEntitySet.getRelatedBindingTarget(naviPropertyName.toString());
        if (edmBindingTarget instanceof EdmEntitySet)
          targetEdmEntitySet = (EdmEntitySet) edmBindingTarget;
        naviPropertyName = new StringBuffer();
      }
    }
    return targetEdmEntitySet;
  }

  public static EdmEntityType determineTargetEntityType(List<UriResource> resources) {
    EdmEntityType targetEdmEntity = null;

    for (UriResource resourceItem : resources) {
      if (resourceItem.getKind() == UriResourceKind.navigationProperty) {
        // first try the simple way like in the example
        targetEdmEntity = (EdmEntityType) ((UriResourceNavigation) resourceItem).getType();
      }
    }
    return targetEdmEntity;
  }

  public static String determineProptertyNavigationPath(List<UriResource> resources) {
    StringBuffer pathName = new StringBuffer();
    if (resources != null) {
      for (int i = resources.size() - 1; i >= 0; i--) {
        UriResource resourceItem = resources.get(i);
        if (resourceItem instanceof UriResourceEntitySet || resourceItem instanceof UriResourceNavigation)
          break;
        UriResourceProperty property = (UriResourceProperty) resourceItem;
        pathName.insert(0, property.getProperty().getName());
        pathName.insert(0, JPAPath.PATH_SEPERATOR);
      }
      if (pathName.length() > 0)
        pathName.deleteCharAt(0);
    }
    return pathName.toString();
  }

  public static UriResourceProperty determineStartNavigationPath(List<UriResource> resources) {
    UriResourceProperty property = null;
    if (resources != null) {
      for (int i = resources.size() - 1; i >= 0; i--) {
        UriResource resourceItem = resources.get(i);
        if (resourceItem instanceof UriResourceEntitySet || resourceItem instanceof UriResourceNavigation)
          break;
        property = (UriResourceProperty) resourceItem;
      }
    }
    return property;
  }

  public static JPAAssociationPath determineAssoziation(ServicDocument sd, EdmType naviStart,
      StringBuffer associationName) throws ODataApplicationException {
    JPAEntityType naviStartType;

    try {
      naviStartType = sd.getEntity(naviStart);
      return naviStartType.getAssociationPath(associationName.toString());
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Unknown navigation property", HttpStatusCode.INTERNAL_SERVER_ERROR
          .ordinal(), Locale.ENGLISH, e);
    }
  }

  public static JPAAssociationPath determineAssoziation(ServicDocument sd, EdmType naviStart,
      EdmNavigationProperty naviTarget, List<UriResource> uriResourceParts) throws ODataApplicationException {

    return determineAssoziation(sd, naviStart, determineAssociationPathName(uriResourceParts, naviTarget));
  }

  public static StringBuffer determineAssociationPathName(List<UriResource> uriResourceParts,
      EdmNavigationProperty naviTarget) {
    StringBuffer associationName = new StringBuffer();
    for (int i = uriResourceParts.size() - 1; i > 0; i--) {
      associationName.insert(0, JPAAssociationPath.PATH_SEPERATOR);
      associationName.insert(0, ((UriResourceProperty) uriResourceParts.get(i)).getProperty()
          .getName());
    }
    associationName.append(naviTarget.getName());
    return associationName;
  }

  public static Map<ExpandItem, JPAAssociationPath> determineAssoziations(ServicDocument sd,
      List<UriResource> startResourceList,
      ExpandOption expandOption) throws ODataApplicationException {
    Map<ExpandItem, JPAAssociationPath> pathList = new HashMap<ExpandItem, JPAAssociationPath>();
    StringBuffer associationName = new StringBuffer();

    UriResource startResourceItem = null;
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
      for (ExpandItem item : expandOption.getExpandItems()) {
        List<UriResource> targetResourceList = item.getResourcePath().getUriResourceParts();
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
        pathList.put(item, Util.determineAssoziation(sd, ((UriResourcePartTyped) startResourceItem).getType(),
            associationName));
      }
    }
    return pathList;
  }
}
