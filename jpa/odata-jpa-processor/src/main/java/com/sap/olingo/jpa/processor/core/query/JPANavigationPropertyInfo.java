package com.sap.olingo.jpa.processor.core.query;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.criteria.From;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourceSingleton;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterComplier;

public final class JPANavigationPropertyInfo implements JPANavigationPropertyInfoAccess {
  private static final Log LOGGER = LogFactory.getLog(JPANavigationPropertyInfo.class);
  private final JPAServiceDocument sd;
  private final UriResourcePartTyped navigationTarget;
  private JPAAssociationPath associationPath;
  private final List<UriParameter> keyPredicates;
  private From<?, ?> fromClause = null;
  private final UriInfoResource uriInfo;
  private JPAEntityType et = null;
  private JPAFilterComplier filterCompiler = null;

  /**
   *
   * Copy constructor, that does not copy the <i>from</i> clause, so the new JPANavigationPropertyInfo can be used in a
   * new query.
   * @param original
   */
  public JPANavigationPropertyInfo(final JPANavigationPropertyInfo original) {

    this.navigationTarget = original.getUriResource();
    this.associationPath = original.getAssociationPath();
    this.keyPredicates = original.getKeyPredicates();
    this.uriInfo = original.getUriInfo();
    this.sd = original.getServiceDocument();
    this.et = this.uriInfo instanceof JPAExpandItem ? ((JPAExpandItem) uriInfo).getEntityType() : null;
  }

  public JPANavigationPropertyInfo(final JPAServiceDocument sd, final JPAAssociationPath associationPath,
      final UriInfoResource uriInfo, final JPAEntityType et) {
    super();
    this.navigationTarget = null;
    this.associationPath = associationPath;
    this.keyPredicates = Collections.emptyList();
    this.uriInfo = uriInfo;
    this.sd = sd;
    this.et = et;
  }

  public JPANavigationPropertyInfo(final JPAServiceDocument sd, final UriResourcePartTyped uriResource,
      final JPAAssociationPath associationPath, final UriInfoResource uriInfo) throws ODataApplicationException {

    this.navigationTarget = uriResource;
    this.associationPath = associationPath;
    this.keyPredicates = uriResource.isCollection() ? Collections.emptyList() : Utility.determineKeyPredicates(
        uriResource);
    this.uriInfo = uriInfo;
    this.sd = sd;
  }

  @Override
  public JPAAssociationPath getAssociationPath() {
    return associationPath;
  }

  @Override
  public UriResourcePartTyped getUriResource() {
    return navigationTarget;
  }

  /**
   * Set the association path to a other entity.
   * @param associationPath
   */
  public void setAssociationPath(final JPAAssociationPath associationPath) {
    assert this.associationPath == null;
    this.associationPath = associationPath;
  }

  JPAEntityType getEntityType() throws ODataJPAModelException {
    if (et != null)
      return et;
    return determineEntityType();
  }

  private JPAEntityType determineEntityType() throws ODataJPAModelException {

    final UriResourcePartTyped resource = getUriResource();
    String castFrom = null;
    if (resource instanceof final UriResourceEntitySet entitySet) {
      et = sd.getEntity(entitySet.getEntitySet().getName());
      if (entitySet.getTypeFilterOnCollection() != null) {
        et = sd.getEntity(entitySet.getTypeFilterOnCollection());
        castFrom = ((UriResourceEntitySet) resource).getEntitySet().getName();
      } else if (entitySet.getTypeFilterOnEntry() != null) {
        et = sd.getEntity(entitySet.getTypeFilterOnEntry());
        castFrom = entitySet.getEntitySet().getName();
      }
    } else if (resource instanceof final UriResourceSingleton singleton) {
      et = sd.getEntity(singleton.getSingleton().getName());
      if (singleton.getEntityTypeFilter() != null) {
        et = sd.getEntity(singleton.getEntityTypeFilter());
        castFrom = singleton.getSingleton().getName();
      }
    } else if (resource instanceof final UriResourceNavigation navigation) {
      et = sd.getEntity(resource.getType());
      if (navigation.getTypeFilterOnEntry() != null) {
        et = sd.getEntity(navigation.getTypeFilterOnEntry());
        castFrom = navigation.getProperty().getName();
      } else if (navigation.getTypeFilterOnCollection() != null) {
        et = sd.getEntity(navigation.getTypeFilterOnCollection());
        castFrom = navigation.getProperty().getName();
      }
    } else {
      et = sd.getEntity(resource.getType());
    }

    if (et == null)
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.JOIN_TABLE_NOT_FOUND);
    if (castFrom != null)
      LOGGER.trace("Found cast from " + castFrom + " to " + et.getExternalName());
    return et;
  }

  JPAFilterComplier getFilterCompiler() {
    return filterCompiler;
  }

  From<?, ?> getFromClause() { // NOSONAR
    return fromClause;
  }

  @Override
  public List<UriParameter> getKeyPredicates() {
    return keyPredicates;
  }

  UriInfoResource getUriInfo() {
    return uriInfo;
  }

  void setFilterCompiler(final JPAFilterComplier filterCompiler) {
    assert this.filterCompiler == null;
    this.filterCompiler = filterCompiler;
  }

  /**
   * Set the from clause. This is possible only once and can not be changed later.
   * @param from
   */
  void setFromClause(final From<?, ?> from) {
    assert fromClause == null;
    fromClause = from;
  }

  private JPAServiceDocument getServiceDocument() {
    return sd;
  }

  @Override
  public String toString() {
    try {
      final String typeName = getEntityType().getExternalName();
      final String associationName = associationPath != null ? associationPath.getAlias() : "";
      return "JPANavigationPropertyInfo [et=" + typeName
          + ", associationPath=" + associationName + "]";
    } catch (final ODataJPAModelException e) {
      return super.toString();
    }
  }

}
