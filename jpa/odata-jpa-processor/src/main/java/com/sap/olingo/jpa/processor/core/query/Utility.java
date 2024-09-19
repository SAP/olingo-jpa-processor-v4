package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath.PATH_SEPARATOR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.NOT_SUPPORTED_RESOURCE_TYPE;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAUtilException.MessageKeys.UNKNOWN_ENTITY_TYPE;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAUtilException.MessageKeys.UNKNOWN_NAVI_PROPERTY;
import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;
import static org.apache.olingo.commons.api.http.HttpStatusCode.NOT_IMPLEMENTED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmStructuredType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceLambdaVariable;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.UriResourceSingleton;
import org.apache.olingo.server.api.uri.UriResourceValue;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAUtilException;
import com.sap.olingo.jpa.processor.core.uri.JPAUriResourceNavigationImpl;

public final class Utility {

  public static final String VALUE_RESOURCE = "$VALUE";
  private static final String FOUND_CAST_FROM = "Found cast from ";
  private static final Log LOGGER = LogFactory.getLog(Utility.class);

  private Utility() {
    // suppress instance creation
  }

  public static JPAAssociationPath determineAssociation(final JPAServiceDocument sd, final EdmType navigationStart,
      final StringBuilder associationName) throws ODataApplicationException {

    try {
      final JPAEntityType navigationStartType = sd.getEntity(navigationStart);
      if (navigationStartType == null)
        throw new ODataJPAUtilException(UNKNOWN_ENTITY_TYPE, BAD_REQUEST);
      JPAAssociationPath path = navigationStartType.getAssociationPath(associationName.toString());
      if (path == null) {
        final var collection = navigationStartType.getCollectionAttribute(associationName.toString());
        if (collection != null)
          path = collection.asAssociation();
      }
      return path;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAUtilException(UNKNOWN_NAVI_PROPERTY, BAD_REQUEST, e);
    }
  }

  public static JPAAssociationPath determineAssociationPath(final JPAServiceDocument sd,
      final UriResourcePartTyped navigationStart, final StringBuilder associationName)
      throws ODataApplicationException {

    JPAEntityType navigationStartType = null;
    try {
      if (navigationStart instanceof final UriResourceEntitySet entitySet) {
        if (entitySet.getTypeFilterOnEntry() != null)
          navigationStartType = sd.getEntity(entitySet.getTypeFilterOnEntry());
        else if (entitySet.getTypeFilterOnCollection() != null)
          navigationStartType = sd.getEntity(entitySet.getTypeFilterOnCollection());
        else
          navigationStartType = sd.getEntity(entitySet.getType());
      }
      if (navigationStart instanceof final UriResourceSingleton singleton) {
        navigationStartType = sd.getEntity(singleton.getType());
      } else if (navigationStart instanceof final UriResourceNavigation navigation) {
        if (navigation.getTypeFilterOnEntry() != null)
          navigationStartType = sd.getEntity(navigation.getTypeFilterOnEntry());
        else
          navigationStartType = sd.getEntity(navigation.getProperty().getType());
      }
      JPAAssociationPath path = navigationStartType == null ? null : navigationStartType.getAssociationPath(
          associationName.toString());
      if (path == null && navigationStartType != null) {
        final JPACollectionAttribute collection = navigationStartType.getCollectionAttribute(associationName
            .toString());
        if (collection != null)
          path = collection.asAssociation();
      }
      return path;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAUtilException(UNKNOWN_NAVI_PROPERTY, BAD_REQUEST, e);
    }
  }

