package org.apache.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;

public abstract class JPAAbstractQuery {

  protected final EntityManager em;
  protected final CriteriaBuilder cb;
  protected final JPAEntityType jpaEntity;
  protected final ServicDocument sd;
  // protected final EdmEntityType edmType;

  public JPAAbstractQuery(final ServicDocument sd, final JPAEntityType jpaEntityType, final EntityManager em)
      throws ODataApplicationException {
    super();
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
    this.jpaEntity = jpaEntityType;
  }

  public JPAAbstractQuery(final ServicDocument sd, final EdmEntityType edmEntityType, final EntityManager em)
      throws ODataApplicationException {
    super();
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
    try {
      this.jpaEntity = sd.getEntity(edmEntityType);
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("An error occured", HttpStatusCode.BAD_REQUEST.getStatusCode(),
          Locale.ENGLISH, e);
    }
  }

  protected javax.persistence.criteria.Expression<Boolean> createWhereByKey(final From<?, ?> root,
      final javax.persistence.criteria.Expression<Boolean> whereCondition, final List<UriParameter> keyPredicates)
      throws ODataApplicationException {
    // .../Organizations('3')
    // .../BusinessPartnerRoles(BusinessPartnerID='6',RoleCategory='C')
    javax.persistence.criteria.Expression<Boolean> compundCondition = whereCondition;

    if (keyPredicates != null) {
      for (final UriParameter keyPredicate : keyPredicates) {
        javax.persistence.criteria.Expression<Boolean> equalCondition;
        try {
          equalCondition = cb.equal(root.get(jpaEntity.getPath(keyPredicate.getName()).getLeaf()
              .getInternalName()), eliminateApostrophe(keyPredicate.getText()));
        } catch (ODataJPAModelException e) {
          throw new ODataApplicationException("Property not found", HttpStatusCode.BAD_REQUEST.getStatusCode(),
              Locale.ENGLISH, e);
        }
        if (compundCondition == null)
          compundCondition = equalCondition;
        else
          compundCondition = cb.and(compundCondition, equalCondition);
      }
    }
    return compundCondition;
  }

  private String eliminateApostrophe(final String text) {
    return text.replaceAll("'", "");
  }

  protected List<UriParameter> determineKeyPredicates(final UriResource uriResourceItem)
      throws ODataApplicationException {

    if (uriResourceItem instanceof UriResourceEntitySet)
      return ((UriResourceEntitySet) uriResourceItem).getKeyPredicates();
    else if (uriResourceItem instanceof UriResourceNavigation)
      return ((UriResourceNavigation) uriResourceItem).getKeyPredicates();
    else
      throw new ODataApplicationException("Unsupported Resource Kind '" + uriResourceItem.getKind().name() + "'",
          HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
  }

  protected abstract Root<?> getRoot();

  public abstract AbstractQuery<?> getQuery();
}