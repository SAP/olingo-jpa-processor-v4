package org.apache.olingo.jpa.processor.core.query;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAQueryException;
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
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
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
          throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
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
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.NOT_SUPPORTED_RESOURCE_TYPE,
          HttpStatusCode.BAD_REQUEST,
          uriResourceItem.getKind().name());
  }

  public abstract Root<?> getRoot();

  public abstract AbstractQuery<?> getQuery();

  protected abstract Locale getLocale();

  protected void generateDesciptionJoin(final HashMap<String, From<?, ?>> joinTables, Set<JPAPath> pathSet)
      throws ODataApplicationException {
    for (final JPAPath descriptionFieldPath : pathSet) {
      final JPADescriptionAttribute desciptionField = ((JPADescriptionAttribute) descriptionFieldPath.getLeaf());
      final List<JPAElement> pathList = descriptionFieldPath.getPath();
      Join<?, ?> join = null;
      JoinType jt;
      for (int i = 0; i < pathList.size(); i++) {
        if (i == pathList.size() - 1)
          jt = JoinType.LEFT;
        else
          jt = JoinType.INNER;
        if (i == 0) {
          join = getRoot().join(pathList.get(i).getInternalName(), jt);
          join.alias(descriptionFieldPath.getAlias());
        } else if (i < pathList.size()) {
          join = join.join(pathList.get(i).getInternalName(), jt);
          join.alias(pathList.get(i).getExternalName());
        }
      }
      if (desciptionField.isLocationJoin())
        join.on(cb.equal(join.get(desciptionField.getInternalName()), getLocale().toString()));
      else
        join.on(cb.equal(determienLocalePath(join, desciptionField), getLocale().getLanguage()));
      joinTables.put(desciptionField.getInternalName(), join);
    }
  }

  private javax.persistence.criteria.Expression<?> determienLocalePath(final Join<?, ?> join,
      final JPADescriptionAttribute desciptionField) throws ODataApplicationException {
    Path<?> p = join;
    try {
      for (final JPAElement pathElement : desciptionField.getLocaleFieldName().getPath()) {
        p = p.get(pathElement.getInternalName());
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return p;
  }
}