  public static Map<JPAExpandItem, JPAAssociationPath> determineAssociations(final JPAServiceDocument sd,
      final List<UriResource> startResourceList, final ExpandOption expandOption) throws ODataApplicationException {

    final Map<JPAExpandItem, JPAAssociationPath> pathList = new HashMap<>();
    final StringBuilder associationNamePrefix = new StringBuilder();

    if (startResourceList != null && expandOption != null) {
      final UriResource startResourceItem = createAssociationNamePrefix(startResourceList, associationNamePrefix);
      // Example1 : ?$expand=Created/User (Property/NavigationProperty)
      // Example2 : ?$expand=Parent/CodeID (NavigationProperty/Property)
      // Example3 : ?$expand=Parent,Children (NavigationProperty, NavigationProperty)
      // Example4 : ?$expand=*
      // Example5 : ?$expand=*/$ref,Parent
      // Example6 : ?$expand=Parent($levels=2)
      // Example7 : ?$expand=*($levels=2)
      // Example8 : ?$expand=*($levels=2;$expand=Parent)
      // Example9 : ?$expand=BusinessPartner/com.sap.olingo.jpa.Person
      for (final ExpandItem item : expandOption.getExpandItems()) {
        if (item.isStar()) {
          determineAssociationsStar(sd, startResourceList, expandOption, pathList, associationNamePrefix, item);
        } else {
          determineAssociations(sd, expandOption, pathList, associationNamePrefix, Objects.requireNonNull(
              startResourceItem), item);
          // For example8 Olingo only provides one ExpandItem next level has to expand Parent
        }
        if (item.getLevelsOption() != null && item.getLevelsOption().isMax())
          LOGGER.warn(
              "Client requested expand with $levels=max. This is depricated. The support will be stop in the future");
      }
    }
    return pathList;
  }

  public static Map<JPAPath, JPAAssociationPath> determineAssociations(final JPAServiceDocument sd,
      final List<UriResource> startResourceList, final Set<JPAPath> selectOptions) throws ODataApplicationException {
    final Map<JPAPath, JPAAssociationPath> pathList = new HashMap<>();
    final StringBuilder associationNamePrefix = new StringBuilder();
    final UriResource startResourceItem = createAssociationNamePrefix(startResourceList, associationNamePrefix);

    for (final var selectOption : selectOptions) {

      final StringBuilder associationName = associationNamePrefix.length() > 0
          ? new StringBuilder(associationNamePrefix).append(selectOption.getAlias())
          : new StringBuilder(selectOption.getAlias());
      pathList.put(selectOption, determineAssociationPath(sd, (UriResourcePartTyped) startResourceItem,
          associationName));
    }

    return pathList;
  }

  private static UriResource createAssociationNamePrefix(final List<UriResource> startResourceList,
      final StringBuilder associationNamePrefix) {
    // Example1: /Organizations('3')/AdministrativeInformation?$expand=Created/User
    // Example2: /Organizations('3')/AdministrativeInformation?$expand=*
    // Example3: /CurrentUser/AdministrativeInformation?$expand=Created/User
    // Association name needs AdministrativeInformation as prefix
    UriResource startResourceItem = null;
    for (int i = startResourceList.size() - 1; i >= 0; i--) {
      startResourceItem = startResourceList.get(i);
      if (startResourceItem instanceof UriResourceEntitySet
          || startResourceItem instanceof UriResourceNavigation
          || startResourceItem instanceof UriResourceSingleton) {
        break;
      }
      associationNamePrefix.insert(0, PATH_SEPARATOR);
      associationNamePrefix.insert(0, ((UriResourceProperty) startResourceItem).getProperty().getName());
    }
    return startResourceItem;
  }

  private static void determineAssociations(final JPAServiceDocument sd, final ExpandOption expandOption,
      final Map<JPAExpandItem, JPAAssociationPath> pathList, final StringBuilder associationNamePrefix,
      final UriResource startResourceItem, final ExpandItem item) throws ODataApplicationException {

    final List<UriResource> targetResourceList = item.getResourcePath().getUriResourceParts(); // Has Cast
    final StringBuilder associationName = determinePathAlias(associationNamePrefix, targetResourceList);
    if (item.getLevelsOption() != null)
      pathList.put(new JPAExpandLevelWrapper(sd, expandOption, item), Utility.determineAssociation(sd,
          ((UriResourcePartTyped) startResourceItem).getType(), associationName));
    else
      pathList.put(new JPAExpandItemWrapper(sd, item), Utility.determineAssociation(sd,
          ((UriResourcePartTyped) startResourceItem).getType(), associationName));
  }

