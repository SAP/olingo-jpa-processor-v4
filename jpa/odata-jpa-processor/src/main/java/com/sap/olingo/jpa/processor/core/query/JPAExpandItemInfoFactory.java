package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ATTRIBUTE_NOT_FOUND;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.olingo.commons.api.ex.ODataException;
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
import com.sap.olingo.jpa.processor.core.api.JPAODataExpandPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public final class JPAExpandItemInfoFactory {
  private static final int ST_INDEX = 0;
  private static final int ET_INDEX = 1;
  private static final int PROPERTY_INDEX = 2;
  private static final int PATH_INDEX = 3;
  private final JPAODataRequestContextAccess requestContext;

  public JPAExpandItemInfoFactory(final JPAODataRequestContextAccess requestContext) {
    this.requestContext = requestContext;
  }

  public List<JPAExpandItemInfo> buildExpandItemInfo(final JPAServiceDocument sd, final UriInfoResource uriResourceInfo,
      final List<JPANavigationPropertyInfo> grandParentHops, final Optional<JPAKeyBoundary> keyBoundary,
      final JPAExpandQueryFactory factory) throws ODataException {

    final List<JPAExpandItemInfo> itemList = new ArrayList<>();
    final List<UriResource> startResourceList = uriResourceInfo.getUriResourceParts();
    final ExpandOption expandOption = uriResourceInfo.getExpandOption();
    // ((UriResourceNavigation)
    // uriResourceInfo.getExpandOption().getExpandItems().get(0).getResourcePath().getUriResourceParts().get(0)).getTypeFilterOnEntry()
    if (startResourceList != null && expandOption != null) {
      final List<JPANavigationPropertyInfo> parentHops = grandParentHops;
      final Map<JPAExpandItem, JPAAssociationPath> expandPath = Utility.determineAssociations(sd, startResourceList,
          expandOption);
      for (final Entry<JPAExpandItem, JPAAssociationPath> item : expandPath.entrySet()) {
        final var expandItem = new JPAExpandItemInfo(sd, item.getKey(), item.getValue(), parentHops);
        final var count = factory.createCountQuery(expandItem, keyBoundary);
        final var provider = requestContext.getPagingProvider();
        final Optional<JPAODataExpandPage> page;
        if (provider.isPresent())
          page = provider.get().getFirstPageExpand(requestContext.getRequestParameter(),
              requestContext.getPathInformation(), requestContext.getUriInfo(), item.getKey().getTopOption(), item
                  .getKey().getSkipOption(),
              item.getValue().getLeaf(), count, requestContext.getEntityManager());
        else
          page = Optional.empty();
        itemList.add(new JPAExpandItemInfo(expandItem, page));
      }
    }
    return itemList;
  }

  /**
   * Navigate to collection property e.g.<br>
   * ../Organizations('1')/Comment or<br>
   * ../CollectionDeeps?$select=FirstLevel/SecondLevel or<br>
   * ../CollectionDeeps/FirstLevel
   * ../CollectionDeeps/FirstLevel/SecondLevel?$select=...,...
   *
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
    final JPAEntityType et = uriResourceInfo instanceof final JPAExpandItem expandItem
        ? expandItem.getEntityType() : null;

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
        // Organizations('1')?$select=Comment
        // Et/St?$select=Cp
        if (SelectOptionUtil.selectAll(select)) {
          // No navigation, extract all collection attributes
          final JPAStructuredType st = (JPAStructuredType) pathInfo[ST_INDEX];
          final Set<JPACollectionAttribute> collectionProperties = new HashSet<>();
          for (final JPAPath path : st.getPathList()) {
            final StringBuilder pathName = new StringBuilder(pathInfo[PATH_INDEX].toString());
            for (final JPAElement pathElement : path.getPath()) {
              pathName.append(pathElement.getExternalName()).append(JPAPath.PATH_SEPARATOR);
              if (pathElement instanceof final JPAAttribute attribute && attribute.isCollection()) {
                if (path.isPartOfGroups(groups.isPresent() ? groups.get().getGroups() : new ArrayList<>(0))
                    && !attribute.isTransient()) {
                  collectionProperties.add(getCollectionPath(pathInfo, pathName));
                }
                break;
              }
            }
          }
          for (final var pathElement : collectionProperties) {
            final JPACollectionExpandWrapper item = new JPACollectionExpandWrapper((JPAEntityType) pathInfo[ET_INDEX],
                uriResourceInfo);
            itemList.add(new JPACollectionItemInfo(sd, item, pathElement.asAssociation(), grandParentHops));
          }
        } else {
          // Et?$select=Cp
          // Et/St?$select=Cp,Cp
          // Et/St/St?$select=Cp
          // Et/St?$select=St/Cp
          final JPAStructuredType st = (JPAStructuredType) pathInfo[ST_INDEX];
          final Set<JPAPath> selectOptions = getCollectionAttributesFromSelection(st, uriResourceInfo
              .getSelectOption());
          final Map<JPAPath, JPAAssociationPath> collectionPaths = Utility.determineAssociations(sd,
              startResourceList, selectOptions);
          for (final JPAAssociationPath path : collectionPaths.values()) {
            final JPACollectionExpandWrapper item = new JPACollectionExpandWrapper((JPAEntityType) pathInfo[ET_INDEX],
                uriResourceInfo, path);
            itemList.add(new JPACollectionItemInfo(sd, item, path, grandParentHops));
          }
        }
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    return itemList;
  }

  private JPACollectionAttribute getCollectionPath(final Object[] pathInfo, final StringBuilder pathName)
      throws ODataJPAModelException, ODataJPAProcessorException {

    final var name = pathName.deleteCharAt(pathName.length() - 1).toString();
    final JPAPath collectionPath = ((JPAEntityType) pathInfo[ET_INDEX]).getPath(name);
    if (collectionPath != null)
      return (JPACollectionAttribute) collectionPath.getLeaf();
    else
      throw new ODataJPAProcessorException(ATTRIBUTE_NOT_FOUND, HttpStatusCode.INTERNAL_SERVER_ERROR, name);
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
          } else if (uriElement instanceof final UriResourceComplexProperty complexProperty
              && !complexProperty.isCollection()) {
            result[ST_INDEX] = sd.getComplexType(complexProperty.getComplexType());
            path.append(complexProperty.getProperty().getName())
                .append(JPAPath.PATH_SEPARATOR);
          } else if (uriElement instanceof final UriResourceProperty resourceProperty
              && result[ST_INDEX] != null) {
            result[PROPERTY_INDEX] = ((JPAStructuredType) result[ST_INDEX]).getPath(resourceProperty
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

  private Set<JPAPath> getCollectionAttributesFromSelection(final JPAStructuredType jpaEntity,
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

  private JPAPath getCollection(final JPAStructuredType jpaEntity, final JPAPath path, final String prefix)
      throws ODataJPAModelException {

    final StringBuilder pathAlias = new StringBuilder(prefix);
    for (final JPAElement pathElement : path.getPath()) {
      pathAlias.append(JPAPath.PATH_SEPARATOR);
      pathAlias.append(pathElement.getExternalName());
      if (pathElement instanceof final JPAAttribute attribute
          && attribute.isCollection()
          && !attribute.isTransient()) {
        return jpaEntity.getPath(pathAlias.toString());
      }
    }
    return null;
  }

  private boolean pathContainsCollection(final JPAPath path) {
    for (final JPAElement pathElement : path.getPath()) {
      if (pathElement instanceof final JPAAttribute attribute
          && attribute.isCollection()
          && !attribute.isTransient()) {
        return true;
      }
    }
    return false;
  }
}
