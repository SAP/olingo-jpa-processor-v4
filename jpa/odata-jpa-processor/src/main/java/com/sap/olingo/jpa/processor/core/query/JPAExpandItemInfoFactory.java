package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

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

  /**
   * Navigate to collection property e.g. ../Organizations('1')/Comment
   * @param sd
   * @param uriResourceInfo
   * @param parentHops
   * @return
   * @throws ODataApplicationException
   */
  public List<JPAExpandItemInfo> buildCollectionItemInfo(final JPAServiceDocument sd,
      final UriInfoResource uriResourceInfo,
      final List<JPANavigationProptertyInfo> grandParentHops) throws ODataApplicationException {

    final List<JPAExpandItemInfo> itemList = new ArrayList<>();
    final List<UriResource> startResourceList = uriResourceInfo.getUriResourceParts();

    if (startResourceList != null) {
      JPAStructuredType st = null;
      JPAEntityType et = null;
      for (UriResource uriElement : startResourceList) {
        try {
          if (uriElement instanceof UriResourceEntitySet) {
            st = et = sd.getEntity(((UriResourceEntitySet) uriElement).getEntityType());
          } else if (uriElement instanceof UriResourceProperty
              && ((UriResourceProperty) uriElement).isCollection()
              && st != null) {
            // TODO Complex types
            JPAPath pathToCollection = st.getPath(((UriResourceProperty) uriElement).getProperty().getName());
            JPACollectionExpandWrapper item = new JPACollectionExpandWrapper(et, uriResourceInfo);
            itemList.add(new JPAExpandItemInfo(sd, item, ((JPACollectionAttribute) pathToCollection.getLeaf())
                .asAssociation(), grandParentHops));
          }
        } catch (ODataJPAModelException e) {
          throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
      }
    }
    return itemList;
  }

}
