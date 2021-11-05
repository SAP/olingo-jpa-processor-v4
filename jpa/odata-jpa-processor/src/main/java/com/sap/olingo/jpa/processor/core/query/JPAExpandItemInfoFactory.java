package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.UriResourceSingleton;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public final class JPAExpandItemInfoFactory {

  private static final int ST_INDEX = 0;
  private static final int ET_INDEX = 1;
  private static final int PROPERTY_INDEX = 2;
  private static final int PATH_INDEX = 3;

  public List<JPAExpandItemInfo> buildExpandItemInfo(final JPAServiceDocument sd, final UriInfoResource uriResourceInfo,
      final List<JPANavigationPropertyInfo> grandParentHops) throws ODataApplicationException {

    final List<JPAExpandItemInfo> itemList = new ArrayList<>();
    final List<UriResource> startResourceList = uriResourceInfo.getUriResourceParts();
    final ExpandOption expandOption = uriResourceInfo.getExpandOption();

    if (startResourceList != null && expandOption != null) {
      final List<JPANavigationPropertyInfo> parentHops = grandParentHops;
      final Map<JPAExpandItem, JPAAssociationPath> expandPath = Util.determineAssociations(sd, startResourceList,
          expandOption);
      for (final Entry<JPAExpandItem, JPAAssociationPath> item : expandPath.entrySet()) {
        itemList.add(new JPAExpandItemInfo(sd, item.getKey(), item.getValue(), parentHops));
      }
    }
    return itemList;
  }

  /**
   * Navigate to collection property e.g.<br>
   * ../Organizations('1')/Comment or<br>
   * ../CollectionDeeps?$select=FirstLevel/SecondLevel or<br>
   * ../CollectionDeeps/FirstLevel
   * @param sd
   * @param uriResourceInfo
   * @param optional
   * @param parentHops
   * @return
   * @throws ODataApplicationException
   */
  public List<JPACollectionItemInfo> buildCollectionItemInfo(final JPAServiceDocument sd,
      final UriInfoResource uriResourceInfo, final List<JPANavigationPropertyInfo> grandParentHops,
      final Optional<JPAODataGroupProvider> groups) throws ODataApplicationException {

    final List<JPACollectionItemInfo> itemList = new ArrayList<>();
    final List<UriResource> startResourceList = uriResourceInfo.getUriResourceParts();
    final SelectOption select = uriResourceInfo.getSelectOption();
    final JPAEntityType et = uriResourceInfo instanceof JPAExpandItem ? ((JPAExpandItem) uriResourceInfo)
        .getEntityType() : null;

    final Object[] pathInfo = determineNavigationElements(sd, startResourceList, et);
    try {
      if (pathInfo[PROPERTY_INDEX] != null) {
        if (((JPAPath) pathInfo[PROPERTY_INDEX]).getLeaf().isCollection()) {
          // BusinessPartnerRoles(BusinessPartnerID='1',RoleCategory='A')/Organization/Comment
          // Organizations('1')/Comment
          // Persons('99')/InhouseAddress
          // Persons('99')/InhouseAddress?$filter=TaskID eq 'DEV'
          // Moved
        }
      } else {

        if (SelectOptionUtil.selectAll(select)) {
          // No navigation, extract all collection attributes
          final JPAStructuredType st = (JPAStructuredType) pathInfo[ST_INDEX];
          final Set<JPAElement> collectionProperties = new HashSet<>();
          for (final JPAPath path : st.getPathList()) {
            final StringBuilder pathName = new StringBuilder(pathInfo[PATH_INDEX].toString());
            for (final JPAElement pathElement : path.getPath()) {
              pathName.append(pathElement.getExternalName()).append(JPAPath.PATH_SEPARATOR);
              if (pathElement instanceof JPAAttribute && ((JPAAttribute) pathElement).isCollection()) {
                if (path.isPartOfGroups(groups.isPresent() ? groups.get().getGroups() : new ArrayList<>(0))
                    && !((JPAAttribute) pathElement).isTransient()) {
                  final JPAPath collectionPath = ((JPAEntityType) pathInfo[ET_INDEX])
                      .getPath(pathName.deleteCharAt(pathName.length() - 1).toString());
                  collectionProperties.add(collectionPath.getLeaf());
                }
                break;
              }
            }
          }
          for (final JPAElement pathElement : collectionProperties) {
            final JPACollectionExpandWrapper item = new JPACollectionExpandWrapper((JPAEntityType) pathInfo[ET_INDEX],
                uriResourceInfo);
            itemList.add(new JPACollectionItemInfo(sd, item, ((JPACollectionAttribute) pathElement)
                .asAssociation(), grandParentHops));
          }
        } else {
          final JPAStructuredType st = (JPAStructuredType) pathInfo[ST_INDEX];
          final Set<JPAPath> selectOptions = getCollectionAttributesFromSelection(st, uriResourceInfo
              .getSelectOption());
          for (final JPAPath path : selectOptions) {
            final JPACollectionExpandWrapper item = new JPACollectionExpandWrapper((JPAEntityType) pathInfo[ET_INDEX],
                uriResourceInfo);
            itemList.add(new JPACollectionItemInfo(sd, item, ((JPACollectionAttribute) path.getLeaf())
                .asAssociation(), grandParentHops));
          }
        }
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    return itemList;
  }

  private Object[] determineNavigationElements(final JPAServiceDocument sd,
      final List<UriResource> startResourceList, final JPAEntityType et) throws ODataJPAQueryException {

    StringBuilder path = new StringBuilder();
    final Object[] result = new Object[4];
    if (startResourceList.isEmpty() && et != null) {
      result[ST_INDEX] = result[ET_INDEX] = et;
    } else {
      for (final UriResource uriElement : startResourceList) {
        try {
          if (uriElement instanceof UriResourceEntitySet || uriElement instanceof UriResourceSingleton
              || uriElement instanceof UriResourceNavigation) {
            result[ST_INDEX] = result[ET_INDEX] = sd.getEntity(((UriResourcePartTyped) uriElement)
                .getType());
            path = new StringBuilder(); // Reset path on switch between entities
          } else if (uriElement instanceof UriResourceComplexProperty
              && !((UriResourceProperty) uriElement).isCollection()) {
            result[ST_INDEX] = sd.getComplexType(((UriResourceComplexProperty) uriElement).getComplexType());
            path.append(((UriResourceComplexProperty) uriElement).getProperty().getName())
                .append(JPAPath.PATH_SEPARATOR);
          } else if (uriElement instanceof UriResourceProperty
              && result[ST_INDEX] != null) {
            result[PROPERTY_INDEX] = ((JPAStructuredType) result[ST_INDEX]).getPath(((UriResourceProperty) uriElement)
                .getProperty().getName());
          }
        } catch (final ODataJPAModelException e) {
          throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
      }
    }
    result[PATH_INDEX] = path;
    return result;
  }

  protected Set<JPAPath> getCollectionAttributesFromSelection(final JPAStructuredType jpaEntity,
      final SelectOption select) throws ODataApplicationException, ODataJPAModelException {

    final Set<JPAPath> collectionAttributes = new HashSet<>();
    if (SelectOptionUtil.selectAll(select)) {
      collectionAttributes.addAll(jpaEntity.getCollectionAttributesPath());
    } else {
      final String pathPrefix = "";
      for (final SelectItem sItem : select.getSelectItems()) {
        final JPAPath selectItemPath = SelectOptionUtil.selectItemAsPath(jpaEntity, pathPrefix, sItem);
        if (selectItemPath.getLeaf().isComplex() && !selectItemPath.getLeaf().isCollection()) {
          for (final JPAPath selectSubItemPath : selectItemPath.getLeaf().getStructuredType().getPathList()) {
            if (pathContainsCollection(selectSubItemPath))
              collectionAttributes.add(getCollection(jpaEntity, selectSubItemPath, selectItemPath.getPath().get(0)
                  .getExternalName()));
          }
        } else if (pathContainsCollection(selectItemPath)) {
          collectionAttributes.add(selectItemPath);
        }
      }
    }
    return collectionAttributes;
  }

  private JPAPath getCollection(final JPAStructuredType jpaEntity, final JPAPath p, final String prefix)
      throws ODataJPAModelException {

    final StringBuilder pathAlias = new StringBuilder(prefix);
    for (final JPAElement pathElement : p.getPath()) {
      pathAlias.append(JPAPath.PATH_SEPARATOR);
      pathAlias.append(pathElement.getExternalName());
      if (pathElement instanceof JPAAttribute
          && ((JPAAttribute) pathElement).isCollection()
          && !((JPAAttribute) pathElement).isTransient()) {
        return jpaEntity.getPath(pathAlias.toString());
      }
    }
    return null;
  }

  private boolean pathContainsCollection(final JPAPath p) throws ODataJPAModelException {
    for (final JPAElement pathElement : p.getPath()) {
      if (pathElement instanceof JPAAttribute
          && ((JPAAttribute) pathElement).isCollection()
          && !((JPAAttribute) pathElement).isTransient()) {
        return true;
      }
    }
    return false;
  }
}
