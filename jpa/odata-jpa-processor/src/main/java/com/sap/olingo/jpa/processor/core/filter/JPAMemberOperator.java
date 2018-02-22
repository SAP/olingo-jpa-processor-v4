package com.sap.olingo.jpa.processor.core.filter;

import java.util.Iterator;
import java.util.Set;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.query.Util;

public class JPAMemberOperator implements JPAOperator {
  private final Member member;
  private final JPAEntityType jpaEntityType;
  private final From<?, ?> root;
  private final JPAAssociationPath association;

  JPAMemberOperator(final JPAEntityType jpaEntityType, final From<?, ?> parent,
      final Member member, final JPAAssociationPath association) {
    super();
    this.member = member;
    this.jpaEntityType = jpaEntityType;
    this.root = parent;
    this.association = association;
  }

  public JPAAttribute determineAttribute() throws ODataApplicationException {
    return determineAttributePath().getLeaf();
  }

  @Override
  public Path<?> get() throws ODataApplicationException {
    final JPAPath selectItemPath = determineAttributePath();
    return determineCriteriaPath(selectItemPath);
  }

  public Member getMember() {
    return member;
  }

  private JPAPath determineAttributePath() throws ODataApplicationException {
    final StringBuilder path = new StringBuilder();
    if (association != null) {
      path.append(association.getAlias());
      path.append(JPAPath.PATH_SEPERATOR);
    }
    path.append(Util.determineProptertyNavigationPath(member.getResourcePath().getUriResourceParts()));
    if (path.lastIndexOf(JPAPath.PATH_SEPERATOR) == path.length() - 1)
      path.deleteCharAt(path.length() - 1);
    JPAPath selectItemPath = null;
    try {
      selectItemPath = jpaEntityType.getPath(path.toString());
    } catch (ODataJPAModelException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    return selectItemPath;
  }

  private Path<?> determineCriteriaPath(final JPAPath selectItemPath) {
    Path<?> p = root;
    for (final JPAElement jpaPathElement : selectItemPath.getPath()) {
      if (jpaPathElement instanceof JPADescriptionAttribute) {
        final Set<?> allJoins = root.getJoins();
        final Iterator<?> iterator = allJoins.iterator();
        while (iterator.hasNext()) {
          Join<?, ?> join = (Join<?, ?>) iterator.next();
          if (join.getAlias() != null && join.getAlias().equals(selectItemPath.getAlias())) {
            final Set<?> subJoins = join.getJoins();
            for (final Object sub : subJoins) {
              // e.g. "Organizations?$filter=Address/RegionName eq 'Kalifornien'
              // see createFromClause in JPAExecutableQuery
              if (((Join<?, ?>) sub).getAlias() != null &&
                  ((Join<?, ?>) sub).getAlias().equals(jpaPathElement.getExternalName())) {
                join = (Join<?, ?>) sub;
              }
            }
            p = join.get(((JPADescriptionAttribute) jpaPathElement).getDescriptionAttribute().getInternalName());
            break;
          }
        }
      } else
        p = p.get(jpaPathElement.getInternalName());
    }
    return p;
  }
}
