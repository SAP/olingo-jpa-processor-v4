package com.sap.olingo.jpa.processor.core.query;

import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.From;

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

public final class JPANavigationPropertyInfo {
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
    this.keyPredicates = uriResource.isCollection() ? Collections.emptyList() : Util.determineKeyPredicates(
        uriResource);
    this.uriInfo = uriInfo;
    this.sd = sd;
  }

  public JPAAssociationPath getAssociationPath() {
    return associationPath;
  }

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
    if (getUriResource() instanceof UriResourceEntitySet) {
      et = sd.getEntity(((UriResourceEntitySet) resource).getEntitySet().getName());
      if (((UriResourceEntitySet) resource).getTypeFilterOnCollection() != null) {
        et = sd.getEntity(((UriResourceEntitySet) resource).getTypeFilterOnCollection());
        castFrom = ((UriResourceEntitySet) resource).getEntitySet().getName();
      } else if (((UriResourceEntitySet) resource).getTypeFilterOnEntry() != null) {
        et = sd.getEntity(((UriResourceEntitySet) resource).getTypeFilterOnEntry());
        castFrom = ((UriResourceEntitySet) resource).getEntitySet().getName();
      }
    } else if (resource instanceof UriResourceSingleton) {
      et = sd.getEntity(((UriResourceSingleton) resource).getSingleton().getName());
      if (((UriResourceSingleton) resource).getEntityTypeFilter() != null) {
        et = sd.getEntity(((UriResourceSingleton) resource).getEntityTypeFilter());
        castFrom = ((UriResourceSingleton) resource).getSingleton().getName();
      }
    } else if (resource instanceof UriResourceNavigation) {
      et = sd.getEntity(resource.getType());
      if (((UriResourceNavigation) resource).getTypeFilterOnEntry() != null) {
        et = sd.getEntity(((UriResourceNavigation) resource).getTypeFilterOnEntry());
        castFrom = ((UriResourceNavigation) resource).getProperty().getName();
      } else if (((UriResourceNavigation) resource).getTypeFilterOnCollection() != null) {
        et = sd.getEntity(((UriResourceNavigation) resource).getTypeFilterOnCollection());
        castFrom = ((UriResourceNavigation) resource).getProperty().getName();
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

  List<UriParameter> getKeyPredicates() {
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
      final String assoziationName = associationPath != null ? associationPath.getAlias() : "";
      return "JPANavigationPropertyInfo [et=" + typeName
          + ", associationPath=" + assoziationName + "]";
    } catch (final ODataJPAModelException e) {
      return super.toString();
    }
  }
}