  private static StringBuilder determinePathAlias(final StringBuilder pathAliasPrefix,
      final List<UriResource> resourceList) {
    StringBuilder associationName;
    associationName = new StringBuilder();
    associationName.append(pathAliasPrefix);

    for (final var targetResourceItem : resourceList) {
      if (targetResourceItem.getKind() == UriResourceKind.entitySet
          || targetResourceItem.getKind() == UriResourceKind.singleton)
        continue;
      if (targetResourceItem.getKind() != UriResourceKind.navigationProperty) {
        associationName.append(((UriResourceProperty) targetResourceItem).getProperty().getName());
        associationName.append(PATH_SEPARATOR);
      } else {
        associationName.append(((UriResourceNavigation) targetResourceItem).getProperty().getName());
        break;
      }
    }
    return associationName;
  }

  private static void determineAssociationsStar(final JPAServiceDocument sd, final List<UriResource> startResourceList,
      final ExpandOption expandOption, final Map<JPAExpandItem, JPAAssociationPath> pathList,
      final StringBuilder associationNamePrefix, final ExpandItem item) throws ODataJPAUtilException {

    // As olingo does not resolve the Star operator the expand Item does not contain a resource path
    final EdmBindingTarget edmBindingTarget = determineBindingTarget(startResourceList);
    try {
      final JPAStructuredType jpaStructuredType = sd.getEntity(edmBindingTarget.getName());
      if (jpaStructuredType == null)
        throw new ODataJPAUtilException(UNKNOWN_ENTITY_TYPE, BAD_REQUEST);
      final List<JPAAssociationPath> associationPaths = jpaStructuredType.getAssociationPathList();
      for (final JPAAssociationPath path : associationPaths) {
        if (associationNamePrefix.length() == 0 ||
            path.getAlias().startsWith(associationNamePrefix.toString())) {
          final var uriInfo = new JPAUriResourceNavigationImpl(findNavigationProperty(edmBindingTarget, path));
          if (item.getLevelsOption() != null && path.getSourceType() == path.getTargetType())
            pathList.put(new JPAExpandLevelWrapper(expandOption, (JPAEntityType) path.getTargetType(),
                findNavigationProperty(edmBindingTarget, path), item), path);
          else
            pathList.put(new JPAExpandItemWrapper(item, (JPAEntityType) path.getTargetType(), uriInfo), path);
        }
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAUtilException(UNKNOWN_ENTITY_TYPE, BAD_REQUEST, e);
    }
  }

  public static EdmBindingTarget determineBindingTarget(final List<UriResource> resources) {
    return determineBindingTargetAndKeys(resources).getEdmBindingTarget();
  }

  public static EdmBindingTargetInfo determineBindingTargetAndKeys(final List<UriResource> resources) {
    EdmBindingTarget targetEdmBindingTarget = null;
    List<UriParameter> targetKeyPredicates = new ArrayList<>();
    StringBuilder navigationPropertyName = new StringBuilder();

    for (final UriResource resourceItem : resources) {
      if (resourceItem.getKind() == UriResourceKind.entitySet) {
        targetEdmBindingTarget = determineBindingTargetOfEntitySet((UriResourceEntitySet) resourceItem);
        targetKeyPredicates = ((UriResourceEntitySet) resourceItem).getKeyPredicates();
      }
      if (resourceItem.getKind() == UriResourceKind.singleton) {
        targetEdmBindingTarget = determineBindingTargetOfSingleton((UriResourceSingleton) resourceItem);
        targetKeyPredicates = Collections.emptyList();
      }
      if (resourceItem.getKind() == UriResourceKind.complexProperty) {
        navigationPropertyName.append(((UriResourceComplexProperty) resourceItem).getProperty().getName());
        navigationPropertyName.append(PATH_SEPARATOR);
      }
      if (resourceItem.getKind() == UriResourceKind.navigationProperty) {
        navigationPropertyName.append(((UriResourceNavigation) resourceItem).getProperty().getName());
        targetKeyPredicates = ((UriResourceNavigation) resourceItem).getKeyPredicates();
        final EdmBindingTarget edmBindingTarget = determineBindingTargetOfNavigation(targetEdmBindingTarget,
            (UriResourceNavigation) resourceItem, navigationPropertyName);
        if (edmBindingTarget instanceof EdmEntitySet || edmBindingTarget instanceof EdmBoundCast)
          targetEdmBindingTarget = edmBindingTarget;
        navigationPropertyName = new StringBuilder();
      }
    }
    return new EdmBindingTargetResult(targetEdmBindingTarget, targetKeyPredicates, navigationPropertyName.toString());
  }

  private static EdmBindingTarget determineBindingTargetOfNavigation(final EdmBindingTarget targetEdmBindingTarget,
      final UriResourceNavigation resourceItem, final StringBuilder navigationPropertyName) {

    final EdmBindingTarget target = targetEdmBindingTarget.getRelatedBindingTarget(navigationPropertyName
        .toString());
    if (target instanceof EdmBindingTarget) {
      if (resourceItem.getTypeFilterOnEntry() != null)
        return new EdmBoundCast((EdmEntityType) resourceItem.getTypeFilterOnEntry(), target);
      else if (resourceItem.getTypeFilterOnCollection() != null)
        return new EdmBoundCast((EdmEntityType) resourceItem.getTypeFilterOnCollection(), target);
    }
    return target;
  }

  public static List<UriParameter> determineKeyPredicates(final UriResource uriResourceItem)
      throws ODataApplicationException {

    if (uriResourceItem instanceof final UriResourceEntitySet entiySet)
      return entiySet.getKeyPredicates();
    else if (uriResourceItem instanceof final UriResourceNavigation navigation)
      return navigation.getKeyPredicates();
    else if (uriResourceItem instanceof UriResourceSingleton)
      return Collections.emptyList();
    else
      throw new ODataJPAQueryException(NOT_SUPPORTED_RESOURCE_TYPE, BAD_REQUEST, uriResourceItem.getKind().name());
  }

  public static EdmBindingTargetInfo determineModifyEntitySetAndKeys(@Nonnull final List<UriResource> resources) {
    EdmBindingTarget targetEdmTopLevel = null;
    List<UriParameter> targetKeyPredicates = new ArrayList<>();
    StringBuilder navigationPropertyName = new StringBuilder();

    for (final UriResource resourceItem : resources) {
      if (resourceItem.getKind() == UriResourceKind.entitySet) {
        targetEdmTopLevel = ((UriResourceEntitySet) resourceItem).getEntitySet();
        targetKeyPredicates = ((UriResourceEntitySet) resourceItem).getKeyPredicates();
      }
      if (resourceItem.getKind() == UriResourceKind.singleton) {
        targetEdmTopLevel = ((UriResourceSingleton) resourceItem).getSingleton();
      }
      if (resourceItem.getKind() == UriResourceKind.complexProperty) {
        navigationPropertyName.append(((UriResourceComplexProperty) resourceItem).getProperty().getName());
        navigationPropertyName.append(PATH_SEPARATOR);
      }
      if (resourceItem.getKind() == UriResourceKind.navigationProperty) {
        navigationPropertyName.append(((UriResourceNavigation) resourceItem).getProperty().getName());
        final List<UriParameter> keyPredicates = ((UriResourceNavigation) resourceItem).getKeyPredicates();
        if (!keyPredicates.isEmpty()) {
          targetKeyPredicates = keyPredicates;
          final EdmBindingTarget edmBindingTarget = targetEdmTopLevel.getRelatedBindingTarget(navigationPropertyName
              .toString());
          if (edmBindingTarget instanceof EdmEntitySet)
            targetEdmTopLevel = edmBindingTarget;
          navigationPropertyName = new StringBuilder();
        }
      }
    }
    return new EdmBindingTargetResult(targetEdmTopLevel, targetKeyPredicates, navigationPropertyName.toString());
  }

  /**
   * Converts the OData navigation list into a intermediate one. Direction is top - down usage e.g. join query.
   * <p>
   * The method only supports queries that start with an entity set or singleton.
   *
   * @param sd
   * @param resourceParts
   * @param filterOption
   * @return
   * @throws ODataApplicationException
   */
  public static List<JPANavigationPropertyInfo> determineNavigationPath(final JPAServiceDocument sd,
      final List<UriResource> resourceParts, final UriInfoResource uriInfo) throws ODataApplicationException {

    final List<JPANavigationPropertyInfo> pathList = new ArrayList<>();

    StringBuilder associationName = null;
    UriResourcePartTyped source = null;
    for (final UriResource resourcePart : resourceParts) {
      if (resourcePart instanceof UriResourceNavigation
          || resourcePart instanceof UriResourceEntitySet
          || resourcePart instanceof UriResourceSingleton) {
        if (source != null) {
          if (resourcePart instanceof final UriResourceNavigation navigation)
            extendNavigationPath(associationName, navigation.getProperty().getName());
          pathList.add(new JPANavigationPropertyInfo(sd, source, determineAssociationPath(sd, source, associationName),
              null));
        }
        source = (UriResourcePartTyped) resourcePart;
        associationName = new StringBuilder();
      } else {
        if ((resourcePart instanceof UriResourceComplexProperty
            || resourcePart instanceof final UriResourceProperty property && property.isCollection())
            && associationName != null) {
          extendNavigationPath(associationName, ((UriResourceProperty) resourcePart).getProperty().getName());
        }
      }
    }
    if (source != null)
      pathList.add(new JPANavigationPropertyInfo(sd, source,
          determineAssociationPath(sd, source, associationName), uriInfo));
    return pathList;
  }

  public static String determinePropertyNavigationPath(final List<UriResource> resources) {
    final StringBuilder pathName = new StringBuilder();
    if (resources != null) {
      for (int i = resources.size() - 1; i >= 0; i--) {
        final UriResource resourceItem = resources.get(i);
        if (resourceItem instanceof UriResourceEntitySet || resourceItem instanceof UriResourceNavigation
            || resourceItem instanceof UriResourceLambdaVariable)
          break;
        if (resourceItem instanceof UriResourceValue) {
          pathName.insert(0, VALUE_RESOURCE);
          pathName.insert(0, PATH_SEPARATOR);
        } else if (resourceItem instanceof final UriResourceProperty property) {
          pathName.insert(0, property.getProperty().getName());
          pathName.insert(0, PATH_SEPARATOR);
        }
      }
      if (pathName.length() > 0)
        pathName.deleteCharAt(0);
    }
    return pathName.toString();
  }

  public static String determinePropertyNavigationPrefix(final List<UriResource> resources) {
    return Utility.determinePropertyNavigationPath(resources).split("/\\" + Utility.VALUE_RESOURCE)[0];
  }

  /**
   * Finds the index of the first property after the last entity set or navigation resource. This is the resource that
   * will be returned in case a complex or primitive type is requested.
   * <p>
   * Example1 : /Organizations -> -1<br>
   * Example2 : /Organizations('3')/AdministrativeInformation -> 1<br>
   * Example3 : /Organizations('3')/Roles -> -1<br>
   * Example4 : /Organizations('3')/Roles/RoleCategory -> 2<br>
   * Example5 : /Organizations('3')/AdministrativeInformation/Created/User/LastName -> 4
   * Example6 : /CurrentUser/AdministrativeInformation -> 1
   */
  public static int determineStartNavigationIndex(final List<UriResource> resources) {

    if (resources != null) {
      for (int i = resources.size() - 1; i >= 0; i--) {
        final UriResource resourceItem = resources.get(i);
        if (resourceItem instanceof UriResourceEntitySet
            || resourceItem instanceof UriResourceNavigation
            || resourceItem instanceof UriResourceSingleton)
          return i == resources.size() ? -1 : i + 1;
      }
    }
    return -1;
  }

  /**
   * Used for Serializer
   */
  public static UriResourceProperty determineStartNavigationPath(final List<UriResource> resources) {

    final int index = determineStartNavigationIndex(resources);
    if (index >= 0 && resources != null)
      return (UriResourceProperty) resources.get(index);
    return null;
  }

  /**
   * Finds an entity type from a navigation property
   */
  public static EdmEntityType determineTargetEntityType(final List<UriResource> resources) {
    EdmEntityType targetEdmEntity = null;

    for (final UriResource resourceItem : resources) {
      if (resourceItem.getKind() == UriResourceKind.navigationProperty) {
        // first try the simple way like in the example
        targetEdmEntity = (EdmEntityType) ((UriResourceNavigation) resourceItem).getType();
        if (((UriResourceNavigation) resourceItem).getTypeFilterOnEntry() != null) {
          targetEdmEntity = (EdmEntityType) ((UriResourceNavigation) resourceItem).getTypeFilterOnEntry();
          LOGGER.trace(FOUND_CAST_FROM + ((UriResourceNavigation) resourceItem).getType().getName() + " to "
              + targetEdmEntity.getName());
        }
        if (((UriResourceNavigation) resourceItem).getTypeFilterOnCollection() != null) {
          targetEdmEntity = (EdmEntityType) ((UriResourceNavigation) resourceItem).getTypeFilterOnCollection();
          LOGGER.trace(FOUND_CAST_FROM + ((UriResourceNavigation) resourceItem).getType().getName() + " to "
              + targetEdmEntity.getName());
        }
      }
    }
    return targetEdmEntity;
  }

  public static boolean hasNavigation(final List<UriResource> uriResourceParts) {
    if (uriResourceParts != null) {
      for (int i = uriResourceParts.size() - 1; i >= 0; i--) {
        if (uriResourceParts.get(i) instanceof UriResourceNavigation)
          return true;
      }
    }
    return false;
  }

  public static boolean hasCollection(final List<UriResource> resourceParts) {
    if (resourceParts != null) {
      for (int i = resourceParts.size() - 1; i >= 0; i--) {
        if (isCollection(resourceParts.get(i)))
          return true;
      }
    }
    return false;
  }

  public static boolean isCollection(final UriResource resourcePart) {
    return (resourcePart instanceof final UriResourceProperty resourceProperty && resourceProperty.isCollection());
  }

  private static EdmBindingTarget determineBindingTargetOfEntitySet(final UriResourceEntitySet resourceItem) {
    EdmBindingTarget targetEdmBindingTarget;
    if (resourceItem.getTypeFilterOnCollection() != null) {
      targetEdmBindingTarget = new EdmBoundCast((EdmEntityType) resourceItem.getTypeFilterOnCollection(), resourceItem
          .getEntitySet());
      LOGGER.trace(FOUND_CAST_FROM + resourceItem.getEntitySet().getName() + " to "
          + targetEdmBindingTarget.getName());
    } else if (resourceItem.getTypeFilterOnEntry() != null) {
      targetEdmBindingTarget = new EdmBoundCast((EdmEntityType) resourceItem.getTypeFilterOnEntry(), resourceItem
          .getEntitySet());
      LOGGER.trace(FOUND_CAST_FROM + resourceItem.getEntitySet().getName() + " to "
          + targetEdmBindingTarget.getName());
    } else {
      targetEdmBindingTarget = resourceItem.getEntitySet();
    }
    return targetEdmBindingTarget;
  }

  private static EdmBindingTarget determineBindingTargetOfSingleton(final UriResourceSingleton resourceItem) {
    EdmBindingTarget targetEdmBindingTarget;
    if (resourceItem.getEntityTypeFilter() != null) {
      targetEdmBindingTarget = new EdmBoundCast(resourceItem.getEntityTypeFilter(), resourceItem.getSingleton());
    } else {
      targetEdmBindingTarget = resourceItem.getSingleton();
    }
    return targetEdmBindingTarget;
  }

  private static void extendNavigationPath(final StringBuilder associationName, final String pathSegment)
      throws ODataJPAQueryException {
    if (associationName == null)
      throw new ODataJPAQueryException(NOT_SUPPORTED_RESOURCE_TYPE, NOT_IMPLEMENTED, "");
    if (associationName.length() > 0)
      associationName.append(PATH_SEPARATOR);
    associationName.append(pathSegment);
  }

  private static EdmNavigationProperty findNavigationProperty(final EdmBindingTarget bindingTarget,
      final JPAAssociationPath path) {

    EdmStructuredType type = bindingTarget.getEntityType();
    final var last = path.getLeaf();
    for (final var item : path.getPath()) {
      if (item == last)
        return type.getNavigationProperty(item.getExternalName());
      else
        type = (EdmStructuredType) type.getProperty(item.getExternalName()).getType();
    }
    return type.getNavigationProperty(path.getAlias());
  }
}
