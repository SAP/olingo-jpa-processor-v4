package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.From;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterComplier;

public final class JPANavigationProptertyInfo {
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
   * Copy constructor, that does not copy the <i>from</i> clause, so the new JPANavigationProptertyInfo can be used in a
   * new query.
   * @param original
   */
  public JPANavigationProptertyInfo(final JPANavigationProptertyInfo original) {

    this.navigationTarget = original.getUriResiource();
    this.associationPath = original.getAssociationPath();
    this.keyPredicates = original.getKeyPredicates();
    this.uriInfo = original.getUriInfo();
    this.sd = original.getServiceDocument();
    this.et = this.uriInfo instanceof JPAExpandItem ? ((JPAExpandItem) uriInfo).getEntityType() : null;
  }

  public JPANavigationProptertyInfo(final JPAServiceDocument sd, final JPAAssociationPath associationPath,
      final UriInfoResource uriInfo, final JPAEntityType et) {
    super();
    this.navigationTarget = null;
    this.associationPath = associationPath;
    this.keyPredicates = new ArrayList<>(1);
    this.uriInfo = uriInfo;
    this.sd = sd;
    this.et = et;
  }

  public JPANavigationProptertyInfo(final JPAServiceDocument sd, final UriResourcePartTyped uriResource,
      final JPAAssociationPath associationPath, final UriInfoResource uriInfo) throws ODataApplicationException {

    this.navigationTarget = uriResource;
    this.associationPath = associationPath;
    this.keyPredicates = uriResource.isCollection() ? new ArrayList<>(1) : Util.determineKeyPredicates(uriResource);
    this.uriInfo = uriInfo;
    this.sd = sd;
  }

  public JPAAssociationPath getAssociationPath() {
    return associationPath;
  }

  public UriResourcePartTyped getUriResiource() {
    return navigationTarget;
  }

  /**
   * Set the association path to a other entity.
   * @param associationPath
   */
  public void setAssociationPath(JPAAssociationPath associationPath) {
    assert this.associationPath == null;
    this.associationPath = associationPath;
  }

  JPAEntityType getEntityType() throws ODataJPAModelException {
    if (et != null)
      return et;
    return sd.getEntity(getUriResiource().getType());
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

  void setFilterCompiler(JPAFilterComplier filterCompiler) {
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
}
